from pydantic import BaseModel, Field
from typing import List


class AnalyseInterventionRequest(BaseModel):
    description: str = ""


class ControleDossierRequest(BaseModel):
    description: str = ""
    visiteFaite: bool = False
    nbRisques: int = 0
    nbMesures: int = 0
    nbEpis: int = 0
    nbPermis: int = 0
    sectionFRenseignee: bool = False


# ⚠️ Les noms de champs DOIVENT correspondre exactement aux propriétés
# du DTO Java AnalyseInterventionIAResponse (désérialisation Jackson par nom).
class AnalyseInterventionIAResponse(BaseModel):
    risques: List[str] = Field(default_factory=list)
    mesures: List[str] = Field(default_factory=list)
    epis: List[str] = Field(default_factory=list)
    permis: List[str] = Field(default_factory=list)
    rapport: str = ""
    alertes: List[str] = Field(default_factory=list)
    complet: bool = True
    provider: str = "CREW_AI"
    tauxConfiance: float = 0.0
