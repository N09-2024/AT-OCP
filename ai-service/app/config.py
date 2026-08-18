import os
from dotenv import load_dotenv

load_dotenv()

# Désactivation permanente de la télémétrie en arrière-plan (évite tout crash/timeout sans internet)
os.environ["OTEL_SDK_DISABLED"] = "true"
os.environ["CREWAI_TELEMETRY_OPT_OUT"] = "true"

GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY", "")
GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")

if GOOGLE_API_KEY:
    os.environ["GOOGLE_API_KEY"] = GOOGLE_API_KEY
    os.environ["GEMINI_API_KEY"] = GOOGLE_API_KEY
else:
    # Ne bloque pas le démarrage : permet d'exécuter /health et les routes
    # en mode dégradé (Spring Boot basculera automatiquement sur MockAIProvider).
    print("[WARN] GOOGLE_API_KEY non défini - les appels Gemini réels échoueront.")
