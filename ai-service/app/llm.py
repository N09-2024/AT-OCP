from langchain_google_genai import ChatGoogleGenerativeAI
from app.config import GOOGLE_API_KEY, GEMINI_MODEL


def get_gemini_llm(temperature: float = 0.2) -> ChatGoogleGenerativeAI:
    """LLM Gemini partagé par LangChain (appel direct) et par les agents CrewAI."""
    key = (
        GOOGLE_API_KEY
        if (GOOGLE_API_KEY and GOOGLE_API_KEY != "votre_cle_gemini_ici")
        else "DUMMY_KEY_FOR_HEALTHCHECK"
    )
    return ChatGoogleGenerativeAI(
        model=GEMINI_MODEL,
        google_api_key=key,
        temperature=temperature,
        convert_system_message_to_human=True,
    )
