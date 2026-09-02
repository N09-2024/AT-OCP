"""Package de configuration.

NB : ce package MASQUE l'ancien module app/config.py (doublon de génération,
supprimé). Toutes les constantes historiques sont ré-exportées ici pour
compatibilité (app/llm.py : `from app.config import GOOGLE_API_KEY, GEMINI_MODEL`).
"""

from app.config.settings import settings

# Constantes historiques (utilisées par app/llm.py) - chaînes, jamais None.
GOOGLE_API_KEY: str = settings.GOOGLE_API_KEY or ""
GEMINI_MODEL: str = settings.LLM_MODEL

__all__ = ["settings", "GOOGLE_API_KEY", "GEMINI_MODEL"]
