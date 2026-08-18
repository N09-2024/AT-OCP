from crewai import Agent
from app.chains.llm_factory import get_crewai_llm
from app.referentiel import RISQUES_OFFICIELS


def build_risk_agent() -> Agent:
    return Agent(
        role="Agent Risques (Évaluateur de Risques Professionnels)",
        goal=(
            "Identifier avec précision tous les risques applicables à l'intervention "
            "en se limitant strictement à la liste officielle OCP Section A."
        ),
        backstory=(
            "Tu es spécialiste de l'évaluation des risques industriels OCP. Tu maîtrises le formulaire F-HSE-SEC-31-04 Section A.\n"
            f"Liste officielle des risques : {', '.join(RISQUES_OFFICIELS)}\n"
            "Tu analyses minutieusement le contexte des travaux pour n'omettre aucun danger réel "
            "tout en évitant d'inventer des risques non pertinents."
        ),
        llm=get_crewai_llm(temperature=0.1),
        verbose=False,
        allow_delegation=False,
    )
