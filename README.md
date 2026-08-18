# OCP AT System — Système de Gestion des Autorisations de Travail

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=springboot)
![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk)
![Python](https://img.shields.io/badge/Python-3.11-3776AB?logo=python)
![FastAPI](https://img.shields.io/badge/FastAPI-0.115.0-009688?logo=fastapi)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6?logo=typescript)
![Vite](https://img.shields.io/badge/Vite-8-646CFF?logo=vite)
![MUI](https://img.shields.io/badge/MUI-v9-007FFF?logo=mui)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)

> Plateforme industrielle de numérisation du cycle de vie complet des **Autorisations de Travail (AT)** et **Permis Associés**, conforme au **Standard OCP S-HSE-SEC-31 v1.0**.

---

## Table des Matières

- [Aperçu & Fonctionnalités](#aperçu--fonctionnalités)
- [Stack Technique](#stack-technique)
- [Architecture du Projet](#architecture-du-projet)
- [Base de Données & Migrations](#base-de-données--migrations)
- [Standard S-HSE-SEC-31 & Rôles](#standard-s-hse-sec-31--rôles)
- [Prérequis](#prérequis)
- [Démarrage Local](#démarrage-local)
- [Démarrage Docker Compose](#démarrage-docker-compose)
- [Variables d'Environnement](#variables-denvironnement)
- [API REST](#api-rest)
- [Comptes de Démonstration](#comptes-de-démonstration)

---

## Aperçu & Fonctionnalités

**OCP AT System** numérise de bout en bout la gestion des autorisations de travail sur les sites OCP :

- Cycle complet S-HSE-SEC-31 : DI/OT/BT ? Classification ? Visite préalable ? 6 visas ? Travaux ? Réception ? Archivage PDF
- Chaîne séquentielle de 6 visas avec signatures manuscrites horodatées (SHA-256, IP, User-Agent)
- Assistant IA (CrewAI + Gemini 2.5 Flash) : Analyse et validation de conformité des permis
- Notifications temps réel par rôle (polling 30s) avec navigation directe vers l'AT
- Génération PDF officielle F-HSE-SEC-31-04 avec QR Code de vérification
- RBAC multi-rôles : CEEP, CEEE, HCEP, HCEE, HMEP, HMEE, ADMIN, RESPONSABLE_EXTERIEUR
- Tableaux de bord personnalisés par rôle avec statistiques HSE

---

## Stack Technique

### Backend — Spring Boot 3.3.0 / Java 17

| Composant | Technologie | Version |
|---|---|---|
| Langage | Java (OpenJDK / Temurin) | **17** |
| Framework | Spring Boot (Web, JPA, Security, Validation, AOP) | **3.3.0** |
| Sécurité | Spring Security 6 + JWT (jjwt) | **0.12.5** |
| Base de données | PostgreSQL | **16** |
| Migrations | Flyway (35 scripts V1 ? V35) | intégré |
| Mapping | MapStruct + Lombok | 1.5.5 / 1.18.32 |
| Documentation | SpringDoc OpenAPI 3 / Swagger UI | **2.5.0** |
| Artefact | `com.ocp:at-backend:0.0.1-SNAPSHOT` | |

### Microservice IA — FastAPI / CrewAI / Gemini

| Composant | Technologie | Version |
|---|---|---|
| Framework | FastAPI + Uvicorn | **0.115.0 / 0.30.6** |
| Validation | Pydantic | **2.9.2** |
| Orchestration IA | CrewAI | **0.70.1** |
| LLM | LangChain + Google Gemini | langchain 0.2.x |
| Modèle IA | Gemini 2.5 Flash | `gemini-2.5-flash` |

### Frontend — React 19 / TypeScript 6 / Vite 8

| Composant | Technologie | Version |
|---|---|---|
| Framework | React | **19.x** |
| Langage | TypeScript | **~6.0.2** |
| Build Tool | Vite | **8.x** |
| UI Library | Material UI (MUI) | **v9.2.0** |
| Navigation | React Router DOM | **v7.18** |
| État Global | Zustand | **5.0.14** |
| Formulaires | React Hook Form + Zod | 7.81 / 4.x |
| Signatures | react-signature-canvas | 1.1.0-alpha.2 |
| Graphiques | Recharts | **3.9.2** |
| Animations | Framer Motion | **12.x** |
| HTTP | Axios + TanStack React Query | 1.18 / 5.x |

---

## Architecture du Projet

```
ocp-at-system/
+-- backend/                              # Spring Boot Java 17
¦   +-- src/main/java/com/ocp/at/
¦   ¦   +-- controller/                   # 37 contrôleurs REST
¦   ¦   +-- service/impl/                 # Logique métier (AT, Visa, Notification, PDF...)
¦   ¦   +-- repository/                   # Spring Data JPA
¦   ¦   +-- entity/                       # Entités JPA
¦   ¦   +-- security/                     # JWT, RBAC, ATContextService
¦   ¦   +-- dto/                          # DTOs request/response
¦   ¦   +-- mapper/                       # MapStruct
¦   +-- src/main/resources/
¦   ¦   +-- application.yml               # Config principale
¦   ¦   +-- application-dev.yml           # Config dev (DB locale)
¦   ¦   +-- application-prod.yml          # Config production
¦   ¦   +-- db/migration/                 # 35 scripts Flyway (V1-V35)
¦   +-- pom.xml
¦
+-- ai-service/                           # Python / FastAPI / CrewAI / Gemini
¦   +-- app/
¦   ¦   +-- main.py
¦   ¦   +-- routes/                       # Endpoints IA
¦   ¦   +-- services/                     # Agents CrewAI
¦   +-- requirements.txt
¦
+-- frontend/                             # React 19 + TypeScript + Vite 8
¦   +-- src/
¦   ¦   +-- components/
¦   ¦   ¦   +-- common/                   # FormulaireOCPInteractive, SignaturePad...
¦   ¦   ¦   +-- layout/                   # Topbar (polling notifs), Sidebar
¦   ¦   +-- modules/
¦   ¦   ¦   +-- auth/                     # LoginPage, RegisterPage
¦   ¦   ¦   +-- autorisations/            # Liste, Détail, Workflow
¦   ¦   ¦   +-- visas/                    # ValidationOCPPage (HC & HM)
¦   ¦   ¦   +-- profile/                  # Profil & Notifications
¦   ¦   ¦   +-- dashboard/                # Tableaux de bord par rôle
¦   ¦   ¦   +-- administration/           # Utilisateurs, Rôles, Référentiels
¦   ¦   +-- services/                     # Clients Axios
¦   ¦   +-- store/                        # Zustand AuthStore
¦   +-- public/
¦   ¦   +-- OCP_Group.jpg                 # Logo OCP officiel
¦   +-- package.json
¦
+-- docker-compose.yml                    # 4 services : postgres, backend, ai-service, nginx
+-- README.md
```

---

## Base de Données & Migrations

PostgreSQL 16 avec 35 scripts Flyway versionnés :

| Scripts | Contenu |
|---|---|
| V1 ? V2 | Schéma initial, sécurité, utilisateurs, rôles, permissions |
| V3 ? V6 | Documents sources, Visites préalables, Analyses de risques |
| V7 ? V11 | AT, Workflow, Permis, Réception travaux, Archivage |
| V12 ? V18 | Correctifs : Flyway, inscriptions, rôles CE/HM/HC |
| V19 ? V26 | Formulaire officiel OCP, Workflow standard, Habilitations |
| V27 ? V35 | Correctifs : Visas, Réception, Permis documents, Hiérarchie |

**Configuration développement** (`application-dev.yml`) :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/at_ocp_db
    username: at_ocp_user
    password: at_ocp_secret
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true

ocp:
  ai:
    provider: CREW_AI       # MOCK | LANG_CHAIN | CREW_AI
    fastapi-url: http://localhost:8000
```

---

## Standard S-HSE-SEC-31 & Rôles

### Rôles Métiers

| Rôle | Désignation | Responsabilités |
|---|---|---|
| **CEEP** | Chef d'Équipe Émetteur / Propriétaire | Rédaction AT, Visite préalable, Visa Étape 1, Réception conjointe |
| **CEEE** | Chef d'Équipe Exécutant | Accusé réception, Visa Étape 2, Démarrage, Fin des travaux |
| **HCEP** | Hors Cadre Émetteur / Propriétaire | Classification, Visa Étape 3, Garant sécurité propriétaire |
| **HCEE** | Hors Cadre Exécutant | Visa Étape 4, Garant sécurité exécutante, Archivage |
| **HMEP** | Haute Maîtrise Émettrice | Visa de garantie Étape 5 |
| **HMEE** | Haute Maîtrise Exécutante | Visa de garantie Étape 6 |
| **ADMIN** | Administrateur Système | Gestion users, rôles, zones, référentiels, audit |
| **RESPONSABLE_EXTERIEUR** | Responsable Entreprise Extérieure | Suivi des AT de son entreprise |

### Chaîne des 6 Visas

```
CEEP (1) ? CEEE (2) ? HCEP (3) ? HCEE (4) ? HMEP (5) ? HMEE (6)
```

Chaque visa est horodaté, signé manuscritement et hashé en SHA-256.

### Workflow — 9 États

```
BROUILLON ? VISITE_REALISEE ? AT_REDIGEE (6 visas)
? INTERVENTION_EN_COURS ? AT_RECONDUITE
? FIN_TRAVAUX_DECLAREE ? TRAVAUX_RECEPTIONES ? ARCHIVEE
```

---

## Prérequis

| Outil | Version | Usage |
|---|---|---|
| Java JDK | **17** (OpenJDK / Temurin) | Backend |
| Maven | **3.9+** | Build backend |
| Node.js | **20+** avec npm | Frontend |
| Python | **3.11** | Microservice IA |
| PostgreSQL | **16** | Base de données |
| Docker | Latest | Déploiement complet (optionnel) |

---

## Démarrage Local

### Étape 1 — Base de Données

```bash
docker run --name at-ocp-postgres \
  -e POSTGRES_DB=at_ocp_db \
  -e POSTGRES_USER=at_ocp_user \
  -e POSTGRES_PASSWORD=at_ocp_secret \
  -p 5432:5432 \
  -d postgres:16-alpine
```

### Étape 2 — Backend Spring Boot

```bash
cd backend

# Windows PowerShell
$env:JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"

mvn spring-boot:run
```

| URL | Description |
|---|---|
| `http://localhost:8080/api` | API REST |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/actuator/health` | Health check |

### Étape 3 — Frontend

```bash
cd frontend
npm install
npm run dev
# Application : http://localhost:5173
```

### Étape 4 — Microservice IA (optionnel)

```bash
cd ai-service

# Windows
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt

$env:GOOGLE_API_KEY="votre-clé-api-google"
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
# Service IA : http://localhost:8000
```

> Sans clé API Google, définir `OCP_AI_PROVIDER=MOCK` dans `application-dev.yml`.

---

## Démarrage Docker Compose

4 services orchestrés : `postgres`, `backend`, `ai-service`, `nginx`

```bash
# Créer le fichier d'environnement
cp .env.example .env   # puis remplir les valeurs

# Lancer
docker-compose up -d

# Logs
docker-compose logs -f backend
docker-compose logs -f ai-service

# Arrêter
docker-compose down
```

---

## Variables d'Environnement

Fichier `.env` à créer à la racine :

```env
# Base de Données
DB_PASSWORD=at_ocp_secret_password

# JWT
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Google Gemini
GOOGLE_API_KEY=AIza...votre_cle
GEMINI_MODEL=gemini-2.5-flash

# Fournisseur IA : MOCK | LANG_CHAIN | CREW_AI
OCP_AI_PROVIDER=CREW_AI

# Spring
SPRING_PROFILES=prod
```

---

## API REST

### Authentification

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Connexion + JWT & refresh token |
| `POST` | `/api/auth/refresh-token` | Renouvellement du JWT |
| `POST` | `/api/auth/logout` | Révocation du refresh token |
| `GET` | `/api/auth/me` | Profil & permissions utilisateur |
| `POST` | `/api/auth/register` | Inscription (validation admin requise) |

### Autorisations de Travail

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/api/autorisations-travail` | Liste paginée (`search`, `statut`, `page`, `size`) |
| `POST` | `/api/autorisations-travail` | Création depuis un document source |
| `GET` | `/api/autorisations-travail/{id}` | Détail complet |
| `PUT` | `/api/autorisations-travail/{id}/auto-save` | Sauvegarde auto des champs |
| `POST` | `/api/autorisations-travail/{id}/soumettre` | Soumission + validation IA permis |
| `POST` | `/api/autorisations-travail/{id}/accuser-reception-ceee` | Accusé réception CEEE |
| `POST` | `/api/autorisations-travail/{id}/demarrer` | Démarrage des travaux |
| `POST` | `/api/autorisations-travail/{id}/reconduire` | Reconduction de poste |
| `POST` | `/api/autorisations-travail/{id}/declarer-fin` | Fin d'intervention |
| `POST` | `/api/autorisations-travail/{id}/receptionner` | Réception conjointe |
| `POST` | `/api/autorisations-travail/{id}/annuler` | Annulation |

### Visas & Signatures

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/visas/create-and-sign` | Signature manuscrite horodatée SHA-256 |
| `GET` | `/api/visas/autorisation/{atId}` | Historique chronologique des visas |

### Notifications

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/api/notifications` | Liste paginée |
| `GET` | `/api/notifications/count-unread` | Compteur non lues (polling 30s) |
| `PUT` | `/api/notifications/{id}/read` | Marquer comme lue |
| `PUT` | `/api/notifications/read-all` | Tout marquer comme lu |

### Documents Sources

| Méthode | Endpoint | Description |
|---|---|---|
| `GET/POST` | `/api/demandes-intervention` | Demandes d'Intervention (DI) |
| `GET/POST` | `/api/ordres-travail` | Ordres de Travail (OT) |
| `GET/POST` | `/api/bons-travail` | Bons de Travail (BT) |

### Permis & PDF

| Méthode | Endpoint | Description |
|---|---|---|
| `GET/POST` | `/api/permis` | Gestion des permis |
| `POST` | `/api/permis-documents/upload` | Upload + analyse OCR |
| `GET` | `/api/pdf/at/{id}` | Formulaire PDF officiel certifié |

### Administration & Référentiels

| Méthode | Endpoint | Description |
|---|---|---|
| `GET/POST/PUT/DELETE` | `/api/utilisateurs` | CRUD utilisateurs |
| `PUT` | `/api/utilisateurs/{id}/approve` | Validation inscription |
| `GET/POST/PUT/DELETE` | `/api/roles` | Rôles & permissions |
| `GET/POST/PUT/DELETE` | `/api/zones` | Zones propriétaire/exécutante |
| `GET/POST/PUT/DELETE` | `/api/services` | Services OCP |
| `GET/POST/PUT/DELETE` | `/api/installations` | Installations |
| `GET` | `/api/dashboard` | KPI & statistiques par rôle |
| `GET` | `/api/audit` | Journal d'audit |

---

## Comptes de Démonstration

Créés automatiquement par les scripts Flyway (V23 seed data) :

| Rôle | Email | Mot de passe | Périmètre |
|---|---|---|---|
| **Admin** | `admin@ocp.ma` | `Admin123!` | Accès complet système |
| **CEEP** | `ceep@ocp.ma` | `Password123!` | Rédaction, Visite, Visa 1, Clôture |
| **CEEE** | `ceee@ocp.ma` | `Password123!` | Accusé, Visa 2, Travaux, Fin |
| **HCEP** | `hcep@ocp.ma` | `Password123!` | Classification, Visa 3 |
| **HCEE** | `hcee@ocp.ma` | `Password123!` | Visa 4, Archivage |
| **HMEP** | `hmep@ocp.ma` | `Password123!` | Visa garantie 5 |
| **HMEE** | `hmee@ocp.ma` | `Password123!` | Visa garantie 6 |

---

<p align="center">
  <strong>OCP Group · Direction Sécurité & Santé au Travail (HSE)</strong><br/>
  Standard <em>S-HSE-SEC-31</em> · Formulaire <em>F-HSE-SEC-31-04</em><br/>
  © 2026 OCP Group — Système AT Intelligente
</p>
