from crewai import Agent
from app.chains.llm_factory import get_crewai_llm


def build_safety_check_agent() -> Agent:
    return Agent(
        role="Agent Contrôle Sécurité Pré-Démarrage",
        goal=(
            "Analyser la cohérence globale du dossier AT avant le début effectif de l'intervention "
            "et identifier d'éventuels angles morts sécuritaires."
        ),
        backstory=(
            "Tu es un auditeur de sécurité terrain OCP. Tu vérifies que les permis complémentaires, "
            "les EPI requis et les consignations sont parfaitement adaptés à la typologie de l'intervention.\n"
            "Tu rédiges des recommandations préventives claires et constructives."
        ),
        llm=get_crewai_llm(temperature=0.1),
        verbose=False,
        allow_delegation=False,
    )
