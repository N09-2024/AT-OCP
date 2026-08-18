# Migration des Rôles & Workflow - Standard OCP S-HSE-SEC-31 v1.0

## Contexte & Principes Métier

Ce document détaille l'implémentation et la procédure de migration de l'ancien modèle à 4 rôles (`ADMIN`, `DEMANDEUR`, `RESPONSABLE_OCP`, `RESPONSABLE_ENTREPRISE`) vers les **8 rôles opérationnels** conformes au Standard OCP S-HSE-SEC-31 v1.0 :
`ADMIN`, `CEEP`, `CEEE`, `HCEP`, `HCEE`, `HMEP`, `HMEE`, `RESPONSABLE_ENTREPRISE`.

### Principe Métier P/E (Propriétaire / Exécutant)

1. **P et E sont des positions contextuelles dépendant du territoire de l'AT**, et non des catégories d'utilisateurs.
   - **P (Propriétaire)** = l'entité/service/zone responsable de l'installation où se déroule l'intervention.
   - **E (Exécutant)** = l'entité/service qui intervient dans le périmètre de P.
2. Un utilisateur `CEEP` (Chef d'Équipe de l'Entité Propriétaire) sur son propre service agit en position **P** sur les AT de sa zone, mais intervient en position **E** (`CEEE`) lorsqu'il réalise des travaux sur le territoire d'un autre service.
3. Le backend résout dynamiquement cette position via la comparaison entre `utilisateur.service.zone.id` et `autorisation_travail.zone_proprietaire_id` / `zone_executante_id`.
4. `RESPONSABLE_ENTREPRISE` est le sous-traitant externe, uniquement concerné par le **Bon de Travail (BT)** et ses permis associées. Il ne participe pas au workflow AT principal.

---

## Matrice des Permissions par Rôle (Standard §8)

| Code Permission | Description | ADMIN | CEEP | CEEE | HCEP | HCEE | HMEP | HMEE | RESP. ENT. |
|---|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `READ_AT` | Consulter les AT | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `CREATE_AT` | Créer une AT / DI (§8.1) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `EDIT_AT` | Modifier un brouillon AT | ✅ | ✅ | ✅ (P) | ❌ | ❌ | ❌ | ❌ | ❌ |
| `SUBMIT_AT` | Soumettre une AT | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `VALIDATE_AT` | Valider une AT (§8.3 G) | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `REJECT_AT` | Rejeter une AT | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `CLOSE_AT` | Clôturer AT & Permis (§8.5) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `CLASSIFY_INTERVENTION` | Classifier Niv 1/2 (Étape 0) | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `CREATE_VISITE` | Réaliser visite chantier (§8.2) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `VALIDATE_VISITE` | Valider visite (§8.2 G) | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| `SIGN_AT` | Signer/viser AT et permis | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ |
| `START_INTERVENTION` | Démarrer intervention (§4) | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `DECLARE_FIN_TRAVAUX` | Déclarer fin travaux (§8.5) | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `RECEIVE_AT` | Réceptionner travaux (§8.5) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `RENEW_AT` | Reconduire AT (§8.4) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| `ARCHIVE_AT` | Archiver officiellement (§8.6 E) | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| `VIEW_ARCHIVE` | Consulter archives (§8.6 G) | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ |
| `MANAGE_HABILITATIONS` | Désigner agents habilités (§9) | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| `VIEW_PERMIS` | Consulter les permis | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `CREATE_PERMIS` / `UPLOAD_PERMIS` | Gérer permis BT | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

*Legend: ✅ = Autorisisation, ❌ = Interdit, P = Participe, G = Garant, E = Exécute*

---

## Procédure d'Application de la Migration Flyway V21

### 1. Rebuild des conteneurs Docker sans cache

Lors de la mise à jour des fichiers de migration Flyway ou du code source Java backend :

```bash
# Rebuild strict sans cache du service backend
docker compose build --no-cache backend

# Réinitialiser les volumes si nécessaire
docker compose down -v

# Lancer la pile de conteneurs
docker compose up -d

# Vérifier l'exécution de Flyway V21 dans les logs
docker compose logs backend | grep -E "V21|Successfully applied"
```

### 2. Procédure de Requalification Manuelle des Utilisateurs

Après application des migrations Flyway `V20` et `V21`, consulter la table `role_migration_report` pour traiter les comptes ayant les anciens rôles `DEMANDEUR` et `RESPONSABLE_OCP`.

```sql
-- 1. Consulter la liste des utilisateurs en attente
SELECT id, matricule, nom_complet, email, ancien_role, statut_migration
FROM role_migration_report
WHERE statut_migration = 'EN_ATTENTE';

-- 2. Rattacher l'utilisateur à son service d'appartenance (REQUIS pour la logique P/E)
UPDATE utilisateurs 
SET service_id = (SELECT id FROM services WHERE code_service = 'SERV_EXEMPLE')
WHERE email = 'user@ocp.ma';

-- 3. Attribuer le nouveau rôle standard
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT u.id, r.id
FROM utilisateurs u, roles r
WHERE u.email = 'user@ocp.ma' AND r.nom = 'CEEP';

-- 4. Retirer l'ancien rôle et marquer comme traité
DELETE FROM utilisateur_roles 
WHERE utilisateur_id = (SELECT id FROM utilisateurs WHERE email = 'user@ocp.ma')
  AND role_id IN (SELECT id FROM roles WHERE nom IN ('DEMANDEUR', 'RESPONSABLE_OCP'));

UPDATE role_migration_report 
SET statut_migration = 'TRAITE', nouveau_role_propose = 'CEEP'
WHERE email = 'user@ocp.ma';
```

---

## Points de vigilance & Exceptions

1. **Rôle HMEE** : Fail-closed intentionnel (lecture seule). Les cases associées à HMEE dans le logigramme du standard §7 sont non renseignées. Aucune permission d'écriture ne lui est accordée sans validation préalable de l'OCP.
2. **Champ `statutWorkflow`** : Les 9 états du standard (`CLASSIFICATION_EFFECTUEE`, `DEMANDE_CREEE`, `VISITE_REALISEE`, `AT_REDIGEE`, `INTERVENTION_EN_COURS`, `AT_RECONDUITE`, `FIN_TRAVAUX_DECLAREE`, `TRAVAUX_RECEPTIONES`, `ARCHIVEE`) sont stockés dans le champ `statut_workflow` de l'entité `AutorisationTravail`, en parallèle du champ `statut` legacy pour préserver la rétrocompatibilité.