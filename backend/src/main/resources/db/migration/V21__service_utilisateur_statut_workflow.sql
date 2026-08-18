-- ============================================================
-- V21 - Conformité Standard S-HSE-SEC-31 v1.0
-- Auteur : Migration automatique AT-OCP
-- Date   : 2026
-- ============================================================
-- Contenu :
--   PARTIE 1 : Lien utilisateur → service (résolution contextuelle P/E)
--   PARTIE 2 : Champ statut_workflow sur les AT (statuts standard §7)
--   PARTIE 3 : Nouvelles permissions granulaires manquantes
--   PARTIE 4 : Attribution des nouvelles permissions aux rôles standard
-- ============================================================

-- ============================================================
-- PARTIE 1 : Lien utilisateur → service (FONDAMENTAL pour P/E)
-- ============================================================
-- Ce lien permet de déterminer à l'exécution si un utilisateur est
-- côté P (propriétaire) ou E (exécutant) sur une AT donnée :
--   utilisateur.service == at.zone_proprietaire.service  → position P
--   utilisateur.service == at.zone_executante.service    → position E
-- Un même utilisateur peut être P sur une AT et E sur une autre.

ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS service_id VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_utilisateur_service'
          AND table_name = 'utilisateurs'
    ) THEN
        ALTER TABLE utilisateurs
            ADD CONSTRAINT fk_utilisateur_service
            FOREIGN KEY (service_id) REFERENCES services(id)
            ON DELETE SET NULL;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_utilisateur_service ON utilisateurs(service_id);

-- ============================================================
-- PARTIE 2 : Champ statut_workflow (statuts §7 du standard)
-- ============================================================
-- Distinct du champ 'statut' legacy (conservé pour rétrocompatibilité).
-- Les nouvelles AT utilisent statut_workflow pour les transitions.

ALTER TABLE autorisations_travail
    ADD COLUMN IF NOT EXISTS statut_workflow VARCHAR(50);

-- Mapping best-effort des AT existantes
-- (nécessite validation humaine pour les cas ambigus)
UPDATE autorisations_travail
SET statut_workflow = CASE statut
    WHEN 'BROUILLON'   THEN 'DEMANDE_CREEE'
    WHEN 'SOUMISE'     THEN 'VISITE_REALISEE'
    WHEN 'VALIDEE'     THEN 'AT_REDIGEE'
    WHEN 'RENOUVELEE'  THEN 'AT_RECONDUITE'
    WHEN 'CLOTUREE'    THEN 'TRAVAUX_RECEPTIONES'
    WHEN 'ARCHIVEE'    THEN 'ARCHIVEE'
    WHEN 'REJETEE'     THEN 'REJETEE'
    WHEN 'ANNULEE'     THEN 'ANNULEE'
    ELSE 'DEMANDE_CREEE'
END
WHERE statut_workflow IS NULL;

CREATE INDEX IF NOT EXISTS idx_at_statut_workflow ON autorisations_travail(statut_workflow);

-- ============================================================
-- PARTIE 3 : Nouvelles permissions granulaires
-- ============================================================

-- CLASSIFY_INTERVENTION : Étape 0 - HCEP classifie Niveau 1/2
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'CLASSIFY_INTERVENTION', 'Classifier une intervention Niveau 1 ou Niveau 2 (HCEP)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'CLASSIFY_INTERVENTION');

-- CREATE_VISITE : Étape 2 - CEEP crée la visite préalable du chantier
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'CREATE_VISITE', 'Créer et réaliser une visite préalable du chantier (CEEP)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'CREATE_VISITE');

-- VALIDATE_VISITE : Étape 2 - HCEE et HMEP valident/garantissent la visite
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'VALIDATE_VISITE', 'Valider/garantir une visite préalable du chantier (HCEE, HMEP)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'VALIDATE_VISITE');

-- SIGN_AT : Étapes 3, 5b - Signer ou viser une AT ou un permis
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'SIGN_AT', 'Signer ou viser une Autorisation de Travail ou un permis'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'SIGN_AT');

-- START_INTERVENTION : Étape 4 - CEEE démarre l'intervention
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'START_INTERVENTION', 'Démarrer une intervention (CEEE - Exécutant)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'START_INTERVENTION');

-- DECLARE_FIN_TRAVAUX : Étape 6 - CEEE déclare la fin des travaux
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'DECLARE_FIN_TRAVAUX', 'Déclarer la fin des travaux (CEEE - Exécutant)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'DECLARE_FIN_TRAVAUX');

-- RECEIVE_AT : Étape 7 - CEEP réceptionne les travaux
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'RECEIVE_AT', 'Réceptionner les travaux (CEEP - Propriétaire)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'RECEIVE_AT');

-- RENEW_AT : Étape 5b - Reconduire/renouveler une AT (dépassement poste)
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'RENEW_AT', 'Reconduire ou renouveler une AT en cas de dépassement de poste'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'RENEW_AT');

-- RECEIVE_NOTIFICATION : Recevoir des notifications système
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'RECEIVE_NOTIFICATION', 'Recevoir des notifications système liées aux AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'RECEIVE_NOTIFICATION');

-- ARCHIVE_AT : Étape 8 - HCEE archive officiellement l'AT
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'ARCHIVE_AT', 'Archiver officiellement une AT et ses documents (HCEE - Exécutant §8.6)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'ARCHIVE_AT');

-- VIEW_ARCHIVE : Consulter les archives (HCEP garant, HCEE exécutant)
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'VIEW_ARCHIVE', 'Consulter et télécharger les archives AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'VIEW_ARCHIVE');

-- MANAGE_HABILITATIONS : §9 - HCEP désigne les agents habilités AT
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'MANAGE_HABILITATIONS', 'Désigner et gérer les agents habilités à délivrer une AT (HCEP §9)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'MANAGE_HABILITATIONS');

-- TRANSFER_AT : Transférer le verrou d'édition entre utilisateurs du même rôle
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'TRANSFER_AT', 'Transférer le verrou d''édition d''une AT à un autre utilisateur'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'TRANSFER_AT');

-- ============================================================
-- PARTIE 4 : Attribution des nouvelles permissions aux rôles
-- ============================================================

-- ---- CEEP (Chef d'Équipe Entité Propriétaire) ----
-- §8.1 E, §8.2 E, §8.3 E, §8.4 visa E, §8.5 réception E, §6 liste Niv1
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CEEP'
  AND p.nom IN (
      'CREATE_VISITE',      -- §8.2 Exécute la visite chantier
      'SIGN_AT',            -- §8.3 Signe l'AT initiale + §8.4 visa reconduction
      'RENEW_AT',           -- §8.4 Reconduction AT (dépassement poste)
      'RECEIVE_AT',         -- §8.5 Réceptionne les travaux
      'TRANSFER_AT',        -- Transfert verrou
      'RECEIVE_NOTIFICATION'-- Informé étape 6 (déclaration fin CEEE)
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ---- CEEE (Chef d'Équipe Entité Exécutante) ----
-- §8.1 I, §8.2 P, §8.3 P, §4 début E, §8.4 P, §8.5 déclaration E, §8.5 réception P
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CEEE'
  AND p.nom IN (
      'START_INTERVENTION',  -- §4 Exécute le démarrage de l'intervention
      'DECLARE_FIN_TRAVAUX', -- §8.5 Exécute la déclaration de fin
      'SIGN_AT',             -- §8.3 P, §8.4 P (co-signe/vise)
      'RECEIVE_NOTIFICATION' -- Informé étape 1 (demande CEEP)
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ---- HCEP (Hors Cadre Entité Propriétaire) ----
-- §8.6 G (archivage), §9 désignation agents habilités, Étape 0 classification
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HCEP'
  AND p.nom IN (
      'CLASSIFY_INTERVENTION', -- Étape 0 classifie Niveau 1/2
      'MANAGE_HABILITATIONS',  -- §9 désigne les agents habilités AT
      'VIEW_ARCHIVE',          -- §8.6 Garant - consulte/supervise les archives
      'RECEIVE_NOTIFICATION'   -- Notifications système
      -- Note : HCEP n'archive PAS lui-même (G ≠ E sur §8.6)
      -- TODO: valider avec OCP si HCEP a une signature opérationnelle directe sur l'AT
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ---- HCEE (Hors Cadre Entité Exécutante) ----
-- §8.2 G, §8.3 G, §4 début G, §8.4 G, §8.6 E (archivage)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HCEE'
  AND p.nom IN (
      'VALIDATE_VISITE',   -- §8.2 Garant de la visite chantier
      'SIGN_AT',           -- §8.3 Garant (co-signe/valide l'AT)
      'ARCHIVE_AT',        -- §8.6 Exécute l'archivage officiel
      'VIEW_ARCHIVE',      -- §8.6 Consulte les archives
      'RECEIVE_NOTIFICATION' -- Notifications système
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ---- HMEP (Haute Maîtrise Entité Propriétaire) ----
-- §8.2 G, §4 début G - rôle de garant à la visite et au démarrage uniquement
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HMEP'
  AND p.nom IN (
      'VALIDATE_VISITE',    -- §8.2 Garant de la visite chantier
      'SIGN_AT',            -- §8.3/§4 Garant (co-signe/valide)
      'RECEIVE_NOTIFICATION'-- Notifications système
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ---- HMEE (Haute Maîtrise Entité Exécutante) ----
-- TODO: à valider avec OCP - rôle non clarifié dans le logigramme standard
-- Comportement fail-closed intentionnel : lecture + notification seules
-- Aucune nouvelle permission d'écriture attribuée en attendant clarification OCP
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HMEE'
  AND p.nom IN ('RECEIVE_NOTIFICATION')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ---- RESPONSABLE_ENTREPRISE (Sous-traitant externe) ----
-- Hors logique P/E - uniquement Bon de Travail (BT) et permis liés
-- Ne participe PAS au workflow AT normal
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'RESPONSABLE_ENTREPRISE'
  AND p.nom IN ('RECEIVE_NOTIFICATION')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ============================================================
-- NOTE FINALE : Les anciens rôles DEMANDEUR et RESPONSABLE_OCP
-- ne sont PAS supprimés automatiquement.
-- Consulter role_migration_report et MIGRATION_ROLES.md
-- pour la procédure de requalification manuelle.
-- ============================================================
