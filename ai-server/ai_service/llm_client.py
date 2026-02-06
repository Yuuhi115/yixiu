import requests
from config import *

def chat(prompt: str) -> str:
    resp = requests.post(
        f"{DEEPSEEK_URL}/v1/chat/completions",
        headers={
            "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
            "Content-Type": "application/json"
        },
        json={
            "models": CHAT_MODEL,
            "messages": [
                {"role": "system", "content": "你是广外义修系统的电脑维修助手"},
                {"role": "user", "content": prompt}
            ]
        }
    )

    resp.raise_for_status()
    return resp.json()["choices"][0]["message"]["content"]