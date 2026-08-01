# OCP AT System — Système de Gestion des Autorisations de Travail

![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript)
![MUI](https://img.shields.io/badge/MUI-v9-007FFF?logo=mui)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-yellow)

> Application industrielle complète de gestion des Autorisations de Travail (AT) développée pour **OCP Group**.  
> Backend Spring Boot **✅ terminé** — Frontend React **🚧 en développement actif**.

---

## 📋 Table des Matières

- [Description](#-description)
- [Stack Technique](#-stack-technique)
- [Architecture du Projet](#-architecture-du-projet)
- [Prérequis](#-prérequis)
- [Installation & Démarrage](#-installation--démarrage)
  - [Backend (Spring Boot)](#backend-spring-boot)
  - [Frontend (React)](#frontend-react)
  - [Avec Docker](#avec-docker)
- [API REST](#-api-rest)
- [Modules Implémentés](#-modules-implémentés)
- [Contribution](#-contribution)
- [Licence](#-licence)

---

## 📝 Description

**OCP AT System** est un système full-stack de gestion du cycle de vie complet des Autorisations de Travail :

- 📄 Création et gestion des **Demandes d'Intervention (DI / OT / BT)**
- 🔄 **Workflow de validation multi-niveaux** (Brouillon → Soumise → Analyse → Autorisation → En cours → Réception → Archivage)
- ✍️ **Signatures manuscrites numériques** (via React Signature Canvas)
- 🛡️ Gestion des **permis** (feu, fouille, espace confiné, travail en hauteur…)
- 👁️ **Visites préalables** de site avec géolocalisation et photos
- ⚠️ **Analyse des risques** (risques, mesures, EPI, moyens d'accès)
- ✅ **Validation & Visas** multi-rôles avec audit trail
- 👥 **Rôles métiers & Sécurité** conformes au **Standard OCP S-HSE-SEC-31 v1.0** (CEEP, CEEE, HCEP, HCEE, HMEP, HMEE, ADMIN, RESPONSABLE_ENTREPRISE)
- 📍 **Résolution contextuelle P/E** (Propriétaire / Exécutant selon le territoire de l'intervention)
- 📦 **Archivage PDF** avec QR Code et recherche avancée
- 📊 **Tableaux de bord** adaptés par rôle
- 🔔 **Notifications** temps réel

---

## 🛠 Stack Technique

### Backend

| Composant | Technologie |
|-----------|-------------|
| Langage | Java 17 |
| Framework | Spring Boot 3 |
| Sécurité | Spring Security + JWT |
| ORM | Spring Data JPA / Hibernate |
| Base de données | PostgreSQL 16 |
| Migration DB | Flyway |
| Mapping | MapStruct |
| Documentation API | Swagger / OpenAPI 3 |
| Architecture | REST, RBAC |
| Tests | JUnit 5, Mockito |

### Frontend

| Composant | Technologie |
|-----------|-------------|
| Framework | React 19 + TypeScript |
| Bundler | Vite |
| UI Library | Material UI (MUI v9) |
| Routage | React Router v7 |
| Requêtes HTTP | Axios |
| Data Fetching | TanStack React Query |
| Formulaires | React Hook Form + Zod |
| État global | Zustand |
| Graphiques | Recharts |
| Signatures | React Signature Canvas |
| Visualisation PDF | React PDF Viewer |
| Animations | Framer Motion |
| Icônes | Heroicons |

---

## 🏗 Architecture du Projet

```
ocp-at-system/
├── backend/                      # Spring Boot 3 (Java 17)
│   └── src/main/java/com/ocp/at/
│       ├── controller/           # REST Controllers
│       ├── service/              # Logique métier
│       ├── repository/           # Spring Data JPA
│       ├── entity/               # Entités JPA
│       ├── dto/                  # Data Transfer Objects
│       ├── mapper/               # MapStruct
│       └── security/             # JWT, RBAC
│
├── frontend/                     # React 19 + TypeScript
│   └── src/
│       ├── app/                  # Providers (ThemeProvider, QueryClient…)
│       ├── assets/               # Images, logos
│       ├── components/           # Composants génériques réutilisables
│       │   ├── layout/           #   Sidebar, Topbar
│       │   └── dashboard/        #   StatCard, AtTable, Charts…
│       ├── constants/            # Constantes applicatives
│       ├── hooks/                # Hooks personnalisés
│       ├── layouts/              # MainLayout, AuthLayout
│       ├── modules/              # Modules métier (1 dossier = 1 domaine)
│       │   ├── auth/             #   Connexion, profil
│       │   ├── dashboard/        #   Tableaux de bord par rôle
│       │   ├── administration/   #   Utilisateurs, rôles, permissions
│       │   ├── referentiels/     #   Zones, installations, équipements…
│       │   ├── documents/        #   DI, OT, BT
│       │   ├── visites/          #   Visites préalables
│       │   ├── analyses/         #   Analyse des risques
│       │   ├── autorisations/    #   AT (workflow complet)
│       │   ├── permis/           #   Gestion des permis
│       │   ├── visas/            #   Validations & signatures
│       │   ├── receptions/       #   Réceptions des travaux
│       │   ├── archives/         #   Archivage & rapports
│       │   └── rapports/         #   Statistiques & exports
│       ├── routes/               # Routage & routes protégées (RBAC)
│       ├── services/             # Clients API (AuthService, ATService…)
│       ├── store/                # Zustand (authStore, uiStore…)
│       ├── theme/                # Thème MUI (couleurs OCP, typographie)
│       ├── types/                # Interfaces TypeScript
│       └── utils/                # Fonctions utilitaires
│
├── docs/                         # Documentation API
├── docker-compose.yml
└── README.md
```

---

## 📦 Prérequis

| Outil | Version |
|-------|---------|
| Java | 17+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| npm | 10+ |
| PostgreSQL | 16+ |
| Docker (optionnel) | 20.10+ |

---

## 🚀 Installation & Démarrage

### Backend (Spring Boot)

```bash
# 1. Créer la base de données PostgreSQL
psql -U postgres -c "CREATE DATABASE at_ocp_db;"
psql -U postgres -c "CREATE USER at_ocp_user WITH PASSWORD 'your_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE at_ocp_db TO at_ocp_user;"

# 2. Configurer les variables d'environnement (ou application.yml)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=at_ocp_db
export DB_USERNAME=at_ocp_user
export DB_PASSWORD=your_password
export JWT_SECRET=your-very-secret-key

# 3. Lancer le backend
cd backend
mvn spring-boot:run
```

> API disponible sur `http://localhost:8080`  
> Swagger UI : `http://localhost:8080/swagger-ui.html`

---

### Frontend (React)

```bash
# 1. Accéder au dossier frontend
cd frontend

# 2. Installer les dépendances
npm install

# 3. Lancer le serveur de développement
npm run dev
```

> Application disponible sur `http://localhost:5173`

```bash
# Build de production
npm run build
```

---

### Avec Docker

```bash
# Copier et configurer les variables d'environnement
cp .env.example .env
# Éditer .env avec vos valeurs

# Démarrer tous les services (Backend + DB)
docker-compose up -d

# Voir les logs
docker-compose logs -f backend

# Arrêter
docker-compose down
```

---

## 📡 API REST

### Endpoints principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| `POST` | `/api/auth/login` | Authentification (JWT) |
| `GET` | `/api/auth/me` | Profil utilisateur connecté |
| `GET` | `/api/autorisations-travail` | Liste paginée des AT |
| `POST` | `/api/autorisations-travail` | Créer une AT |
| `GET` | `/api/autorisations-travail/{id}` | Détails d'une AT |
| `PUT` | `/api/autorisations-travail/{id}` | Modifier une AT |
| `POST` | `/api/at/{id}/submit` | Soumettre une AT |
| `POST` | `/api/at/{id}/validate` | Valider une AT |
| `POST` | `/api/at/{id}/reject` | Rejeter une AT |
| `GET` | `/api/statistiques/dashboard` | KPI du tableau de bord |

### Authentification

```bash
# Connexion
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@ocp.ma","password":"password123"}'

# Utiliser le token retourné
curl http://localhost:8080/api/autorisations-travail \
  -H "Authorization: Bearer <token>"
```

---

## 📦 Modules Implémentés

| # | Module | Backend | Frontend |
|---|--------|---------|----------|
| 1 | Authentification & Sécurité (JWT, RBAC) | ✅ | ✅ |
| 2 | Référentiels (Zones, Installations, EPI…) | ✅ | 🚧 |
| 3 | Documents d'Intervention (DI / OT / BT) | ✅ | 🚧 |
| 4 | Visites Préalables | ✅ | 🚧 |
| 5 | Analyse des Risques | ✅ | 🚧 |
| 6 | Autorisations de Travail (Workflow) | ✅ | 🚧 |
| 7 | Gestion des Permis (PDF, IA) | ✅ | 🚧 |
| 8 | Validations & Visas (Signature manuscrite) | ✅ | 🚧 |
| 9 | Réceptions des Travaux | ✅ | 🚧 |
| 10 | Archives & Rapports (QR Code, PDF) | ✅ | 🚧 |

> ✅ Terminé &nbsp;|&nbsp; 🚧 En développement &nbsp;|&nbsp; ⬜ À venir

---

## 👥 Rôles & Workflow — Standard OCP S-HSE-SEC-31 v1.0

Le système implémente les 8 rôles opérationnels et les 9 étapes du workflow du **Standard OCP S-HSE-SEC-31 v1.0** :

### Rôles Opérationnels

| Rôle | Nom complet | Définition & Rôle dans le workflow |
|------|-------------|-----------------------------------|
| **CEEP** | Chef d'Équipe Entité Propriétaire | Opérationnel terrain P. Rédige l'AT (§8.3), réalise la visite (§8.2), réceptionne (§8.5). |
| **CEEE** | Chef d'Équipe Entité Exécutante | Opérationnel terrain E. Démarre les travaux (§4), déclare la fin des travaux (§8.5). |
| **HCEP** | Hors Cadre Entité Propriétaire | Cadre hiérarchique P. Classifie Niveau 1/2 (Étape 0), garant archivage (§8.6), habilitations (§9). |
| **HCEE** | Hors Cadre Entité Exécutante | Cadre hiérarchique E. Garant de la visite (§8.2) et de l'AT (§8.3), exécute l'archivage (§8.6). |
| **HMEP** | Haute Maîtrise Entité Propriétaire | Maîtrise P. Garant de la visite chantier (§8.2) et du démarrage d'intervention (§4). |
| **HMEE** | Haute Maîtrise Entité Exécutante | Maîtrise E. Position fail-closed en lecture seule. |
| **ADMIN** | Administrateur Système | Gestion globale du système et des référentiels. |
| **RESPONSABLE_ENTREPRISE** | Responsable Entreprise Externe | Sous-traitant externe. Gestion des Bons de Travaux (BT) et des permis associés. |

### Workflow des 9 Étapes (`statutWorkflow`)

```
[0. CLASSIFICATION_EFFECTUEE] (HCEP)
             ↓
[1. DEMANDE_CREEE]            (CEEP - DI/OT/BT)
             ↓
[2. VISITE_REALISEE]          (CEEP E, HCEE/HMEP G)
             ↓
[3. AT_REDIGEE]               (CEEP E, HCEE G, CEEE P)
             ↓
[4. INTERVENTION_EN_COURS]    (CEEE E)
             ↓
[5b. AT_RECONDUITE]           (Si dépassement poste ; retour Visite si > 24h)
             ↓
[6. FIN_TRAVAUX_DECLAREE]     (CEEE E)
             ↓
[7. TRAVAUX_RECEPTIONES]      (CEEP E, CEEE P)
             ↓
[8. ARCHIVEE]                 (HCEE E, HCEP G - min. 1 an)
```

> Pour plus de détails sur la procédure de migration et la matrice des permissions, consulter [MIGRATION_ROLES.md](MIGRATION_ROLES.md).

---

## 🎨 Design System

Le frontend respecte la **charte graphique OCP** :

| Token | Valeur |
|-------|--------|
| Primary | `#009A44` (OCP Green) |
| Dark Green | `#006B3C` |
| Light Green | `#EAF7EF` |
| Background | `#F7F9FB` |
| Warning | `#F59E0B` |
| Danger | `#DC2626` |
| Typographie | Inter (Google Fonts) |
| Border Radius | 16px |

---

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/ma-fonctionnalite`)
3. Commit (`git commit -m 'feat: ajout de ma fonctionnalité'`)
4. Push (`git push origin feature/ma-fonctionnalite`)
5. Ouvrir une **Pull Request**

### Conventions

- **Backend** : Google Java Style Guide, commits sémantiques
- **Frontend** : ESLint + Prettier, TypeScript strict mode
- Ajouter des tests pour toute nouvelle fonctionnalité
- Documenter les nouveaux endpoints dans `docs/API.md`

---

## 📚 Documentation

- [docs/API.md](docs/API.md) — Documentation des endpoints REST
- [DOCKER.md](DOCKER.md) — Guide de déploiement Docker
- [AUDIT_REPORT.md](AUDIT_REPORT.md) — Rapport d'audit technique
- [CHANGELOG.md](CHANGELOG.md) — Historique des versions
- Swagger UI : `http://localhost:8080/swagger-ui.html`

---

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 👥 Équipe

- **Maintainers** : OCP Dev Team
- **Contact** : dev@ocp.ma

---

<p align="center">
  Développé avec ❤️ pour <strong>OCP Group</strong>
  <br/>
  <img src="https://img.shields.io/badge/OCP-Successful%20Together-009A44?style=for-the-badge" alt="OCP Successful Together"/>
</p>
