from fastapi import APIRouter
from app.schemas.analysis import (
    AnalyzeAtRequest,
    AnalyzeAtResponse,
    AnalyseInterventionRequest,
    ControleDossierRequest,
    AnalyseInterventionIAResponse,
)
from app.services.analysis_service import AnalysisService

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
    return AnalysisService.analyze_with_langchain(req)


@router.post("/controler-dossier", response_model=AnalyseInterventionIAResponse)
def legacy_langchain_controler_dossier(req: ControleDossierRequest):
    return AnalysisService.analyze_with_langchain(req)


@router.post("/crew/analyse-intervention", response_model=AnalyseInterventionIAResponse)
def legacy_crew_analyse_intervention(req: AnalyseInterventionRequest):
    return AnalysisService.analyze_with_crew(req)


@router.post("/crew/controler-dossier", response_model=AnalyseInterventionIAResponse)
def legacy_crew_controler_dossier(req: ControleDossierRequest):
    return AnalysisService.analyze_with_crew(req)
