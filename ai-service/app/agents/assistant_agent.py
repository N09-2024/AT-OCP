from crewai import Agent
from app.chains.llm_factory import get_crewai_llm
from app.tools.ocp_tools import search_ocp_procedures, verify_hse_rules


def build_assistant_agent() -> Agent:
    return Agent(
        role="Assistant AT OCP",
        goal=(
            "Fournir des réponses précises, documentées et claires aux utilisateurs sur le processus d'Autorisation de Travail, "
            "les exigences HSE et le standard S-HSE-SEC-31, en s'appuyant rigoureusement sur les connaissances OCP."
        ),
        backstory=(
            "Tu es l'assistant digital d'information HSE pour les intervenants et exploitants OCP. "
            "Tu expliques la procédure d'autorisation de travail, le logigramme en 9 étapes, les visas CEEP/CEEE/HCEP/HCEE/HMEP/HMEE, "
            "et les permis requis. Si une information n'est pas couverte par la documentation, tu l'indiques en toute transparence."
        ),
        llm=get_crewai_llm(temperature=0.2),
        tools=[search_ocp_procedures, verify_hse_rules],
        verbose=False,
        allow_delegation=False,
    )
