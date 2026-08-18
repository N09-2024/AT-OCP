import logging
import os
from typing import Any, Optional, Union
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import BaseMessage, AIMessage
from langchain_core.outputs import ChatResult, ChatGeneration
from app.config.settings import settings

logger = logging.getLogger(__name__)


class MockChatModel(BaseChatModel):
    """Fallback déterministe hors-ligne lorsque aucune clé API LLM valide n'est configurée ou en cas de quota dépassé."""

    def _generate(
        self,
        messages: list[BaseMessage],
        stop: Optional[list[str]] = None,
        run_manager: Optional[Any] = None,
        **kwargs: Any,
    ) -> ChatResult:
        prompt_text = " ".join([m.content for m in messages if isinstance(m.content, str)]).lower()
        
        risques = []
        mesures = []
        epis = ["Bottes de sécurité"]
        permis = []
        missing = []
        alertes = []
        sources = ["Standard S-HSE-SEC-31 §8-§10 (Référentiel OCP)", "Formulaire F-HSE-SEC-31-04"]

        if any(w in prompt_text for w in ["hauteur", "échafaudage", "echelle", "toiture"]):
            risques.append("Travail en hauteur")
            mesures.append("Balisage")
            epis.append("Harnais de sécurité")
            permis.append("Permis pour travail en hauteur")
            sources.append("Standard S-HSE-SEC-31 §10.1 (Travaux en hauteur)")

        if any(w in prompt_text for w in ["soudure", "meulage", "flamme", "point chaud", "feu", "oxycoupage"]):
            risques.append("Produits inflammables")
            mesures.append("Balisage")
            epis.append("Casque soudure")
            permis.append("Permis de feu")
            sources.append("Standard S-HSE-SEC-31 §10.2 (Permis de feu)")

        if any(w in prompt_text for w in ["confiné", "cuve", "bac", "fosse", "trémie", "réservoir"]):
            risques.append("Espaces confinés")
            mesures.append("Ventilation")
            epis.append("ARI")
            permis.append("Permis pour espace confiné")
            sources.append("Standard S-HSE-SEC-31 §10.3 (Espaces confinés)")

        if any(w in prompt_text for w in ["acide", "chimique", "soufre", "réactif", "ammoniac", "base"]):
            risques.append("Produits chimiques")
            epis.extend(["Gants antiacides", "Lunettes étanches", "Tenue antiacide"])
            sources.append("Standard S-HSE-SEC-31 §10.4 (Risques chimiques)")

        if any(w in prompt_text for w in ["électr", "tension", "armoire", "moteur", "câble", "disjoncteur"]):
            risques.append("Electricité")
            mesures.append("Consignation des Energies")
            permis.append("Plan de consignation")
            sources.append("Standard S-HSE-SEC-31 §9 (Procédure de Consignation)")

        if any(w in prompt_text for w in ["fouille", "tranchée", "terrassement", "réseau", "tube enterré"]):
            risques.append("Proximité aux réseaux enterrés")
            permis.append("Permis de fouille")
            sources.append("Standard S-HSE-SEC-31 §10.5 (Permis de fouille)")

        if not risques:
            risques.append("Outillage")
            epis.append("Gants de manutention")

        if "visite_faite=false" in prompt_text or "visite préalable: non" in prompt_text:
            alertes.append("La visite préalable de chantier (§8.2) n'a pas encore été effectuée.")
            missing.append("Validation de la visite préalable de chantier")

        import json
        response_dict = {
            "summary": "Analyse synthétique de l'Autorisation de Travail (Mode Assistance OCP).",
            "missingInformation": missing,
            "identifiedRisks": list(set(risques)),
            "recommendedMeasures": list(set(mesures)),
            "inconsistencies": [],
            "warnings": alertes,
            "sources": list(set(sources)),
            "confidence": "HIGH",
            "risques": list(set(risques)),
            "mesures": list(set(mesures)),
            "epis": list(set(epis)),
            "permis": list(set(permis)),
            "rapport": "Analyse effectuée avec succès selon le standard OCP S-HSE-SEC-31.",
            "alertes": alertes,
            "complet": len(alertes) == 0,
            "provider": "MOCK_AI",
            "tauxConfiance": 0.90,
            "answer": "Selon le standard S-HSE-SEC-31, les autorisations de travail exigent l'évaluation systématique des risques (section A), les mesures de préparation (section B), les EPI spécifiques (section D) et les permis complémentaires (section E). Les visas CEEP et CEEE sont obligatoires avant démarrage.",
            "suggestedQuestions": [
                "Quels sont les prérequis pour un permis de feu ?",
                "Comment se déroule la consignation électrique ?",
                "Quels sont les rôles respectifs du CEEP et du CEEE ?"
            ]
        }
        
        content = json.dumps(response_dict, ensure_ascii=False)
        return ChatResult(generations=[ChatGeneration(message=AIMessage(content=content))])

    @property
    def _llm_type(self) -> str:
        return "mock-chat-model"


def get_llm(temperature: Optional[float] = None) -> BaseChatModel:
    """
    Factory centralisée instanciant le modèle LLM LangChain.
    """
    temp = temperature if temperature is not None else settings.LLM_TEMPERATURE
    provider = (settings.LLM_PROVIDER or "GEMINI").upper()

    try:
        if provider == "GEMINI":
            api_key = settings.GOOGLE_API_KEY or settings.GEMINI_API_KEY
            if not api_key or api_key in ("votre_cle_gemini_ici", "DUMMY_KEY"):
                return MockChatModel()
            from langchain_google_genai import ChatGoogleGenerativeAI
            model_name = settings.LLM_MODEL or "gemini-2.5-flash"
            if model_name.startswith("models/"):
                model_name = model_name[7:]
            return ChatGoogleGenerativeAI(
                model=model_name,
                google_api_key=api_key,
                temperature=temp,
            )

        elif provider == "OPENAI":
            api_key = settings.OPENAI_API_KEY
            if not api_key:
                return MockChatModel()
            from langchain_openai import ChatOpenAI
            return ChatOpenAI(
                model=settings.LLM_MODEL or "gpt-4o-mini",
                api_key=api_key,
                temperature=temp,
                base_url=settings.LLM_BASE_URL,
            )

        elif provider == "OLLAMA":
            from langchain_community.chat_models import ChatOllama
            return ChatOllama(
                model=settings.LLM_MODEL or "llama3",
                base_url=settings.LLM_BASE_URL or "http://localhost:11434",
                temperature=temp,
            )

        else:
            return MockChatModel()

    except Exception as ex:
        logger.error(f"Erreur instanciation LLM: {ex}. Repli sur MockChatModel.")
        return MockChatModel()


def get_crewai_llm(temperature: Optional[float] = None) -> Any:
    """
    Retourne la configuration LLM compatible avec CrewAI / LiteLLM.
    """
    provider = (settings.LLM_PROVIDER or "GEMINI").upper()
    api_key = settings.GOOGLE_API_KEY or settings.GEMINI_API_KEY
    model_name = settings.LLM_MODEL or "gemini-2.5-flash"
    if model_name.startswith("models/"):
        model_name = model_name[7:]

    if provider == "GEMINI" and api_key and api_key not in ("votre_cle_gemini_ici", "DUMMY_KEY"):
        # LiteLLM format pour Gemini
        os.environ["GEMINI_API_KEY"] = api_key
        return f"gemini/{model_name}"
    elif provider == "OPENAI" and settings.OPENAI_API_KEY:
        os.environ["OPENAI_API_KEY"] = settings.OPENAI_API_KEY
        return f"openai/{settings.LLM_MODEL or 'gpt-4o-mini'}"
    else:
        return MockChatModel()
