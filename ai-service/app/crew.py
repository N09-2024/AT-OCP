import json
import re
from crewai import Task, Crew, Process

from app.agents.agent_risques import build_agent_risques
from app.agents.agent_hse import build_agent_hse
from app.agents.agent_controle import build_agent_controle
from app.schemas import AnalyseInterventionIAResponse


def _extract_json(text: str) -> dict:
    """Les LLM entourent parfois le JSON de texte/markdown : on l'extrait de force."""
    match = re.search(r"\{.*\}", text, re.DOTALL)
    if not match:
        return {}
    try:
        return json.loads(match.group(0))
    except json.JSONDecodeError:
        return {}


def run_crew_analyse_intervention(description: str) -> AnalyseInterventionIAResponse:
    agent_risques = build_agent_risques()
    agent_hse = build_agent_hse()

    task_risques = Task(
        description=(
            f"Description de l'intervention :\n\"\"\"{description}\"\"\"\n\n"
            "Retourne UNIQUEMENT un JSON de la forme "
            '{"risques": ["..."]} listant les risques officiels concernés.'
        ),
        expected_output="Un objet JSON strict avec la clé 'risques'.",
        agent=agent_risques,
    )

    task_hse = Task(
        description=(
            "À partir des risques identifiés par l'Agent Risques (contexte de la tâche précédente) "
            "et de la description originale, retourne UNIQUEMENT un JSON de la forme :\n"
            '{"risques": ["..."], "mesures": ["..."], "epis": ["..."], "permis": ["..."], '
            '"rapport": "résumé en 1-2 phrases"}'
        ),
        expected_output="Un objet JSON strict avec les clés risques, mesures, epis, permis, rapport.",
        agent=agent_hse,
        context=[task_risques],
    )

    crew = Crew(
        agents=[agent_risques, agent_hse],
        tasks=[task_risques, task_hse],
        process=Process.sequential,
        verbose=False,
    )

    result = crew.kickoff()
    data = _extract_json(str(result))

    return AnalyseInterventionIAResponse(
        risques=data.get("risques", []),
        mesures=data.get("mesures", []),
        epis=data.get("epis", []),
        permis=data.get("permis", []),
        rapport=data.get("rapport", "Analyse CrewAI (Agent Risques → Agent HSE) terminée."),
        alertes=[],
        complet=True,
        provider="CREW_AI",
        tauxConfiance=0.85,
    )


def run_crew_controler_dossier(
    description: str,
    visite_faite: bool,
    nb_risques: int,
    nb_mesures: int,
    nb_epis: int,
    nb_permis: int,
    section_f_renseignee: bool,
) -> AnalyseInterventionIAResponse:
    # 1) On réutilise le pipeline Risques→HSE pour avoir des suggestions à jour
    suggestions = run_crew_analyse_intervention(description)

    agent_controle = build_agent_controle()

    task_controle = Task(
        description=(
            "État actuel du dossier AT (formulaire F-HSE-SEC-31-04) :\n"
            f"- Description renseignée : {'oui' if description.strip() else 'non'}\n"
            f"- Visite préalable chantier réalisée (§8.2) : {'oui' if visite_faite else 'non'}\n"
            f"- Nombre de risques cochés (section A) : {nb_risques}\n"
            f"- Nombre de mesures cochées (section B) : {nb_mesures}\n"
            f"- Nombre d'EPI cochés (section D) : {nb_epis}\n"
            f"- Nombre de permis identifiés (section E) : {nb_permis}\n"
            f"- Section F (mesures exécutant) renseignée : {'oui' if section_f_renseignee else 'non'}\n\n"
            "Retourne UNIQUEMENT un JSON : "
            '{"alertes": ["..."], "rapport": "synthèse courte", "complet": true|false}'
        ),
        expected_output="Un objet JSON strict avec les clés alertes, rapport, complet.",
        agent=agent_controle,
    )

    crew = Crew(
        agents=[agent_controle],
        tasks=[task_controle],
        process=Process.sequential,
        verbose=False,
    )
    result = crew.kickoff()
    data = _extract_json(str(result))

    return AnalyseInterventionIAResponse(
        risques=suggestions.risques,
        mesures=suggestions.mesures,
        epis=suggestions.epis,
        permis=suggestions.permis,
        rapport=data.get("rapport", "Contrôle dossier terminé."),
        alertes=data.get("alertes", []),
        complet=data.get("complet", len(data.get("alertes", [])) == 0),
        provider="CREW_AI",
        tauxConfiance=0.85,
    )
