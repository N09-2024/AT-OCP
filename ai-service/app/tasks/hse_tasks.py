from crewai import Task, Agent
from typing import Dict, Any, List


def create_hse_measures_task(agent: Agent, context_data: Dict[str, Any], previous_tasks: List[Task]) -> Task:
    description = context_data.get("description", "")
    
    return Task(
        description=(
            f"À partir des risques identifiés dans la tâche précédente et de la description : \"{description}\",\n"
            "Déduis selon les règles officielles du Standard S-HSE-SEC-31 :\n"
            "1. Les mesures de préparation (Section B)\n"
            "2. Les EPI obligatoires (Section D)\n"
            "3. Les permis complémentaires requis (Section E)\n\n"
            "Retourne UNIQUEMENT un JSON structuré :\n"
            "{\"recommendedMeasures\": [...], \"epis\": [...], \"permis\": [...], \"warnings\": [...]}"
        ),
        expected_output="Objet JSON strict avec recommendedMeasures, epis, permis et warnings.",
        agent=agent,
        context=previous_tasks,
    )
