"""
Chemin rapide LangChain (1 seul appel Gemini).
Utilisé par LangChainProvider.java via /analyse-intervention et /controler-dossier.
"""
from langchain.prompts import ChatPromptTemplate
from app.llm import get_gemini_llm
from app.referentiel import (
    RISQUES_OFFICIELS, MESURES_OFFICIELLES, EPIS_OFFICIELS,
    PERMIS_OFFICIELS, REGLES_HSE,
)
from app.schemas import AnalyseInterventionIAResponse
from app.crew import _extract_json

PROMPT_ANALYSE = ChatPromptTemplate.from_template(
    """Tu es un assistant HSE OCP. À partir de la description d'intervention ci-dessous,
propose UNIQUEMENT des éléments issus des référentiels officiels suivants (jamais d'invention) :

Risques possibles : {risques}
Mesures possibles : {mesures}
EPI possibles : {epis}
Permis possibles : {permis}

Règles de croisement à respecter :
{regles}

Description : \"\"\"{description}\"\"\"

Réponds UNIQUEMENT avec un JSON strict :
{{"risques": [...], "mesures": [...], "epis": [...], "permis": [...], "rapport": "1-2 phrases"}}"""
)

PROMPT_CONTROLE = ChatPromptTemplate.from_template(
    """Tu contrôles la complétude d'un dossier d'Autorisation de Travail OCP (F-HSE-SEC-31-04).
État : description="{description}" visite_faite={visite_faite} nb_risques={nb_risques}
nb_mesures={nb_mesures} nb_epis={nb_epis} nb_permis={nb_permis} section_f_renseignee={section_f}

Réponds UNIQUEMENT avec un JSON strict :
{{"alertes": [...], "rapport": "synthèse courte", "complet": true|false}}"""
)


def langchain_analyser_intervention(description: str) -> AnalyseInterventionIAResponse:
    llm = get_gemini_llm(temperature=0.2)
    chain = PROMPT_ANALYSE | llm
    result = chain.invoke({
        "description": description,
        "risques": ", ".join(RISQUES_OFFICIELS),
        "mesures": ", ".join(MESURES_OFFICIELLES),
        "epis": ", ".join(EPIS_OFFICIELS),
        "permis": ", ".join(PERMIS_OFFICIELS),
        "regles": REGLES_HSE,
    })
    data = _extract_json(result.content)
    return AnalyseInterventionIAResponse(
        risques=data.get("risques", []),
        mesures=data.get("mesures", []),
        epis=data.get("epis", []),
        permis=data.get("permis", []),
        rapport=data.get("rapport", "Analyse LangChain/Gemini terminée."),
        provider="LANG_CHAIN",
        tauxConfiance=0.8,
    )


def langchain_controler_dossier(
    description: str, visite_faite: bool, nb_risques: int, nb_mesures: int,
    nb_epis: int, nb_permis: int, section_f: bool,
) -> AnalyseInterventionIAResponse:
    llm = get_gemini_llm(temperature=0.1)
    chain = PROMPT_CONTROLE | llm
    result = chain.invoke({
        "description": description,
        "visite_faite": visite_faite,
        "nb_risques": nb_risques,
        "nb_mesures": nb_mesures,
        "nb_epis": nb_epis,
        "nb_permis": nb_permis,
        "section_f": section_f,
    })
    data = _extract_json(result.content)
    suggestions = langchain_analyser_intervention(description)
    return AnalyseInterventionIAResponse(
        risques=suggestions.risques,
        mesures=suggestions.mesures,
        epis=suggestions.epis,
        permis=suggestions.permis,
        rapport=data.get("rapport", "Contrôle terminé."),
        alertes=data.get("alertes", []),
        complet=data.get("complet", len(data.get("alertes", [])) == 0),
        provider="LANG_CHAIN",
        tauxConfiance=0.8,
    )
