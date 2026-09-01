import logging
from app.schemas.chat import ChatRequest, ChatResponse
from app.rag.retriever import rag_retriever
from app.chains.llm_factory import get_llm, MockChatModel
from app.memory.chat_memory import chat_memory_manager
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

Historique de la conversation (mémoire des échanges précédents) :
{history}

Question actuelle de l'utilisateur :
{message}

INSTRUCTIONS :
1. Réponds de façon précise, bienveillante et professionnelle, en tenant compte de l'historique.
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
        sources = []

        try:
            # =========================
            # 1. RAG
            # =========================
            try:
                rag_context, sources = rag_retriever.get_context_and_sources(
                    request.message,
                    top_k=3
                )
            except Exception as rag_ex:
                logger.exception(
                    "Erreur RAG pendant le chat : %s",
                    rag_ex
                )
                rag_context = ""
                sources = ["Standard OCP S-HSE-SEC-31"]

            # =========================
            # 2. Contexte AT
            # =========================
            at_ctx_str = "Aucune AT spécifique sélectionnée."

            if request.atContext:
                at_ctx_str = (
                    f"AT ID : {request.atContext.get('atId', 'N/A')}, "
                    f"Description : {request.atContext.get('description', 'N/A')}, "
                    f"Installation : {request.atContext.get('installation', 'N/A')}, "
                    f"Statut : {request.atContext.get('statut', 'N/A')}"
                )

            # =========================
            # 2bis. Mémoire conversationnelle (LangChain)
            # =========================
            history_str = chat_memory_manager.get_history_text(request.conversationId)

            # =========================
            # 3. LLM
            # =========================
            llm = get_llm(temperature=0.2)
            chain = CHAT_PROMPT | llm

            try:
                result = chain.invoke({
                    "rag_context": (
                        rag_context
                        or "Standard général S-HSE-SEC-31."
                    ),
                    "at_context": at_ctx_str,
                    "history": history_str,
                    "message": request.message,
                })

                content = getattr(result, "content", str(result))

                data = extract_json_from_output(content)

            except Exception as llm_ex:
                logger.exception(
                    "Erreur LLM pour le chat : %s",
                    llm_ex
                )

                # =========================
                # 4. Fallback Mock
                # =========================
                mock = MockChatModel()

                res = mock.invoke([
                    HumanMessage(content=request.message)
                ])

                data = extract_json_from_output(res.content)

            # =========================
            # 5. Réponse finale
            # =========================
            answer = data.get("answer") or (
                "D'après les procédures OCP (Standard S-HSE-SEC-31), "
                "chaque intervention doit faire l'objet d'une évaluation "
                "des risques et des validations requises avant son démarrage."
            )

            sources_list = (
                data.get("sources")
                or sources
                or ["Standard OCP S-HSE-SEC-31"]
            )

            suggested = data.get("suggestedQuestions") or [
                "Quelles sont les étapes pour obtenir un permis de feu ?",
                "Quel est le rôle exact du CEEP et du CEEE ?",
                "Quels sont les EPI obligatoires pour un travail en hauteur ?"
            ]

            # Sauvegarde de l'échange dans la mémoire de la conversation.
            chat_memory_manager.save_exchange(
                request.conversationId, request.message, answer
            )

            return ChatResponse(
                answer=answer,
                sources=sources_list,
                confidence=data.get("confidence", "HIGH"),
                suggestedQuestions=suggested,
            )

        except Exception as ex:
            logger.exception(
                "Erreur critique dans ChatService.process_chat : %s",
                ex
            )

            # Dernier filet de sécurité
            return ChatResponse(
                answer=(
                    "L'assistant IA est momentanément indisponible. "
                    "Veuillez réessayer dans quelques instants."
                ),
                sources=["Standard OCP S-HSE-SEC-31"],
                confidence="LOW",
                suggestedQuestions=[
                    "Quel est le rôle du CEEP ?",
                    "Quel est le rôle du CEEE ?"
                ],
            )