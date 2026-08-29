from crewai import Agent
from app.chains.llm_factory import get_crewai_llm


def build_completion_agent() -> Agent:
    return Agent(
        role="Agent d'Analyse de Fin de Chantier",
        goal=(
            "Vérifier la complétude et la cohérence de la déclaration de fin de travaux et des opérations de réception."
        ),
        backstory=(
            "Tu es un superviseur de réception de travaux OCP. Tu compares les travaux initialement planifiés "
            "avec ceux réellement réalisés, en surveillant l'évacuation des matériels et la remise en service sécurisée."
        ),
        llm=get_crewai_llm(temperature=0.1),
        verbose=False,
        allow_delegation=False,
    )
