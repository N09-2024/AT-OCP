# Microservice IA — AT-OCP

Microservice indépendant d'assistance intelligente pour le système d'Autorisation de Travail (AT) du Groupe OCP, conforme au **Standard S-HSE-SEC-31** et au formulaire **F-HSE-SEC-31-04**.

---

## 1. Architecture Globale

```
                         FRONTEND
                     React (MUI / TS)
                           │
                           │ REST API (JWT)
                           ▼
                    SPRING BOOT
                  Backend métier
                           │
                           │ HTTP REST
                           ▼
                 PYTHON AI SERVICE (FastAPI)
                           │
                           ▼
                        CREWAI
                Orchestration multi-agents
                           │
             ┌─────────────┼─────────────┐
             │             │             │
             ▼             ▼             ▼
        Agent AT      Agent HSE     Agent Risques
             │             │             │
             └─────────────┼─────────────┘
                           │
                           ▼
                       LANGCHAIN
                 Prompts, Tools & RAG
                           │
                           ▼
                          LLM
              (Gemini, OpenAI, Ollama, Mock)
```

---

## 2. Responsabilités Distinctes

### CREWAI (Orchestration Multi-Agents)
- Définition et spécialisation des agents (`at_agent`, `hse_agent`, `risk_agent`, `assistant_agent`).
- Définition des tâches séquentielles (`at_tasks`, `hse_tasks`, `risk_tasks`, `chat_tasks`).
- Organisation du workflow d'analyse et coordination entre les agents.

### LANGCHAIN (Intégration LLM & RAG)
- Abstraction et usine du LLM (`llm_factory.py`) permettant de changer de modèle via `.env`.
- Gestion des prompts structurés et chaînes d'exécution.
- Moteur RAG avec récupération contextuelle et citations de sources réglementaires.
- Outils LangChain (`search_ocp_procedures`, `verify_hse_rules`).

### SPRING BOOT (Cœur Métier)
- Gestion de la sécurité, authentification JWT et droits RBAC.
- Respect strict du workflow des visas : CEEP → CEEE → HCEP → HCEE → HMEP → HMEE.
- Mécanisme de résilience (Circuit Breaker) basculant automatiquement sur le Mock si l'IA est indisponible.
- **Règle absolue** : L'IA ne valide jamais, ne signe jamais et ne modifie aucun statut d'AT.

---

## 3. Agents Spécialisés

| Agent | Rôle & Responsabilité | Référentiel |
|---|---|---|
| **Agent AT** | Analyse de complétude, vérification de cohérence, détection des données manquantes, synthèse | Formulaire F-HSE-SEC-31-04 |
| **Agent HSE** | Déduction des mesures de préparation (Section B), des EPI (Section D) et permis (Section E) | Standard S-HSE-SEC-31 §8-§10 |
| **Agent Risques** | Identification et structuration des risques d'intervention | Liste officielle Section A |
| **Assistant AT** | Chat conversationnel s'appuyant sur la base de connaissances RAG | Base documentaire OCP |

---

## 4. Endpoints API

### `POST /api/ai/analyze-at`
Analyse complète d'une AT par le Crew multi-agents.
- **Entrée** : `{ "atId": "...", "description": "...", "installation": "...", "visiteFaite": true, ... }`
- **Sortie** : `{ "summary": "...", "identifiedRisks": [...], "recommendedMeasures": [...], "sources": [...], ... }`

### `POST /api/ai/chat`
Assistant conversationnel RAG.
- **Entrée** : `{ "message": "...", "atContext": { "atId": "...", "description": "..." } }`
- **Sortie** : `{ "answer": "...", "sources": [...], "suggestedQuestions": [...] }`

### `GET /health`
Vérification de l'état du microservice et du modèle actif.

---

## 5. Configuration (`.env`)

Créer un fichier `.env` à partir de `.env.example` :

```bash
AI_SERVICE_PORT=8000
AI_SERVICE_HOST=0.0.0.0

# Fournisseur LLM : GEMINI, OPENAI, OLLAMA, MOCK
LLM_PROVIDER=GEMINI
LLM_MODEL=gemini-2.5-flash
LLM_TEMPERATURE=0.2

# Clé API
GOOGLE_API_KEY=votre_cle_gemini_ici
GEMINI_API_KEY=votre_cle_gemini_ici

# RAG
RAG_ENABLED=true
TOP_K_DOCUMENTS=4

# Télémétrie
CREWAI_TELEMETRY_OPT_OUT=true
OTEL_SDK_DISABLED=true
```

---

## 6. Installation & Démarrage

### Prérequis
- Python 3.10+ (ou environnement virtuel existant `.venv`)

### Installation des dépendances
```bash
pip install -r requirements.txt
```

### Lancement du microservice
```bash
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

---

## 7. Exécution des Tests

```bash
python -m unittest discover -s tests
```
