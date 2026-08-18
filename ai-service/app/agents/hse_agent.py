from crewai import Agent
from app.chains.llm_factory import get_crewai_llm
from app.referentiel import MESURES_OFFICIELLES, EPIS_OFFICIELS, PERMIS_OFFICIELS, REGLES_HSE


def build_hse_agent() -> Agent:
    return Agent(
        role="Agent HSE (Spécialiste Sécurité Industrielle)",
        goal=(
            "À partir des risques et de l'intervention, déduire les mesures de préparation (Section B), "
            "les EPI obligatoires (Section D) et les permis requis (Section E) selon les règles officielles OCP."
        ),
        backstory=(
            "Tu es ingénieur HSE sur site industriel OCP. Tu appliques rigoureusement le Standard S-HSE-SEC-31.\n"
            f"{REGLES_HSE}\n"
            f"Mesures autorisées : {', '.join(MESURES_OFFICIELLES)}\n"
            f"EPI autorisés : {', '.join(EPIS_OFFICIELS)}\n"
            f"Permis autorisés : {', '.join(PERMIS_OFFICIELS)}\n"
            "Tu ne recommandes que des mesures et EPI figurant dans ce référentiel officiel."
        ),
        llm=get_crewai_llm(temperature=0.1),
        verbose=False,
        allow_delegation=False,
    )
