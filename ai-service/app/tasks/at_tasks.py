from crewai import Task, Agent
from typing import Dict, Any, List


def create_at_synthesis_task(agent: Agent, context_data: Dict[str, Any], previous_tasks: List[Task]) -> Task:
    description = context_data.get("description", "")
    visite_faite = context_data.get("visiteFaite", False)
    section_f = context_data.get("sectionFRenseignee", False)
    at_id = context_data.get("atId", "Non attribué")

    return Task(
        description=(
            f"Consolide l'analyse complète de l'Autorisation de Travail (AT: {at_id}) :\n"
            f"- Description renseignée : {'Oui' if description.strip() else 'Non'}\n"
            f"- Visite préalable chantier (§8.2) : {'Oui' if visite_faite else 'Non'}\n"
            f"- Mesures exécutant (Section F) : {'Oui' if section_f else 'Non'}\n\n"
            "Combine les risques identifiés par l'Agent Risques et les mesures/EPI/permis déduits par l'Agent HSE.\n"
            "Produis une synthèse globale, signale les informations manquantes éventuelles et les avertissements.\n"
            "RAPPEL : Ne valide pas l'AT (seuls CEEP/CEEE valident).\n\n"
            "Retourne UNIQUEMENT un JSON structuré final :\n"
            "{\n"
            "  \"summary\": \"Synthèse globale de l'AT...\",\n"
            "  \"missingInformation\": [\"...\"],\n"
            "  \"identifiedRisks\": [\"...\"],\n"
            "  \"recommendedMeasures\": [\"...\"],\n"
            "  \"inconsistencies\": [\"...\"],\n"
            "  \"warnings\": [\"...\"],\n"
            "  \"sources\": [\"Standard S-HSE-SEC-31\", \"Formulaire F-HSE-SEC-31-04\"],\n"
            "  \"confidence\": \"HIGH\",\n"
            "  \"risques\": [\"...\"],\n"
            "  \"mesures\": [\"...\"],\n"
            "  \"epis\": [\"...\"],\n"
            "  \"permis\": [\"...\"],\n"
            "  \"rapport\": \"Synthèse pour affichage formulaire\",\n"
            "  \"alertes\": [\"...\"],\n"
            "  \"complet\": true|false,\n"
            "  \"provider\": \"CREW_AI\",\n"
            "  \"tauxConfiance\": 0.85\n"
            "}"
        ),
        expected_output="Objet JSON strict complet avec toutes les clés d'analyse AT.",
        agent=agent,
        context=previous_tasks,
    )
