from crewai import Task, Agent
from typing import Dict, Any


def create_chat_task(agent: Agent, user_message: str, at_context: Dict[str, Any] = None) -> Task:
    context_str = ""
    if at_context:
        context_str = (
            f"\nContexte de l'AT en cours :\n"
            f"- AT ID : {at_context.get('atId', 'N/A')}\n"
            f"- Description : {at_context.get('description', 'N/A')}\n"
            f"- Statut : {at_context.get('statut', 'N/A')}\n"
        )

    return Task(
        description=(
            f"Question de l'utilisateur :\n\"{user_message}\"\n"
            f"{context_str}\n"
            "Utilise les outils de recherche documentaire OCP pour répondre avec exactitude.\n"
            "Cite obligatoirement les sources réglementaires (ex: Standard S-HSE-SEC-31 §...).\n"
            "Ne jamais inventer de règle ou procédure absente de la documentation.\n"
            "Propose 2 à 3 questions de suivi pertinentes.\n\n"
            "Retourne UNIQUEMENT un JSON :\n"
            "{\n"
            "  \"answer\": \"Réponse détaillée et pédagogique...\",\n"
            "  \"sources\": [\"Standard S-HSE-SEC-31 §8\"],\n"
            "  \"confidence\": \"HIGH\",\n"
            "  \"suggestedQuestions\": [\"Question 1\", \"Question 2\"]\n"
            "}"
        ),
        expected_output="Objet JSON strict avec answer, sources, confidence, suggestedQuestions.",
        agent=agent,
    )
