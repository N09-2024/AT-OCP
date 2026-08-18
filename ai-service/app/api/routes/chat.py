from fastapi import APIRouter
from app.schemas.chat import ChatRequest, ChatResponse
from app.services.chat_service import ChatService

router = APIRouter(tags=["Chat"])


@router.post("/api/ai/chat", response_model=ChatResponse)
def chat_with_assistant(request: ChatRequest):
    """
    Assistant conversationnel pour répondre aux questions sur les AT, les risques,
    les mesures HSE et les procédures OCP en s'appuyant sur le RAG.
    """
    return ChatService.process_chat(request)
