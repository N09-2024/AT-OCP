from crewai import Task, Agent
from typing import Dict, Any


def create_risk_analysis_task(agent: Agent, context_data: Dict[str, Any]) -> Task:
    description = context_data.get("description", "")
    installation = context_data.get("installation", "Non spécifiée")
    equipement = context_data.get("equipement", "Non spécifié")
    type_intervention = context_data.get("typeIntervention", "Non spécifié")

    return Task(
        description=(
            f"Analyse l'intervention suivante :\n"
            f"- Description : \"{description}\"\n"
            f"- Installation : \"{installation}\"\n"
            f"- Équipement : \"{equipement}\"\n"
            f"- Type : \"{type_intervention}\"\n\n"
            "Identifie les risques applicables figurant STRICTEMENT dans la liste officielle Section A OCP.\n"
            "Retourne UNIQUEMENT un JSON structuré :\n"
            "{\"identifiedRisks\": [\"Risque 1\", \"Risque 2\"], \"inconsistencies\": []}"
        ),
        expected_output="Objet JSON strict avec identifiedRisks et inconsistencies.",
        agent=agent,
    )
