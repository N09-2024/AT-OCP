from fastapi import FastAPI
from app.schemas import (
    AnalyseInterventionRequest,
    ControleDossierRequest,
    AnalyseInterventionIAResponse,
)
from app.crew import run_crew_analyse_intervention, run_crew_controler_dossier
from app.langchain_direct import langchain_analyser_intervention, langchain_controler_dossier

app = FastAPI(title="AT-OCP — Microservice IA", version="1.0.0")


@app.get("/health")
def health():
    return {"status": "UP"}


# ---------------------------------------------------------------------------
# Chemin LangChain direct (rapide, 1 appel Gemini)
# Appelé par LangChainProvider.java → /analyse-intervention, /controler-dossier
# ---------------------------------------------------------------------------
@app.post("/analyse-intervention", response_model=AnalyseInterventionIAResponse)
def analyse_intervention(req: AnalyseInterventionRequest):
    return langchain_analyser_intervention(req.description)


@app.post("/controler-dossier", response_model=AnalyseInterventionIAResponse)
def controler_dossier(req: ControleDossierRequest):
    return langchain_controler_dossier(
        req.description,
        req.visiteFaite,
        req.nbRisques,
        req.nbMesures,
        req.nbEpis,
        req.nbPermis,
        req.sectionFRenseignee,
    )


# ---------------------------------------------------------------------------
# Chemin CrewAI multi-agents (Agent Risques → Agent HSE → Agent Contrôle)
# Appelé par CrewAIProvider.java → /crew/analyse-intervention, /crew/controler-dossier
# ---------------------------------------------------------------------------
@app.post("/crew/analyse-intervention", response_model=AnalyseInterventionIAResponse)
def crew_analyse_intervention(req: AnalyseInterventionRequest):
    return run_crew_analyse_intervention(req.description)


@app.post("/crew/controler-dossier", response_model=AnalyseInterventionIAResponse)
def crew_controler_dossier(req: ControleDossierRequest):
    return run_crew_controler_dossier(
        req.description,
        req.visiteFaite,
        req.nbRisques,
        req.nbMesures,
        req.nbEpis,
        req.nbPermis,
        req.sectionFRenseignee,
    )
