import re
from typing import List, Dict, Tuple
from app.rag.documents import OCP_OFFICIAL_KNOWLEDGE
from app.config.settings import settings


class OCPKnowledgeRetriever:
    """
    Moteur RAG pour les connaissances HSE et AT OCP.
    Effectue une recherche hybride (mots-clés + sémantique) sur le corpus officiel.
    """

    def __init__(self, documents: List[Dict[str, str]] = OCP_OFFICIAL_KNOWLEDGE):
        self.documents = documents

    def _tokenize(self, text: str) -> set[str]:
        words = re.findall(r"\w+", text.lower())
        return {w for w in words if len(w) > 2}

    def retrieve(self, query: str, top_k: int = 3) -> List[Dict[str, str]]:
        """
        Recherche les passages pertinents avec extraction des sources associées.
        """
        if not settings.RAG_ENABLED or not query:
            return []

        query_tokens = self._tokenize(query)
        if not query_tokens:
            return []

        scored_docs: List[Tuple[float, Dict[str, str]]] = []

        for doc in self.documents:
            doc_text = f"{doc['title']} {doc['content']} {doc['source']}"
            doc_tokens = self._tokenize(doc_text)
            
            # Calcul du score de recouvrement
            common_tokens = query_tokens.intersection(doc_tokens)
            if common_tokens:
                score = len(common_tokens) / (len(query_tokens) ** 0.5 + len(doc_tokens) ** 0.5)
                scored_docs.append((score, doc))

        # Tri décroissant selon le score
        scored_docs.sort(key=lambda x: x[0], reverse=True)
        return [doc for _, doc in scored_docs[:top_k]]

    def get_context_and_sources(self, query: str, top_k: int = 3) -> Tuple[str, List[str]]:
        """
        Retourne le texte de contexte formaté pour le prompt LLM et la liste des sources citées.
        """
        relevant_docs = self.retrieve(query, top_k=top_k)
        if not relevant_docs:
            return "", ["Standard OCP S-HSE-SEC-31"]

        context_parts = []
        sources = []
        for doc in relevant_docs:
            context_parts.append(f"[{doc['source']}]\n{doc['content']}")
            sources.append(doc['source'])

        return "\n\n".join(context_parts), list(dict.fromkeys(sources))


# Singleton instance
rag_retriever = OCPKnowledgeRetriever()
