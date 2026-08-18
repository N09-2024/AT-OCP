from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config.settings import settings
from app.api.routes.health import router as health_router
from app.api.routes.analysis import router as analysis_router
from app.api.routes.chat import router as chat_router

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="Microservice IA pour le système AT-OCP : Multi-agents CrewAI, LangChain et RAG.",
)

# Configuration CORS pour autoriser les requêtes du backend Spring Boot et du frontend si nécessaire
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Inclusion des routeurs
app.include_router(health_router)
app.include_router(analysis_router)
app.include_router(chat_router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.AI_SERVICE_HOST,
        port=settings.AI_SERVICE_PORT,
        reload=True,
    )
