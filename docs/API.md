# Documentation API AT-OCP

## Authentification

### POST /api/auth/login
Connexion utilisateur et obtention du token JWT.

**Request:**
```json
{
  "email": "user@ocp.ma",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### POST /api/auth/register
Inscription nouvel utilisateur.

**Request:**
```json
{
  "matricule": "EMP001",
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@ocp.ma",
  "motDePasse": "SecurePass123!",
  "telephone": "+212600000000"
}
```

### GET /api/auth/me
Obtenir les informations de l'utilisateur connecté.

**Headers:** `Authorization: Bearer <token>`

---

## Autorisations de Travail

### GET /api/autorisations-travail
Liste paginée des autorisations de travail.

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| page | int | Numéro de page (défaut: 0) |
| size | int | Taille page (défaut: 20) |
| search | string | Recherche textuelle |
| statut | string | Filtrer par statut |
| sort | string | Tri (ex: dateCreation,desc) |

**Response (200):**
```json
{
  "content": [
    {
      "id": "at-001",
      "numero": "AT-2026-000001",
      "objet": "Travaux de maintenance",
      "statut": "BROUILLON",
      "demandeur": {
        "id": "user-001",
        "nom": "Dupont",
        "prenom": "Jean"
      },
      "dateCreation": "2026-07-11T10:00:00Z"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0
}
```

### POST /api/autorisations-travail
Créer une nouvelle autorisation de travail.

**Request:**
```json
{
  "objet": "Travaux de maintenance électrique",
  "description": "Intervention sur le tableau électrique principal",
  "dateDebut": "2026-07-15",
  "dateFin": "2026-07-16",
  "lieuTravaux": "Bâtiment A, Étage 2",
  "typeTravaux": "ELECTRIQUE",
  "documentId": "doc-001",
  "documentType": "DI"
}
```

### GET /api/autorisations-travail/{id}
Obtenir les détails d'une AT.

### PUT /api/autorisations-travail/{id}
Mettre à jour une AT (brouillon uniquement).

### DELETE /api/autorisations-travail/{id}
Supprimer une AT (brouillon uniquement).

### POST /api/at/{id}/submit
Soumettre une AT pour validation.

**Response (200):**
```json
{
  "id": "at-001",
  "statut": "SOUMISE",
  "etatVerrou": "LIBRE"
}
```

### POST /api/at/{id}/validate
Valider une AT.

### POST /api/at/{id}/reject
Rejeter une AT.

**Request:**
```json
{
  "motifRejet": "Documents incomplets"
}
```

### POST /api/at/{id}/close
Clôturer une AT.

---

## Workflow

### GET /api/workflows
Liste des configurations de workflow.

### GET /api/workflows/transitions
Obtenir les transitions disponibles.

**Query Parameters:**
- `etatActuel` - État actuel de l'AT

---

## Utilisateurs

### GET /api/utilisateurs
Liste des utilisateurs.

### GET /api/utilisateurs/{id}
Détails d'un utilisateur.

### POST /api/utilisateurs
Créer un utilisateur.

### PUT /api/utilisateurs/{id}
Modifier un utilisateur.

### DELETE /api/utilisateurs/{id}
Supprimer un utilisateur.

### POST /api/utilisateurs/{id}/activate
Activer un utilisateur.

### POST /api/utilisateurs/{id}/deactivate
Désactiver un utilisateur.

---

## Rôles

### GET /api/roles
Liste des rôles.

### POST /api/roles
Créer un rôle.

### POST /api/utilisateurs/{id}/roles/{roleId}
Affecter un rôle à un utilisateur.

### DELETE /api/utilisateurs/{id}/roles/{roleId}
Retirer un rôle à un utilisateur.

---

## Documents

### POST /api/documents/{type}/{id}/creer-at
Créer une AT depuis un document (DI/DR).

**Path Parameters:**
- `type` - Type de document (DI, DR)
- `id` - ID du document

---

## Zones

### GET /api/zones
Liste des zones.

### POST /api/zones
Créer une zone.

### GET /api/zones/{id}
Détails d'une zone.

### PUT /api/zones/{id}
Modifier une zone.

### DELETE /api/zones/{id}
Supprimer une zone.

---

## Codes d'Erreur

| Code | Message | Description |
|------|---------|-------------|
| AT_001 | AT non trouvée | L'AT spécifiée n'existe pas |
| AT_002 | AT non modifiable | L'AT n'est plus en état brouillon |
| AT_003 | Transition non autorisée | Transition de workflow non permise |
| AT_004 | Numérotation invalide | Erreur lors de la génération du numéro |
| USER_001 | Utilisateur non trouvé | L'utilisateur spécifié n'existe pas |
| USER_002 | Email déjà utilisé | L'email existe déjà |
| AUTH_001 | Authentification échouée | Identifiants incorrects |
| AUTH_002 | Token expiré | Le token JWT a expiré |
| VISA_001 | Visa non trouvé | Le visa spécifié n'existe pas |
| WF_001 | Workflow non configuré | Configuration workflow manquante |

---

## États des Autorisations de Travail

| État | Description |
|------|-------------|
| BROUILLON | AT en cours de rédaction |
| SOUMISE | AT soumise pour validation |
| VALIDEE | AT validée, prête pour exécution |
| EN_COURS | Travaux en cours |
| CLOTUREE | AT clôturée |
| REJETEE | AT rejetée |
| ANNULEE | AT annulée |

## États des Visas

| État | Description |
|------|-------------|
| EN_ATTENTE | Visa en attente de validation |
| VALIDE | Visa validé |
| REJETE | Visa rejeté |
