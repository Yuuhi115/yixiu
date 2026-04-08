# embedding.py
import jieba
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import os

_vectorizer = None
_knowledge_matrix = None
_knowledge_ids = []
_stopwords = set()

def load_stopwords():
    """
    加载停用词表
    """
    global _stopwords
    stopwords_file = os.path.join(os.path.dirname(__file__), 'scu_stopwords.txt')

    try:
        with open(stopwords_file, 'r', encoding='utf-8') as f:
            _stopwords = set(line.strip() for line in f if line.strip())
        print(f"[停用词] 已加载 {len(_stopwords)} 个停用词")
    except Exception as e:
        print(f"[停用词] 加载失败：{e}")
        _stopwords = set()


def jieba_tokenize(text: str) -> str:
    """
    使用 jieba 分词，去除停用词后拼接
    """
    if not _stopwords:
        load_stopwords()

    tokens = jieba.lcut(text)
    filtered_tokens = [
        t for t in tokens
        if len(t.strip()) > 0
           and t not in _stopwords
    ]
    print(f"[分词] {text} -> {filtered_tokens}")
    return " ".join(filtered_tokens)


def init_tfidf(corpus: list[str], ids: list[int]):
    """
    初始化 TF-IDF 模型
    """
    global _vectorizer, _knowledge_matrix, _knowledge_ids

    if not corpus:
        return

    # 对整个知识库先分词
    tokenized_corpus = [jieba_tokenize(text) for text in corpus]
    # print(f"corpus:{tokenized_corpus}")

    _vectorizer = TfidfVectorizer(
        max_features=5000,
        ngram_range=(1, 2)
    )

    _knowledge_matrix = _vectorizer.fit_transform(tokenized_corpus)
    _knowledge_ids = ids

    print(f"[TF-IDF+jieba] 已加载知识条数：{len(ids)}")
    print(f"知识库矩阵数据大小 ({_knowledge_matrix.data.nbytes / 1024 :.2f}KB):")


def query_similar(text: str):
    """
    查询最相似知识
    """
    if _vectorizer is None or _knowledge_matrix is None:
        print("[TF-IDF+jieba] 模型未初始化")
        return None, 0.0

    query_text = jieba_tokenize(text)
    query_vec = _vectorizer.transform([query_text])

    scores = cosine_similarity(query_vec, _knowledge_matrix)[0]
    best_idx = int(np.argmax(scores))

    return _knowledge_ids[best_idx], float(scores[best_idx])
