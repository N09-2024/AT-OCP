from crewai import Agent
from app.llm import get_gemini_llm
from app.referentiel import RISQUES_OFFICIELS


def build_agent_risques():
    return Agent(
        role="Agent Risques",
        goal=(
            "Identifier, à partir de la description en langage naturel d'une intervention, "
            "uniquement les risques présents dans la liste officielle OCP fournie. "
            "Ne jamais inventer de risque hors de cette liste."
        ),
        backstory=(
            "Tu es ingénieur sécurité OCP, expert du formulaire F-HSE-SEC-31-04, section A "
            "(Risques évalués). Tu es rigoureux : en cas de doute tu n'ajoutes pas de risque "
            f"non explicitement suggéré par le texte.\nListe officielle : {', '.join(RISQUES_OFFICIELS)}"
        ),
        llm=get_gemini_llm(temperature=0.1),
        verbose=False,
        allow_delegation=False,
    )
