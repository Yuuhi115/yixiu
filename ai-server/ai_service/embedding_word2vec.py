import numpy as np
import jieba

# 全局模型（单例）
W2V_PATH = "./models/merge_sgns_bigram_char300.txt"
EMBEDDING_DIM = 300

_word_vectors = {}
_loaded = False


def load_word2vec():
    global _loaded
    if _loaded:
        return

    print("正在加载 Word2Vec 模型...")
    with open(W2V_PATH, "r", encoding="utf-8", errors="ignore") as f:
        first_line = f.readline()  # 跳过 header
        for line in f:
            parts = line.rstrip().split(" ")
            word = parts[0]
            vector = np.asarray(parts[1:], dtype=np.float32)
            _word_vectors[word] = vector

    _loaded = True
    print(f"Word2Vec 加载完成，词表大小：{len(_word_vectors)}")

# 向量转换
def vector_to_blob(vec: np.ndarray) -> bytes:
    return vec.astype("float32").tobytes()


def blob_to_vector(blob: bytes) -> np.ndarray:
    return np.frombuffer(blob, dtype=np.float32)

# 文本向量化
def embed_text(text: str) -> np.ndarray:
    """
    Word2Vec 等价 embedding：
    - 分词
    - 查词向量
    - 平均池化
    """
    if not _loaded:
        load_word2vec()

    words = jieba.lcut(text)
    vectors = []

    for w in words:
        if w in _word_vectors:
            vectors.append(_word_vectors[w])

    # 如果一句话没有任何词命中词表
    if not vectors:
        return np.zeros(EMBEDDING_DIM, dtype="float32")

    vec = np.mean(vectors, axis=0)
    vec = vec.astype("float32")

    # 归一化
    norm = np.linalg.norm(vec)
    if norm > 0:
        vec = vec / norm

    return vec