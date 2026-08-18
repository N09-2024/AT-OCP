from langchain_core.tools import tool
from app.rag.retriever import rag_retriever
from app.referentiel import RISQUES_OFFICIELS, MESURES_OFFICIELLES, EPIS_OFFICIELS, PERMIS_OFFICIELS, REGLES_HSE


@tool
def search_ocp_procedures(query: str) -> str:
    """Recherche dans la base de connaissances officielle OCP (Standard S-HSE-SEC-31, formulaires, règles)."""
    context, sources = rag_retriever.get_context_and_sources(query, top_k=3)
    if not context:
        return "Aucune procédure OCP spécifique trouvée pour cette requête. Se référer au Standard S-HSE-SEC-31 général."
    return f"Résultats trouvés (Sources : {', '.join(sources)}) :\n{context}"


@tool
def verify_hse_rules(risk_name: str) -> str:
    """Vérifie les mesures, EPI et permis obligatoires pour un risque donné selon le Standard S-HSE-SEC-31."""
    risk_lower = risk_name.lower()
    if "hauteur" in risk_lower:
        return "Règle OCP : Travail en hauteur requiert le Permis de travail en hauteur, le Harnais de sécurité, et un balisage de zone."
    if "feu" in risk_lower or "inflammable" in risk_lower or "point chaud" in risk_lower:
        return "Règle OCP : Travaux par point chaud requièrent le Permis de feu, extincteur à proximité, et écran pare-étincelles."
    if "confiné" in risk_lower:
        return "Règle OCP : Espaces confinés requièrent le Permis espace confiné, ventilation forcée, test d'atmosphère et surveillant extérieur."
    if "électr" in risk_lower:
        return "Règle OCP : Travaux électriques requièrent la Consignation des Énergies (cadenassage + VAT) et Plan de consignation."
    if "fouille" in risk_lower or "enterré" in risk_lower:
        return "Règle OCP : Proximité réseaux enterrés requiert le Permis de fouille et repérage préalable."
    if "chimique" in risk_lower or "acide" in risk_lower:
        return "Règle OCP : Risques chimiques requièrent Gants antiacides, Lunettes étanches, Tenue antiacide et rince-œil vérifié."
    return f"Risque '{risk_name}' répertorié. Appliquer les mesures de protection générale et EPI standards (Casque, Bottes)."
