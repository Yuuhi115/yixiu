from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
import re
import requests
import math
import pymysql

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

    cursor.execute("SELECT knowledge_id, problem, solution FROM ai_knowledge WHERE status = 1")
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
    source_type = data.get("sourceType")
    source_id = data.get("sourceId")

    if not problem or not solution:
        return jsonify({"code": 400, "msg": "problem 或 solution 不能为空", "data": None})

    conn = get_db()
    cursor = conn.cursor()
    if source_id is None or source_id == '':
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

# ========= 重构TF-IDF向量 =========
@app.route("/api/v1/ai/knowledge/rebuild", methods=["POST"])
def rebuild_tfidf():
    load_knowledge()
    return jsonify({"code": 200, "msg": "TF-IDF向量重建成功"})

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
    # if is_new_conversation and (best_id is None or score < SIMILARITY_THRESHOLD):
    #     return jsonify({
    #         "code": 200,
    #         "type": "MANUAL",
    #         "answer": "该问题暂无法智能判断，建议提交人工维修申请。",
    #         "headline": generate_headline(question)
    #     })

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
        "hitKnowledgeId": best_id
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


# ========= 任务类型映射表 =========
CATEGORY_MAP = {
    "软件系统": 1,
    "网络通讯": 2,
    "硬件维修": 3,
    "移动外设": 4,
    "综合咨询": 5
}


# ========= AI 报修分类接口 =========
@app.route("/api/v1/ai/ask/getTaskSkillId", methods=["POST"])
def classify_task():
    data = request.json
    description = data.get("problemDescription")

    if not description:
        return jsonify({"code": 400, "msg": "报修内容不能为空", "data": None})

    try:
        # 调用 DeepSeek 进行逻辑分类
        category_name = call_deepseek_for_classification(description)

        # 将 AI 返回的文字映射为数字序号
        # 如果 AI 返回了预期之外的词，默认归类为 "综合咨询" (4)
        category_id = CATEGORY_MAP.get(category_name, 4)

        print(f"AI 分类结果: {category_name} -> 序号: {category_id}")

        return jsonify({
            "code": 200,
            "msg": "分类成功",
            "data": category_id,
            "categoryName": category_name  # 可选：返回分类名称方便前端调试
        })
    except Exception as e:
        print(f"AI 分类异常: {str(e)}")
        return jsonify({"code": 500, "msg": "AI 分类服务异常", "data": 4})


def call_deepseek_for_classification(description: str) -> str:
    headers = {
        "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
        "Content-Type": "application/json"
    }

    # 精心设计的系统 Prompt，强制 AI 只能输出固定的分类名称
    system_content = (
        "你是一个校园电脑维修分类引擎。请根据用户的报修描述，将其归类到以下5个固定类别之一：\n"
        "[软件系统, 网络通讯, 硬件维修, 移动外设, 综合咨询]。\n"
        "【严格要求】：\n"
        "1. 必须且只能输出这5个词中的一个，禁止输出任何其他文字、标点或解释。\n"
        "2. 如果描述太简短无法判断，请输出“综合咨询”。"
    )

    payload = {
        "model": "deepseek-chat",
        "messages": [
            {"role": "system", "content": system_content},
            {"role": "user", "content": f"请分类该描述：\"{description}\""}
        ],
        "temperature": 0.3,  # 降低随机性，使分类更稳定
        "max_tokens": 10  # 限制输出长度
    }

    resp = requests.post(
        f"{DEEPSEEK_URL}/chat/completions",
        headers=headers,
        json=payload,
        timeout=10
    )
    resp.raise_for_status()

    # 获取结果并清洗掉可能存在的多余字符
    raw_result = resp.json()["choices"][0]["message"]["content"].strip()
    clean_result = re.sub(r"[^\u4e00-\u9fa5]", "", raw_result)  # 只保留中文
    return clean_result




BETA = 0.5


@app.route("/api/v1/ai/ask/updateAndGetExperts", methods=["POST"])
def update_and_get_experts():
    req_data = request.get_json()

    if not req_data or req_data.get("code") != 200:
        return jsonify({"code": 400, "msg": "无效的请求数据格式", "data": None})

    data = req_data.get("data", {})
    target_skill_id = data.get("skillId")
    volunteers_data = data.get("volunteerScoreDtoList", [])

    db = get_db()
    cursor = db.cursor(pymysql.cursors.DictCursor)

    updated_results = []

    try:
        # ==========================================
        # 1. 计算最新得分并落库 (适配新表结构)
        # ==========================================
        for v_data in volunteers_data:
            v_id = v_data.get("volunteerId")
            history_list = v_data.get("requestCategoryAndScoreList", [])

            skill_stats = {}
            for item in history_list:
                s_id = item.get("skillId")
                score = item.get("score", 0)
                if s_id not in skill_stats:
                    skill_stats[s_id] = {"task_count": 0, "total_score": 0}
                skill_stats[s_id]["task_count"] += 1
                skill_stats[s_id]["total_score"] += score

            for s_id, stats in skill_stats.items():
                n = stats["task_count"]
                total_score = stats["total_score"]
                avg_score = total_score / n if n > 0 else 0.0

                # 门槛：不足 2 单，得分为 0
                if n >= 2:
                    bayesian_score = round(avg_score * (1 + BETA * math.log(n)))
                else:
                    bayesian_score = 0.000

                # 使用 ON DUPLICATE KEY UPDATE，保护自增主键 skill_rel_id 不变
                sql_upsert = """
                    INSERT INTO volunteer_skill 
                    (volunteer_id, skill_id, task_count, average_score, bayesian_score)
                    VALUES (%s, %s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE
                    task_count = VALUES(task_count),
                    average_score = VALUES(average_score),
                    bayesian_score = VALUES(bayesian_score)
                """
                cursor.execute(sql_upsert, (v_id, s_id, n, avg_score, bayesian_score))

                if s_id == target_skill_id:
                    updated_results.append({
                        "volunteerId": v_id,
                        "skillId": s_id,
                        "newCount": n,
                        "skilledScore": round(bayesian_score, 2),
                        "isExpert": False
                    })

        # ==========================================
        # 2. 统计学判定专家 (均值 + 1.5倍标准差)
        # ==========================================
        expert_ids = []
        if target_skill_id is not None:
            # 修改查询字段以匹配数据库
            sql_scores = """
                SELECT volunteer_id, bayesian_score 
                FROM volunteer_skill 
                WHERE skill_id = %s AND task_count >= 2
            """
            cursor.execute(sql_scores, (target_skill_id,))
            valid_records = cursor.fetchall()

            if valid_records:
                scores = [float(record['bayesian_score']) for record in valid_records]

                if len(scores) > 1:
                    mean_score = np.mean(scores)
                    std_dev = np.std(scores, ddof=0)
                    threshold = mean_score + 0.52 * std_dev

                    for r in valid_records:
                        if float(r['bayesian_score']) >= threshold:
                            expert_ids.append(r['volunteer_id'])
                elif len(scores) == 1:
                    expert_ids.append(valid_records[0]['volunteer_id'])

            # ==========================================
            # 3. 将专家状态同步回数据库的 is_expert 字段
            # ==========================================
            # 先将该领域所有人重置为非专家
            cursor.execute(
                "UPDATE volunteer_skill SET is_expert = 0 WHERE skill_id = %s",
                (target_skill_id,)
            )
            # 再将达标的人设为专家
            if expert_ids:
                format_strings = ','.join(['%s'] * len(expert_ids))
                sql_set_expert = f"UPDATE volunteer_skill SET is_expert = 1 WHERE skill_id = %s AND volunteer_id IN ({format_strings})"
                cursor.execute(sql_set_expert, [target_skill_id] + expert_ids)

            # 修正本次参与任务者的 JSON 返回标签
            for res in updated_results:
                if res["volunteerId"] in expert_ids:
                    res["isExpert"] = True

        db.commit()

        print(f"更新结果: {updated_results}")

        return jsonify({
            "code": 200,
            "msg": "分值更新与专家判定成功",
            "data": {
                "skillId": target_skill_id,
                "expertIds": expert_ids,
                "updatedVolunteers": updated_results
            }
        })

    except Exception as e:
        db.rollback()
        return jsonify({"code": 500, "msg": f"系统错误: {str(e)}", "data": None})
    finally:
        db.close()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8084, debug=True)
