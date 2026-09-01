"""
Gestion de la mémoire conversationnelle (couche LangChain du plan d'architecture).

Une mémoire fenêtrée par conversation (conversationId fourni par le backend
Spring Boot) : l'assistant IA se souvient des derniers échanges de l'utilisateur.
Stockage en mémoire process — suffisant pour une instance FastAPI unique ;
une persistance Redis/DB pourra s'y substituer sans changer l'API du manager.
"""

import logging
import threading
import time
from typing import Dict, Optional

from langchain.memory import ConversationBufferWindowMemory

logger = logging.getLogger(__name__)

# Durée de vie d'une conversation inactive (1 h) et taille maximale du cache.
_TTL_SECONDS = 3600
_MAX_CONVERSATIONS = 500


class ChatMemoryManager:
    """Mémoire fenêtrée (k derniers échanges) par conversationId."""

    def __init__(self, window_k: int = 6):
        self._k = window_k
        self._lock = threading.Lock()
        self._memories: Dict[str, ConversationBufferWindowMemory] = {}
        self._last_used: Dict[str, float] = {}

    def _get(self, conversation_id: str) -> ConversationBufferWindowMemory:
        with self._lock:
            self._evict_if_needed()
            memory = self._memories.get(conversation_id)
            if memory is None:
                memory = ConversationBufferWindowMemory(k=self._k, return_messages=True)
                self._memories[conversation_id] = memory
                logger.debug("Mémoire créée pour la conversation %s", conversation_id)
            self._last_used[conversation_id] = time.time()
            return memory

    def _evict_if_needed(self) -> None:
        """Supprime les conversations expirées / les plus anciennes si saturé."""
        now = time.time()
        expired = [cid for cid, ts in self._last_used.items() if now - ts > _TTL_SECONDS]
        for cid in expired:
            self._memories.pop(cid, None)
            self._last_used.pop(cid, None)
        while len(self._memories) > _MAX_CONVERSATIONS:
            oldest = min(self._last_used, key=self._last_used.get)
            self._memories.pop(oldest, None)
            self._last_used.pop(oldest, None)

    def get_history_text(self, conversation_id: Optional[str]) -> str:
        """Historique fenêtré (k derniers échanges) formaté pour le prompt."""
        if not conversation_id:
            return "(Nouvelle conversation — aucun historique.)"
        memory = self._memories.get(conversation_id)
        if memory is None:
            return "(Nouvelle conversation — aucun historique.)"
        with self._lock:
            self._last_used[conversation_id] = time.time()
            messages = list(memory.chat_memory.messages)
        if not messages:
            return "(Nouvelle conversation — aucun historique.)"
        # ConversationBufferWindowMemory stocke tout : la fenêtre s'applique ici.
        window = messages[-(self._k * 2):]
        lines = []
        for m in window:
            role = "Utilisateur" if m.type == "human" else "Assistant"
            content = str(m.content).replace("\n", " ")
            lines.append(f"{role} : {content}")
        return "\n".join(lines)

    def save_exchange(self, conversation_id: Optional[str], question: str, answer: str) -> None:
        """Enregistre l'échange (question → réponse) dans la mémoire de la conversation."""
        if not conversation_id:
            return
        try:
            memory = self._get(conversation_id)
            with self._lock:
                memory.save_context({"input": question}, {"output": answer})
        except Exception as ex:
            logger.warning("Sauvegarde mémoire impossible pour %s : %s", conversation_id, ex)

    def clear(self, conversation_id: str) -> None:
        with self._lock:
            self._memories.pop(conversation_id, None)
            self._last_used.pop(conversation_id, None)


# Instance partagée par les requêtes FastAPI (thread-safe).
chat_memory_manager = ChatMemoryManager()
