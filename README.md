# AT-OCP - Autorisation de Travail

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

Système de gestion des Autorisations de Travail pour OCP.

## 📋 Table des Matières

- [Description](#-description)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Démarrage Rapide](#-démarrage-rapide)
- [API REST](#-api-rest)
- [Déploiement](#-déploiement)
- [CI/CD](#-cicd)
- [Contribution](#-contribution)
- [Licence](#-licence)

## 📝 Description

AT-OCP est une application Spring Boot permettant de gérer le cycle de vie complet des autorisations de travail :
- Création et gestion des demandes d'autorisation
- Workflow de validation multi-niveaux
- Gestion des visas et approbations
- Suivi des travaux et réceptions
- Génération de rapports PDF

## ✨ Fonctionnalités

### Gestion des Autorisations de Travail
- ✅ Création, modification, suppression d'AT
- ✅ Workflow d'approbation configurable
- ✅ Numérotation automatique
- ✅ Verrouillage optimiste pour édition concurrente
- ✅ Sauvegarde automatique (auto-save)

### Workflow
- ✅ États : Brouillon → Soumise → Validée → En Cours → Clôturée
- ✅ Validation multi-niveaux avec visas
- ✅ Rejet avec motif obligatoire
- ✅ Règles métier configurables

### Sécurité
- ✅ Authentification JWT
- ✅ Gestion des rôles et permissions
- ✅ Audit trail complet
- ✅ Protection contre les attaques courantes

### Intégrations
- ✅ Génération de PDF
- ✅ Export Excel
- ✅ API REST complète
- ✅ Swagger UI (documentation)

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (à venir)                      │
└──────────────────────────┬────────────────────────────────────┘
                           │ REST API
┌──────────────────────────▼────────────────────────────────────┐
│                      Spring Boot 3.2                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│  │ Controllers │  │  Services   │  │      Security        │   │
│  └─────────────┘  └─────────────┘  └─────────────────────┘   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐   │
│  │   Mappers   │  │  Repositor  │  │     Validators      │   │
│  └─────────────┘  └─────────────┘  └─────────────────────┘   │
└──────────────────────────┬────────────────────────────────────┘
                           │
┌──────────────────────────▼────────────────────────────────────┐
│                    PostgreSQL 16                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │    AT    │  │  Visa    │  │ Utilisat │  │ Workflow │     │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │
└───────────────────────────────────────────────────────────────┘
```

### Stack Technique

| Composant | Technology |
|-----------|------------|
| Backend | Java 21, Spring Boot 3.2 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA, Hibernate |
| Migration | Flyway |
| Security | Spring Security, JWT |
| Testing | JUnit 5, Mockito, JaCoCo |
| API Docs | SpringDoc OpenAPI (Swagger) |
| PDF | iText, OpenPDF |
| Docker | Multi-stage Dockerfile |

## 📦 Prérequis

- **Java**: 21 ou supérieur
- **Maven**: 3.9+
- **PostgreSQL**: 16+
- **Docker**: 20.10+ (optionnel)
- **Docker Compose**: 2.0+ (optionnel)

## 🚀 Installation

### 1. Cloner le Repository

```bash
git clone https://github.com/OCP-N09-2024/AT-OCP.git
cd AT-OCP/AT-OCP
```

### 2. Configuration de la Base de Données

```bash
# Connexion à PostgreSQL
psql -U postgres -c "CREATE DATABASE at_ocp_db;"
psql -U postgres -c "CREATE USER at_ocp_user WITH PASSWORD 'your_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE at_ocp_db TO at_ocp_user;"
```

### 3. Configuration de l'Application

```bash
# Copier le fichier de configuration
cp backend/src/main/resources/application.yml.example backend/src/main/resources/application.yml

# Éditer les configurations
nano backend/src/main/resources/application.yml
```

### 4. Variables d'Environnement (optionnel)

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=at_ocp_db
export DB_USERNAME=at_ocp_user
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key
```

## 🏃 Démarrage Rapide

### Mode Développement

```bash
cd backend

# Compiler et exécuter
mvn spring-boot:run

# Ou compiler le JAR et exécuter
mvn clean package -DskipTests
java -jar target/at-backend-*.jar
```

L'application sera accessible sur : http://localhost:8080

### Avec Docker

```bash
# Configuration
cp .env.example .env
# Éditer .env avec vos valeurs

# Démarrer
make up

# Voir les logs
make logs-backend

# Arrêter
make down
```

### Avec Docker Compose Direct

```bash
docker-compose up -d
```

## 📡 API REST

### Points d'Accès Principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/login` | Authentification |
| GET | `/api/autorisations-travail` | Liste des AT |
| POST | `/api/autorisations-travail` | Créer une AT |
| GET | `/api/autorisations-travail/{id}` | Détails AT |
| PUT | `/api/autorisations-travail/{id}` | Modifier AT |
| POST | `/api/at/{id}/submit` | Soumettre AT |
| POST | `/api/at/{id}/validate` | Valider AT |
| POST | `/api/at/{id}/reject` | Rejeter AT |
| POST | `/api/at/{id}/close` | Clôturer AT |

### Documentation Swagger

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Authentification

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@ocp.ma","password":"password"}'

# Utiliser le token
curl -X GET http://localhost:8080/api/autorisations-travail \
  -H "Authorization: Bearer <token>"
```

## 🚢 Déploiement

### Docker

Voir [DOCKER.md](DOCKER.md) pour les instructions détaillées.

```bash
# Build image
docker build -t at-ocp-backend ./backend

# Run avec docker-compose
docker-compose up -d
```

### Production

1. Configurer les secrets GitHub (voir [CI/CD](#cicd))
2. Modifier `docker-compose.prod.yml` si nécessaire
3. Configurer le reverse proxy (Nginx)
4. Configurer SSL/TLS

## 🔄 CI/CD

### Workflows GitHub Actions

| Workflow | Déclencheur | Description |
|----------|-------------|-------------|
| `ci.yml` | push, PR | Build, Test, Security |
| `cd.yml` | push main/develop | Déploiement |
| `pr.yml` | PR | Validation complète |

### Configuration Requise

1. **Secrets GitHub**:
   - `DOCKER_USERNAME` / `DOCKER_PASSWORD`
   - `SONAR_TOKEN`
   - `SLACK_WEBHOOK_URL`
   - Clés SSH pour déploiement

2. **Environnements GitHub**:
   - `staging`
   - `production`

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/amazing-feature`)
3. Commit (`git commit -m 'feat: add amazing feature'`)
4. Push (`git push origin feature/amazing-feature`)
5. Ouvrir une Pull Request

### Standards de Code

- Respecter les conventions Java (Google Style)
- Ajouter des tests pour les nouvelles fonctionnalités
- Mettre à jour la documentation
- Utiliser des commits sémantiques

## 📚 Documentation Additionnelle

- [AUDIT_REPORT.md](AUDIT_REPORT.md) - Rapport d'audit technique
- [DOCKER.md](DOCKER.md) - Guide Docker
- [API Documentation](http://localhost:8080/swagger-ui.html) - Swagger UI

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Équipe

- **Maintainers**: OCP Dev Team
- **Contact**: dev@ocp.ma

---

<p align="center">
  Développé avec ❤️ par OCP
</p>
