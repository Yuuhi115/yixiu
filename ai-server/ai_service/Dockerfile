FROM python:3.12-slim

# 防止 Python 输出缓存
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1

WORKDIR /app

# 先复制依赖文件（利用 Docker 缓存）
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple/

# 再复制代码和模型
COPY . .

# 暴露端口
EXPOSE 8084

# 用 gunicorn 启动（1 worker + 1 thread，极省内存）
CMD ["gunicorn", "-w", "1", "-k", "gthread", "--threads", "2", "-b", "0.0.0.0:8084", "app:app"]
