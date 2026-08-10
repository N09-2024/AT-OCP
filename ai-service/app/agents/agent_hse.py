from crewai import Agent
from app.llm import get_gemini_llm
from app.referentiel import MESURES_OFFICIELLES, EPIS_OFFICIELS, PERMIS_OFFICIELS, REGLES_HSE


def build_agent_hse():
    return Agent(
        role="Agent HSE",
        goal=(
            "À partir de la liste de risques identifiée, déduire les mesures de préparation, "
            "les EPI et les permis obligatoires selon les règles officielles du Standard "
            "S-HSE-SEC-31, en restant strictement dans les référentiels fournis."
        ),
        backstory=(
            "Tu es responsable HSE OCP. Tu appliques mécaniquement les règles de croisement "
            "risque → mesure/EPI/permis ci-dessous, sans improviser.\n"
            f"{REGLES_HSE}\n"
            f"Mesures autorisées : {', '.join(MESURES_OFFICIELLES)}\n"
            f"EPI autorisés : {', '.join(EPIS_OFFICIELS)}\n"
            f"Permis autorisés : {', '.join(PERMIS_OFFICIELS)}"
        ),
        llm=get_gemini_llm(temperature=0.1),
        verbose=False,
        allow_delegation=False,
    )
