from fastapi import APIRouter
from app.config.settings import settings

router = APIRouter(tags=["Health"])


@router.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": settings.APP_NAME,
        "version": settings.APP_VERSION,
        "llm_provider": settings.LLM_PROVIDER,
        "llm_model": settings.LLM_MODEL,
        "rag_enabled": settings.RAG_ENABLED,
    }
