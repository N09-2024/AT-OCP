import logging
from app.schemas.chat import ChatRequest, ChatResponse
from app.rag.retriever import rag_retriever
from app.chains.llm_factory import get_llm, MockChatModel
from langchain.prompts import ChatPromptTemplate
from langchain_core.messages import HumanMessage
from app.crews.at_crew import extract_json_from_output

logger = logging.getLogger(__name__)

CHAT_PROMPT = ChatPromptTemplate.from_template(
    """Tu es l'Assistant IA officiel pour le système d'Autorisations de Travail du Groupe OCP (Standard S-HSE-SEC-31).

Contexte documentaire OCP (Sources fiables) :
{rag_context}

Contexte de l'AT consultée :
{at_context}

Historique / Question utilisateur :
{message}

INSTRUCTIONS :
1. Réponds de façon précise, bienveillante et professionnelle.
2. Appuie-toi PRIORITAIREMENT sur le contexte documentaire ci-dessus.
3. Si l'information n'existe pas dans les standards OCP fournis, dis-le clairement au lieu d'inventer une règle.
4. Propose 2 ou 3 questions courtes et pertinentes pour approfondir.

Réponds UNIQUEMENT sous forme d'un objet JSON strict :
{{
  "answer": "Explication détaillée avec références réglementaires...",
  "sources": ["Standard OCP S-HSE-SEC-31 §8"],
  "confidence": "HIGH",
  "suggestedQuestions": ["Question 1", "Question 2"]
}}"""
)


class ChatService:
    @staticmethod
    def process_chat(request: ChatRequest) -> ChatResponse:
        rag_context, sources = rag_retriever.get_context_and_sources(request.message, top_k=3)
        
        at_ctx_str = "Aucune AT spécifique sélectionnée."
        if request.atContext:
            at_ctx_str = (
                f"AT ID : {request.atContext.get('atId', 'N/A')}, "
                f"Description : {request.atContext.get('description', 'N/A')}, "
                f"Installation : {request.atContext.get('installation', 'N/A')}, "
                f"Statut : {request.atContext.get('statut', 'N/A')}"
            )

        llm = get_llm(temperature=0.2)
        chain = CHAT_PROMPT | llm
        data = {}

        try:
            result = chain.invoke({
                "rag_context": rag_context or "Standard général S-HSE-SEC-31.",
                "at_context": at_ctx_str,
                "message": request.message,
            })
            data = extract_json_from_output(getattr(result, "content", str(result)))
        except Exception as ex:
            logger.warning(f"Erreur lors de l'appel LLM pour le chat ({ex}). Repli sur le Mock RAG.")
            mock = MockChatModel()
            res = mock.invoke([HumanMessage(content=request.message)])
            data = extract_json_from_output(res.content)

        answer = data.get("answer") or (
            "D'après les procédures OCP (Standard S-HSE-SEC-31), chaque intervention doit faire l'objet "
            "d'une visite préalable (étape 2) et de la validation des visas CEEP et CEEE avant tout commencement."
        )
        sources_list = data.get("sources") or sources or ["Standard OCP S-HSE-SEC-31"]
        suggested = data.get("suggestedQuestions") or [
            "Quelles sont les étapes pour obtenir un permis de feu ?",
            "Quel est le rôle exact du CEEP et du CEEE ?",
            "Quels sont les EPI obligatoires pour un travail en hauteur ?"
        ]

        return ChatResponse(
            answer=answer,
            sources=sources_list,
            confidence=data.get("confidence", "HIGH"),
            suggestedQuestions=suggested,
        )
