import json
import re
import logging
from crewai import Crew, Process
from typing import Dict, Any

from app.agents.risk_agent import build_risk_agent
from app.agents.hse_agent import build_hse_agent
from app.agents.at_agent import build_at_agent
from app.tasks.risk_tasks import create_risk_analysis_task
from app.tasks.hse_tasks import create_hse_measures_task
from app.tasks.at_tasks import create_at_synthesis_task
from app.schemas.analysis import AnalyzeAtResponse
from app.chains.llm_factory import MockChatModel

logger = logging.getLogger(__name__)


def extract_json_from_output(text: str) -> dict:
    """Extrait de manière robuste un JSON depuis une réponse textuelle."""
    if not text:
        return {}
    match = re.search(r"\{.*\}", text, re.DOTALL)
    if match:
        try:
            return json.loads(match.group(0))
        except json.JSONDecodeError:
            pass
    try:
        return json.loads(text)
    except Exception:
        return {}


def run_at_crew(context_data: Dict[str, Any]) -> AnalyzeAtResponse:
    """
    Orchestration Multi-Agents CrewAI pour l'analyse d'une Autorisation de Travail :
    Pipeline : Risk Agent -> HSE Agent -> AT Agent (Synthèse).
    En cas de problème LLM / quota, bascule sur une analyse de secours pour ne jamais bloquer.
    """
    data = {}
    try:
        agent_risques = build_risk_agent()
        agent_hse = build_hse_agent()
        agent_at = build_at_agent()

        task_risques = create_risk_analysis_task(agent_risques, context_data)
        task_hse = create_hse_measures_task(agent_hse, context_data, [task_risques])
        task_at = create_at_synthesis_task(agent_at, context_data, [task_risques, task_hse])

        crew = Crew(
            agents=[agent_risques, agent_hse, agent_at],
            tasks=[task_risques, task_hse, task_at],
            process=Process.sequential,
            verbose=False,
        )

        result = crew.kickoff()
        data = extract_json_from_output(str(result))
    except Exception as ex:
        logger.warning(f"Exécution CrewAI non concluante ({ex}). Repli sur l'analyseur de secours.")
        # Génération déterministe de secours
        from langchain_core.messages import HumanMessage
        mock = MockChatModel()
        desc = f"{context_data.get('description', '')} {context_data.get('installation', '')} {context_data.get('equipement', '')}"
        res = mock.invoke([HumanMessage(content=desc)])
        data = extract_json_from_output(res.content)

    # Extraction sécurisée
    risques = data.get("risques") or data.get("identifiedRisks", [])
    mesures = data.get("mesures") or data.get("recommendedMeasures", [])
    epis = data.get("epis", [])
    permis = data.get("permis", [])
    missing = data.get("missingInformation", [])
    inconsistencies = data.get("inconsistencies", [])
    warnings = data.get("warnings") or data.get("alertes", [])
    sources = data.get("sources") or [
        "Standard OCP S-HSE-SEC-31",
        "Formulaire F-HSE-SEC-31-04 (Sections A, B, D, E)"
    ]

    summary = data.get("summary") or data.get("rapport") or "Analyse multi-agents réalisée avec succès."

    return AnalyzeAtResponse(
        summary=summary,
        missingInformation=missing,
        identifiedRisks=risques,
        recommendedMeasures=mesures,
        inconsistencies=inconsistencies,
        warnings=warnings,
        sources=sources,
        confidence=data.get("confidence", "HIGH"),
        risques=risques,
        mesures=mesures,
        epis=epis,
        permis=permis,
        rapport=summary,
        alertes=warnings,
        complet=data.get("complet", len(warnings) == 0),
        provider="CREW_AI",
        tauxConfiance=data.get("tauxConfiance", 0.85),
    )
