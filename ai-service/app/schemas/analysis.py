from pydantic import BaseModel, Field
from typing import List, Optional


class AnalyzeAtRequest(BaseModel):
    atId: Optional[str] = None
    description: str = ""
    typeIntervention: Optional[str] = None
    niveau: Optional[str] = None
    installation: Optional[str] = None
    equipement: Optional[str] = None
    risques: List[str] = Field(default_factory=list)
    mesures: List[str] = Field(default_factory=list)
    epi: List[str] = Field(default_factory=list)
    moyensAcces: List[str] = Field(default_factory=list)
    
    # Rétrocompatibilité avec les formulaires rapides
    visiteFaite: bool = False
    nbRisques: int = 0
    nbMesures: int = 0
    nbEpis: int = 0
    nbPermis: int = 0
    sectionFRenseignee: bool = False


# Alias rétrocompatibles
AnalyseInterventionRequest = AnalyzeAtRequest
ControleDossierRequest = AnalyzeAtRequest


class AnalyzeAtResponse(BaseModel):
    summary: str = ""
    missingInformation: List[str] = Field(default_factory=list)
    identifiedRisks: List[str] = Field(default_factory=list)
    recommendedMeasures: List[str] = Field(default_factory=list)
    inconsistencies: List[str] = Field(default_factory=list)
    warnings: List[str] = Field(default_factory=list)
    sources: List[str] = Field(default_factory=list)
    confidence: str = "HIGH"  # HIGH, MEDIUM, LOW

    # Champs de compatibilité directe avec le DTO existant AnalyseInterventionIAResponse
    risques: List[str] = Field(default_factory=list)
    mesures: List[str] = Field(default_factory=list)
    epis: List[str] = Field(default_factory=list)
    permis: List[str] = Field(default_factory=list)
    rapport: str = ""
    alertes: List[str] = Field(default_factory=list)
    complet: bool = True
    provider: str = "CREW_AI"
    tauxConfiance: float = 0.85


AnalyseInterventionIAResponse = AnalyzeAtResponse
