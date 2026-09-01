from crewai import Agent
from app.llm import get_gemini_llm
from app.referentiel import RISQUES_OFFICIELS
from app.tools.ocp_tools import query_ocp_database, search_ocp_procedures


def build_agent_risques():
    return Agent(
        role="Agent Analyste Risques",
        goal=(
            "Identifier, à partir de la description en langage naturel d'une intervention, "
            "uniquement les risques présents dans la liste officielle OCP (référentiel en base "
            "PostgreSQL consultable via l'outil SQL, ou liste ci-dessous). "
            "Ne jamais inventer de risque hors de cette liste."
        ),
        backstory=(
            "Tu es ingénieur sécurité OCP, expert du formulaire F-HSE-SEC-31-04, section A "
            "(Risques évalués). Tu es rigoureux : en cas de doute tu n'ajoutes pas de risque "
            f"non explicitement suggéré par le texte.\nListe officielle : {', '.join(RISQUES_OFFICIELS)}"
        ),
        llm=get_gemini_llm(temperature=0.1),
        tools=[query_ocp_database, search_ocp_procedures],
        verbose=False,
        allow_delegation=True,
    )
