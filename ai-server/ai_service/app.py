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
    data = request.json

    question = data.get("question")
    conversation_id = data.get("conversationId")

    if not question:
        return jsonify({"code": 400, "msg": "问题不能为空"})

    is_new_conversation = conversation_id is None

    clean_q = clean_text(question)

    best_id, score = query_similar(clean_q)
    print(f"最相似知识id：{best_id}, 分数:{score}")

    # 新对话且未命中则不调用deepseek
    if is_new_conversation and (best_id is None or score < SIMILARITY_THRESHOLD):
        return jsonify({
            "code": 200,
            "type": "MANUAL",
            "answer": "该问题暂无法智能判断，建议提交人工维修申请。",
            "headline": generate_headline(question)
        })

    history_messages = []
    if not is_new_conversation:
        history_messages = fetch_recent_messages(conversation_id)

    context = ""
    if best_id is not None and score >= SIMILARITY_THRESHOLD:
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
    answer = call_deepseek_chat_with_history(
        history_messages=history_messages,
        context=context,
        question=question
    )

    result = {
        "code": 200,
        "type": "AI",
        "answer": answer,
        "similarity": round(score, 3) if best_id else None,
        "hit_knowledge_id": best_id
    }

    if is_new_conversation:
        result["headline"] = generate_headline(question)

    return jsonify(result)


def call_deepseek_chat_with_history(history_messages, context, question):
    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json"
    }

    messages = [
        {
            "role": "system",
            "content": "你是一名高校电脑维修助手，请结合上下文与对话历史帮助学生解决问题。"
        }
    ]

    # 历史对话
    if history_messages:
        for msg in history_messages:
            messages.append({
                "role": msg["role"],
                "content": msg["content"]
            })

    # 命中知识库才给 context
    if context:
        messages.append({
            "role": "system",
            "content": context
        })

    # 当前问题
    messages.append({
        "role": "user",
        "content": question
    })

    payload = {
        "model": "deepseek-chat",
        "messages": messages
    }
    print("payload:", payload)

    resp = requests.post(
        f"{DEEPSEEK_URL}/chat/completions",
        headers=headers,
        json=payload,
        timeout=30
    )
    resp.raise_for_status()

    return resp.json()["choices"][0]["message"]["content"]

def generate_headline(question: str) -> str:
    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": "deepseek-chat",
        "messages": [
            {
                "role": "system",
                "content": "你是一个助手，请将用户的问题概括成不超过15字的对话标题。"
            },
            {
                "role": "user",
                "content": question
            }
        ]
    }

    resp = requests.post(
        f"{DEEPSEEK_URL}/chat/completions",
        headers=headers,
        json=payload,
        timeout=15
    )
    resp.raise_for_status()

    return resp.json()["choices"][0]["message"]["content"].strip()

def fetch_recent_messages(conversation_id, limit=6):
    conn = get_db()
    cursor = conn.cursor(pymysql.cursors.DictCursor)

    cursor.execute("""
        SELECT role, content
        FROM ai_chat_message
        WHERE conversation_id = %s
        ORDER BY message_id ASC
        LIMIT %s
    """, (conversation_id, limit))

    rows = cursor.fetchall()
    cursor.close()
    conn.close()
    print(f"最近对话：{rows}")

    # 反转顺序（最早 → 最新）
    return list(rows)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8084, debug=True)
