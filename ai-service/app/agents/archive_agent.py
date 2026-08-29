from crewai import Agent
from app.chains.llm_factory import get_crewai_llm


def build_archive_agent() -> Agent:
    return Agent(
        role="Agent d'Audit d'Archivage et Intégrité",
        goal=(
            "Vérifier la traçabilité complète et la conformité documentaire du dossier avant archivage définitif."
        ),
        backstory=(
            "Tu es un auditeur de conformité réglementaire OCP. Tu vérifies que chaque étape du logigramme S-HSE-SEC-31 §7 "
            "a été rigoureusement respectée et que toutes les signatures et visas nécessaires sont documentés."
        ),
        llm=get_crewai_llm(temperature=0.1),
        verbose=False,
        allow_delegation=False,
    )
