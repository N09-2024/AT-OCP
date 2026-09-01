from fastapi import APIRouter
from app.schemas.analysis import (
    AnalyzeAtRequest,
    AnalyzeAtResponse,
    AnalyseInterventionRequest,
    ControleDossierRequest,
    AnalyseInterventionIAResponse,
)
from app.services.analysis_service import AnalysisService
from app.langchain_direct import (
    langchain_analyser_intervention,
    langchain_controler_dossier,
)

router = APIRouter(tags=["Analysis"])


@router.post("/api/ai/analyze-at", response_model=AnalyzeAtResponse)
def analyze_at(request: AnalyzeAtRequest):
    """
    Endpoint principal d'analyse d'Autorisation de Travail par le Crew multi-agents (CrewAI).
    Analyse le contexte complet : description, installation, équipement, risques, mesures, etc.
    """
    return AnalysisService.analyze_with_crew(request)


# ---------------------------------------------------------------------------
# Routes de rétrocompatibilité pour les anciens appels LangChain et CrewAI
# ---------------------------------------------------------------------------

@router.post("/analyse-intervention", response_model=AnalyseInterventionIAResponse)
def legacy_langchain_analyse_intervention(req: AnalyseInterventionRequest):
    """Chaîne LangChain directe (transformations prompt + référentiels)."""
    return langchain_analyser_intervention(req.description)


@router.post("/controler-dossier", response_model=AnalyseInterventionIAResponse)
def legacy_langchain_controler_dossier(req: ControleDossierRequest):
    """Contrôle de complétude réel via la chaîne LangChain PROMPT_CONTROLE."""
    return langchain_controler_dossier(
        req.description,
        req.visiteFaite,
        req.nbRisques,
        req.nbMesures,
        req.nbEpis,
        req.nbPermis,
        req.sectionFRenseignee,
    )


@router.post("/crew/analyse-intervention", response_model=AnalyseInterventionIAResponse)
def legacy_crew_analyse_intervention(req: AnalyseInterventionRequest):
    """CrewAI : Agent Analyste Risques → Agent Inspecteur HSE."""
    from app.crew import run_crew_analyse_intervention
    return run_crew_analyse_intervention(req.description)


@router.post("/crew/controler-dossier", response_model=AnalyseInterventionIAResponse)
def legacy_crew_controler_dossier(req: ControleDossierRequest):
    """CrewAI : pipeline complet + Agent Contrôleur de Dossier."""
    from app.crew import run_crew_controler_dossier
    return run_crew_controler_dossier(
        req.description,
        req.visiteFaite,
        req.nbRisques,
        req.nbMesures,
        req.nbEpis,
        req.nbPermis,
        req.sectionFRenseignee,
    )
