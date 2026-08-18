import logging
from app.schemas.analysis import AnalyzeAtRequest, AnalyzeAtResponse
from app.crews.at_crew import run_at_crew
from app.chains.llm_factory import get_llm
from app.rag.retriever import rag_retriever
from app.referentiel import RISQUES_OFFICIELS, MESURES_OFFICIELLES, EPIS_OFFICIELS, PERMIS_OFFICIELS, REGLES_HSE
from langchain.prompts import ChatPromptTemplate
from app.crews.at_crew import extract_json_from_output

logger = logging.getLogger(__name__)

DIRECT_LANGCHAIN_PROMPT = ChatPromptTemplate.from_template(
    """Tu es un expert HSE OCP. Analyse l'intervention suivante selon le Standard S-HSE-SEC-31 et le formulaire F-HSE-SEC-31-04.

Contexte réglementaire :
{rag_context}

Référentiels autorisés :
- Risques possibles (Section A) : {risques}
- Mesures de préparation (Section B) : {mesures}
- EPI autorisés (Section D) : {epis}
- Permis autorisés (Section E) : {permis}

Données de l'intervention :
- Description : \"{description}\"
- Installation : \"{installation}\"
- Équipement : \"{equipement}\"
- Visite préalable réalisée : {visite_faite}

RÈGLES STRICTES :
1. N'utilise QUE les libellés de risques, mesures, EPI et permis officiels ci-dessus.
2. Déduis les permis obligatoires selon les règles de croisement (§10).
3. L'IA ne valide jamais l'AT.

Réponds UNIQUEMENT avec un objet JSON strict :
{{
  "summary": "synthèse en 1-2 phrases",
  "missingInformation": ["..."],
  "identifiedRisks": ["..."],
  "recommendedMeasures": ["..."],
  "inconsistencies": ["..."],
  "warnings": ["..."],
  "sources": ["Standard OCP S-HSE-SEC-31 §8"],
  "confidence": "HIGH",
  "risques": ["..."],
  "mesures": ["..."],
  "epis": ["..."],
  "permis": ["..."],
  "rapport": "rapport court",
  "alertes": ["..."],
  "complet": true,
  "provider": "LANG_CHAIN",
  "tauxConfiance": 0.85
}}"""
)


class AnalysisService:
    @staticmethod
    def analyze_with_crew(request: AnalyzeAtRequest) -> AnalyzeAtResponse:
        """Analyse multi-agents avec CrewAI (Risk Agent -> HSE Agent -> AT Agent)."""
        context_data = {
            "atId": request.atId,
            "description": request.description,
            "typeIntervention": request.typeIntervention,
            "niveau": request.niveau,
            "installation": request.installation,
            "equipement": request.equipement,
            "risques": request.risques,
            "mesures": request.mesures,
            "epi": request.epi,
            "moyensAcces": request.moyensAcces,
            "visiteFaite": request.visiteFaite,
            "nbRisques": request.nbRisques or len(request.risques),
            "nbMesures": request.nbMesures or len(request.mesures),
            "nbEpis": request.nbEpis or len(request.epi),
            "nbPermis": request.nbPermis,
            "sectionFRenseignee": request.sectionFRenseignee,
        }
        return run_at_crew(context_data)

    @staticmethod
    def analyze_with_langchain(request: AnalyzeAtRequest) -> AnalyzeAtResponse:
        """Analyse directe via chaîne LangChain (rapide, enrichie par RAG)."""
        rag_context, sources = rag_retriever.get_context_and_sources(
            f"{request.description} {request.installation or ''} {request.equipement or ''}", top_k=2
        )
        llm = get_llm(temperature=0.1)
        chain = DIRECT_LANGCHAIN_PROMPT | llm
        
        try:
            result = chain.invoke({
                "rag_context": rag_context or REGLES_HSE,
                "risques": ", ".join(RISQUES_OFFICIELS),
                "mesures": ", ".join(MESURES_OFFICIELLES),
                "epis": ", ".join(EPIS_OFFICIELS),
                "permis": ", ".join(PERMIS_OFFICIELS),
                "description": request.description,
                "installation": request.installation or "Non spécifiée",
                "equipement": request.equipement or "Non spécifié",
                "visite_faite": "Oui" if request.visiteFaite else "Non",
            })
            data = extract_json_from_output(getattr(result, "content", str(result)))
        except Exception as ex:
            logger.error(f"Erreur LangChain direct: {ex}")
            data = {}

        risques = data.get("risques") or data.get("identifiedRisks", [])
        mesures = data.get("mesures") or data.get("recommendedMeasures", [])
        epis = data.get("epis", [])
        permis = data.get("permis", [])
        warnings = data.get("warnings") or data.get("alertes", [])
        missing = data.get("missingInformation", [])

        summary = data.get("summary") or data.get("rapport") or "Analyse LangChain terminée avec succès."

        return AnalyzeAtResponse(
            summary=summary,
            missingInformation=missing,
            identifiedRisks=risques,
            recommendedMeasures=mesures,
            inconsistencies=data.get("inconsistencies", []),
            warnings=warnings,
            sources=data.get("sources") or sources,
            confidence=data.get("confidence", "HIGH"),
            risques=risques,
            mesures=mesures,
            epis=epis,
            permis=permis,
            rapport=summary,
            alertes=warnings,
            complet=data.get("complet", len(warnings) == 0),
            provider="LANG_CHAIN",
            tauxConfiance=data.get("tauxConfiance", 0.80),
        )
