from typing import List, Dict, Tuple
from app.rag.retriever import rag_retriever


class RAGService:
    @staticmethod
    def get_context_and_sources(query: str, top_k: int = 3) -> Tuple[str, List[str]]:
        return rag_retriever.get_context_and_sources(query, top_k=top_k)

    @staticmethod
    def search_documents(query: str, top_k: int = 3) -> List[Dict[str, str]]:
        return rag_retriever.retrieve(query, top_k=top_k)
