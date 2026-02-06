from flask import Flask, request, jsonify
from flask_cors import CORS
import pymysql
import re
import requests

from embedding import init_tfidf, query_similar
from config import (
    DB_CONFIG,
    DEEPSEEK_API_KEY,
    DEEPSEEK_URL,
    SIMILARITY_THRESHOLD
)

app = Flask(__name__)
CORS(app)


def get_db():
    return pymysql.connect(**DB_CONFIG)


def clean_text(text: str) -> str:
    if not text:
        return ""
    text = text.lower()
    text = re.sub(r"\s+", " ", text)
    text = re.sub(r"[^\u4e00-\u9fa5a-z0-9%#@&\[\]]", "", text)
    return text.strip()


def build_knowledge_text(problem: str, solution: str) -> str:
    return f"{clean_text(problem)} {clean_text(solution)}"


# ========== TF-IDF 初始化 ==========
def load_knowledge():
    conn = get_db()
    cursor = conn.cursor(pymysql.cursors.DictCursor)

    cursor.execute("SELECT knowledge_id, problem, solution FROM ai_knowledge")
    rows = cursor.fetchall()

    cursor.close()
    conn.close()

    corpus = []
    ids = []

    for row in rows:
        corpus.append(clean_text(row["problem"]))
        ids.append(row["knowledge_id"])

    if corpus:
        init_tfidf(corpus, ids)


# 服务启动时加载知识
load_knowledge()


@app.route("/api/v1/ai/health", methods=["GET"])
def health():
    return "ok"


# ========= 知识入库 =========
@app.route("/api/v1/ai/knowledge", methods=["POST"])
def add_knowledge():
    data = request.json
    problem = data.get("problem")
    solution = data.get("solution")
    source_type = data.get("source_type")
    source_id = data.get("source_id")

    if not problem or not solution:
        return jsonify({"code": 400, "msg": "problem 或 solution 不能为空", "data": None})

    conn = get_db()
    cursor = conn.cursor()
    if source_id is None:
        sql = """
            INSERT INTO ai_knowledge (source_type, problem, solution)
            VALUES (%s, %s, %s)
        """
        cursor.execute(sql, (source_type, problem, solution))
        conn.commit()
    else:
        sql = """
            INSERT INTO ai_knowledge (source_type, problem, solution, source_id)
            VALUES (%s, %s, %s, %s)
        """
        cursor.execute(sql, (source_type, problem, solution, source_id))
        conn.commit()
    # 获取刚插入记录的 knowledge_id
    knowledge_id = cursor.lastrowid
    print(f"新增知识ID: {knowledge_id}")

    cursor.close()
    conn.close()

    # 🔥 新增知识后重建 TF-IDF
    load_knowledge()

    return jsonify({"code": 200, "msg": "知识入库成功", "data": knowledge_id})


# ========= 智能问答 =========
@app.route("/api/v1/ai/ask", methods=["POST"])
def ask():
    question = request.json.get("question")

    if not question:
        return jsonify({"code": 400, "msg": "问题不能为空"})

    clean_q = clean_text(question)

    best_id, score = query_similar(clean_q)
    print(f"最相似知识id：{best_id}, 分数:{score}")

    if best_id is None or score < SIMILARITY_THRESHOLD:
        return jsonify({
            "code": 200,
            "type": "MANUAL",
            "answer": "该问题暂无法智能判断，建议提交人工维修申请。"
        })

    conn = get_db()
    cursor = conn.cursor(pymysql.cursors.DictCursor)
    cursor.execute(
        "SELECT problem, solution FROM ai_knowledge WHERE knowledge_id=%s",
        (best_id,)
    )
    row = cursor.fetchone()
    cursor.close()
    conn.close()

    context = f"""
电脑故障/问题：
{row['problem']}

解决方案：
{row['solution']}
""".strip()
    print("上下文：", context)
    answer = call_deepseek_chat(context, question)

    return jsonify({
        "code": 200,
        "type": "AI",
        "answer": answer,
        "similarity": round(score, 3)
    })


def call_deepseek_chat(context: str, question: str) -> str:
    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": "deepseek-chat",
        "messages": [
            {
                "role": "system",
                "content": "你是一名高校电脑维修助手，请基于提供的维修案例回答问题。"
            },
            {
                "role": "user",
                "content": f"{context}\n\n学生问题：{question}"
            }
        ]
    }

    resp = requests.post(
        f"{DEEPSEEK_URL}/chat/completions",
        headers=headers,
        json=payload,
        timeout=30
    )
    resp.raise_for_status()

    return resp.json()["choices"][0]["message"]["content"]


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8084, debug=True)
