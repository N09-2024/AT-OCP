# Changelog

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr-FR/1.0.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-07-11

### Ajouté
- **Entités JPA**:
  - `AutorisationTravail` - Gestion des autorisations de travail
  - `Utilisateur` - Gestion des utilisateurs
  - `Role` / `Permission` - Système de rôles
  - `Visa` - Workflow de validation
  - `WorkflowAT` - Configuration du workflow
  - `Document` - Gestion documentaire
  - `Reception` - Réception des travaux
  - `Zone` / `Site` / `Batiment` - Gestion spatiale

- **API REST**:
  - Authentification JWT (`/api/auth/*`)
  - Gestion AT (`/api/autorisations-travail/*`, `/api/at/*`)
  - Gestion utilisateurs (`/api/utilisateurs/*`)
  - Workflow (`/api/workflows/*`)
  - Documents (`/api/documents/*`)
  - Zones (`/api/zones/*`)

- **Sécurité**:
  - Authentification JWT
  - Gestion des rôles et permissions
  - Protection CORS/CSRF
  - Rate limiting

- **Base de données**:
  - Migrations Flyway (V1-V12)
  - Optimisation avec EntityGraph
  - Requêtes optimisées avec @BatchSize

- **Infrastructure**:
  - Configuration Docker complète
  - Docker Compose (dev, staging, prod)
  - GitHub Actions CI/CD
  - Makefile pour commandes simplifiées

- **Tests**:
  - Tests unitaires avec JUnit 5
  - Tests MockMvc pour controllers
  - Couverture JaCoCo (~50%)

### Corrigé
- Problèmes N+1 sur les relations JPA (EAGER → LAZY)
- StackOverflow sur equals/hashCode (@EqualsAndHashCode.Include)
- Configuration Security (Constructor Injection)
- Méthode `obtenirEtatSuivant()` dans WorkflowATServiceImpl

### Modifié
- Entités mises à jour avec FetchType.LAZY
- Validation des DTOs avec Jakarta Validation
- Migrations Flyway pour compatibilité PostgreSQL

## [0.1.0] - 2024-06-01

### Ajouté
- Structure initiale du projet Spring Boot
- Configuration de base PostgreSQL
- Modèles de domaine initiaux

---

## Notes de Version Futures

### [1.1.0] - Prévu
- [ ] Module frontend React
- [ ] Notifications email
- [ ] Tableau de bord analytique
- [ ] API REST étendue

### [1.2.0] - Prévu
- [ ] Intégration calendrier
- [ ] Rappels automatiques
- [ ] Export avancé PDF/Excel

### [2.0.0] - Prévu
- [ ] Micro-services
- [ ] Cache Redis
- [ ] Recherche Elasticsearch
