import os
from typing import Optional
from dotenv import load_dotenv

load_dotenv()

class Settings:
    APP_NAME: str = "AT-OCP AI Microservice"
    APP_VERSION: str = "2.0.0"
    AI_SERVICE_PORT: int = int(os.getenv("AI_SERVICE_PORT", "8000"))
    AI_SERVICE_HOST: str = os.getenv("AI_SERVICE_HOST", "0.0.0.0")
    
    # LLM Configuration
    LLM_PROVIDER: str = os.getenv("LLM_PROVIDER", "GEMINI").upper()  # GEMINI, OPENAI, OLLAMA, MOCK
    LLM_MODEL: str = os.getenv("LLM_MODEL", "gemini-2.5-flash")
    LLM_BASE_URL: Optional[str] = os.getenv("LLM_BASE_URL", None)
    LLM_TEMPERATURE: float = float(os.getenv("LLM_TEMPERATURE", "0.2"))
    
    # API Keys
    GOOGLE_API_KEY: Optional[str] = os.getenv("GOOGLE_API_KEY") or os.getenv("GEMINI_API_KEY")
    GEMINI_API_KEY: Optional[str] = os.getenv("GEMINI_API_KEY") or os.getenv("GOOGLE_API_KEY")
    OPENAI_API_KEY: Optional[str] = os.getenv("OPENAI_API_KEY")
    
    # Telemetry and CrewAI Opt-out
    CREWAI_TELEMETRY_OPT_OUT: bool = True
    OTEL_SDK_DISABLED: bool = True
    
    # RAG Settings
    RAG_ENABLED: bool = os.getenv("RAG_ENABLED", "true").lower() in ("true", "1", "yes")
    TOP_K_DOCUMENTS: int = int(os.getenv("TOP_K_DOCUMENTS", "4"))
    SIMILARITY_THRESHOLD: float = float(os.getenv("SIMILARITY_THRESHOLD", "0.3"))

    # Connecteur SQL (LangChain) — PostgreSQL du backend, LECTURE SEULE.
    # DATABASE_URL prioritaire ; sinon assemblé depuis DB_HOST/DB_PORT/...
    DATABASE_URL: Optional[str] = os.getenv("DATABASE_URL")
    DB_HOST: Optional[str] = os.getenv("DB_HOST")
    DB_PORT: int = int(os.getenv("DB_PORT", "5432"))
    DB_NAME: str = os.getenv("DB_NAME", "at_ocp_db")
    DB_USER: str = os.getenv("DB_USER", os.getenv("DB_USERNAME", "at_ocp_user"))
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "at_ocp_secret")


settings = Settings()

# Synchronize os.environ for third-party libraries (LiteLLM, CrewAI, LangChain)
if settings.GOOGLE_API_KEY:
    os.environ["GOOGLE_API_KEY"] = settings.GOOGLE_API_KEY
    os.environ["GEMINI_API_KEY"] = settings.GOOGLE_API_KEY
if settings.OPENAI_API_KEY:
    os.environ["OPENAI_API_KEY"] = settings.OPENAI_API_KEY

os.environ["OTEL_SDK_DISABLED"] = "true"
os.environ["CREWAI_TELEMETRY_OPT_OUT"] = "true"
