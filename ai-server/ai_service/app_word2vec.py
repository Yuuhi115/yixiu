from flask import Flask, request, jsonify
from flask_cors import CORS
import pymysql
import json
import re
import requests
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from embedding_word2vec import embed_text, vector_to_blob, blob_to_vector

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

# 文本拼接（仅用于向量化）
def build_knowledge_text(problem: str, solution: str) -> str:
    return f"""
问题描述：
{clean_text(problem)}

解决方案：
{clean_text(solution)}
""".strip()

def clean_text(text: str) -> str:
    if not text:
        return ""
    text = text.lower()
    # 替换多个空格为一个空格
    text = re.sub(r"\s+", " ", text)
    # 删除非中文、英文、数字的字符
    text = re.sub(r"[^\u4e00-\u9fa5a-z0-9]", "", text)
    return text.strip()

@app.route("/api/v1/ai/health", methods=["GET"])
def health():
    return "ok"

# 知识入库接口（Java 定时调用）
@app.route("/api/v1/ai/knowledge", methods=["POST"])
def add_knowledge():
    data = request.json
    problem = data.get("problem")
    solution = data.get("solution")
    source_type = data.get("source_type")
    print("问题：", problem)


    if not problem or not solution:
        return jsonify({"code": 500,"msg": "problem 或 solution 不能为空","data": None})

    # 拼接文本并向量化
    text = build_knowledge_text(problem, solution)
    embedding_vector = embed_text(text)
    embedding = vector_to_blob(embedding_vector)

    conn = get_db()
    cursor = conn.cursor()

    sql = """
        INSERT INTO ai_knowledge (source_type, problem, solution, embedding)
        VALUES (%s, %s, %s, %s)
    """
    cursor.execute(sql, (
        source_type,
        problem,
        solution,
        embedding
    ))

    conn.commit()
    cursor.close()
    conn.close()

    return jsonify({"code":200, "msg": "知识入库成功","data": None})

# 知识检索 + 问答接口

@app.route("/api/v1/ai/ask", methods=["POST"])
def ask():
    data = request.json
    question = data.get("question")

    if not question:
        return jsonify({"code":400,"msg": "问题不能为空","data": None})

    # 用户问题向量
    query_vector = np.array(embed_text(clean_text(question))).reshape(1, -1)

    # 读取所有知识
    conn = get_db()
    cursor = conn.cursor(pymysql.cursors.DictCursor)
    cursor.execute("SELECT knowledge_id, problem, solution, embedding FROM ai_knowledge")
    rows = cursor.fetchall()
    cursor.close()
    conn.close()

    if not rows:
        return jsonify({
            "type": "MANUAL",
            "answer": "当前暂无可用知识，请提交人工维修申请。"
        })

    best_score = 0
    best_row = None

    # 计算相似度
    for row in rows:
        db_vector_blob = row["embedding"]
        db_vector = blob_to_vector(db_vector_blob)
        score = cosine_similarity(query_vector.reshape(1, -1),
                                  db_vector.reshape(1, -1))[0][0]

        if score > best_score:
            best_score = score
            best_row = row
        print("相似度：", score)
        print("问题：", row['problem'])
        print("解决方案：", row['solution'])

    print("最佳相似度：", best_score)
    print("最佳问题：", best_row['problem'])
    print("最佳解决方案：", best_row['solution'])
    # 相似度不达标
    if best_score < SIMILARITY_THRESHOLD:
        return jsonify({
            "type": "MANUAL",
            "answer": "该问题暂无法智能判断，建议提交正式维修申请。"
        })

    # 构建上下文
    context = f"""
问题：
{best_row['problem']}

解决方案：
{best_row['solution']}
""".strip()

    # 调用 DeepSeek 生成最终回答
    answer = call_deepseek_chat(context, question)

    return jsonify({
        "code":200,
        "msg": "问题处理成功",
        "type": "AI",
        "answer": answer,
        "similarity": round(float(best_score), 3)
    })


# DeepSeek 问答生成

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
                "content": f"""
以下是相关维修案例：
{context}

学生问题：
{question}
"""
            }
        ]
    }
    url = f"{DEEPSEEK_URL}/chat/completions"
    resp = requests.post(url, headers=headers, json=payload)
    resp.raise_for_status()

    return resp.json()["choices"][0]["message"]["content"]


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8084, debug=True)