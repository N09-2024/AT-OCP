-- ============================================================
-- Migration V28: Rôles applicatifs CE / HM / HC / ADMIN / RESPONSABLE_EXTERIEUR
-- Conforme au Standard S-HSE-SEC-31 v1.0
--
-- Principe :
--   CEEP/CEEE, HMEP/HMEE, HCEP/HCEE ne sont PLUS des rôles utilisateur.
--   Ce sont des positions contextuelles (P/E) résolues via
--   utilisateur.service.zone vs AT.zoneProprietaire / zoneExecutante.
--
-- Rôles applicatifs :
--   CE                    → Chef d'Équipe (devient CEEP ou CEEE selon contexte)
--   HM                    → Haute Maîtrise (devient HMEP ou HMEE selon contexte)
--   HC                    → Hors Cadre (devient HCEP ou HCEE selon contexte)
--   ADMIN                 → Administrateur système
--   RESPONSABLE_EXTERIEUR → Sous-traitant (BT + permis uniquement)
-- ============================================================

-- ------------------------------------------------------------
-- 1. Permissions manquantes éventuelles
-- ------------------------------------------------------------
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'CLASSIFY_INTERVENTION', 'Classifier une intervention Niveau 1 / 2'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'CLASSIFY_INTERVENTION');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'ARCHIVE_AT', 'Archiver une autorisation de travail'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'ARCHIVE_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'VIEW_ARCHIVE', 'Consulter les archives AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'VIEW_ARCHIVE');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'MANAGE_BT', 'Gérer les Bons de Travaux (entreprise extérieure)'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'MANAGE_BT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'UPLOAD_PERMIS', 'Uploader / gérer les permis'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'UPLOAD_PERMIS');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'CREATE_AT', 'Créer une autorisation de travail'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'CREATE_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'EDIT_AT', 'Modifier un brouillon AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'EDIT_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'SUBMIT_AT', 'Soumettre une AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'SUBMIT_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'VALIDATE_AT', 'Valider / garantir une AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'VALIDATE_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'CLOSE_AT', 'Clôturer une AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'CLOSE_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'READ_AT', 'Consulter les AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'READ_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'EXPORT_PDF', 'Exporter PDF'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'EXPORT_PDF');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'VIEW_PERMIS', 'Consulter les permis'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'VIEW_PERMIS');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'EDIT_PERMIS', 'Éditer les permis'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'EDIT_PERMIS');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'UPLOAD_FILES', 'Uploader des fichiers'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'UPLOAD_FILES');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'MANAGE_REFERENTIALS', 'Gérer les référentiels'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'MANAGE_REFERENTIALS');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'VIEW_AUDIT', 'Consulter l''audit'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'VIEW_AUDIT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'MANAGE_USERS', 'Gérer les utilisateurs'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'MANAGE_USERS');

-- ------------------------------------------------------------
-- 2. Créer les 5 rôles applicatifs
-- ------------------------------------------------------------
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'CE', 'Chef d''Équipe — position CEEP ou CEEE selon le territoire de l''AT'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'CE');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HM', 'Haute Maîtrise — position HMEP ou HMEE selon le territoire de l''AT'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HM');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HC', 'Hors Cadre — position HCEP ou HCEE selon le territoire de l''AT'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HC');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'ADMIN', 'Administrateur système'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'ADMIN');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'RESPONSABLE_EXTERIEUR', 'Responsable Entreprise Extérieure (BT + permis uniquement)'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'RESPONSABLE_EXTERIEUR');

-- Alias éventuel si l'ancien nom RESPONSABLE_ENTREPRISE existe encore
UPDATE roles SET nom = 'RESPONSABLE_EXTERIEUR',
                  description = 'Responsable Entreprise Extérieure (BT + permis uniquement)'
WHERE nom = 'RESPONSABLE_ENTREPRISE'
  AND NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'RESPONSABLE_EXTERIEUR');

-- ------------------------------------------------------------
-- 3. Permissions par rôle applicatif
--    (les guards P/E restent dans ATContextService + services métier)
-- ------------------------------------------------------------

-- CE : union des capacités CEEP + CEEE (le contexte P/E filtre à l'exécution)
DELETE FROM role_permissions WHERE role_id = (SELECT id FROM roles WHERE nom = 'CE');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CE'
  AND p.nom IN (
    'CREATE_AT', 'EDIT_AT', 'SUBMIT_AT', 'READ_AT',
    'CREATE_VISITE', 'SIGN_AT', 'CLOSE_AT', 'RECEIVE_AT',
    'START_INTERVENTION', 'DECLARE_FIN_TRAVAUX', 'RENEW_AT',
    'VIEW_PERMIS', 'EDIT_PERMIS', 'UPLOAD_FILES', 'EXPORT_PDF',
    'RECEIVE_NOTIFICATION', 'TRANSFER_AT'
  );

-- HM : capacités HMEP (garant visite + démarrage) ; HMEE = lecture seule via contexte
DELETE FROM role_permissions WHERE role_id = (SELECT id FROM roles WHERE nom = 'HM');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HM'
  AND p.nom IN (
    'READ_AT', 'VALIDATE_VISITE', 'SIGN_AT', 'START_INTERVENTION',
    'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  );

-- HC : union HCEP + HCEE (classification, garantie, archivage, habilitations)
DELETE FROM role_permissions WHERE role_id = (SELECT id FROM roles WHERE nom = 'HC');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HC'
  AND p.nom IN (
    'READ_AT', 'CLASSIFY_INTERVENTION', 'VALIDATE_AT', 'REJECT_AT',
    'VALIDATE_VISITE', 'SIGN_AT', 'ARCHIVE_AT', 'VIEW_ARCHIVE',
    'MANAGE_HABILITATIONS', 'MANAGE_REFERENTIALS', 'VIEW_AUDIT',
    'VIEW_PERMIS', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  );

-- ADMIN : quasi tout
DELETE FROM role_permissions WHERE role_id = (SELECT id FROM roles WHERE nom = 'ADMIN');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'ADMIN';

-- RESPONSABLE_EXTERIEUR : BT + permis uniquement
DELETE FROM role_permissions WHERE role_id = (SELECT id FROM roles WHERE nom = 'RESPONSABLE_EXTERIEUR');
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'RESPONSABLE_EXTERIEUR'
  AND p.nom IN (
    'READ_AT', 'VIEW_PERMIS', 'EDIT_PERMIS', 'UPLOAD_PERMIS',
    'MANAGE_BT', 'UPLOAD_FILES', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  );

-- ------------------------------------------------------------
-- 4. Rapport de migration utilisateurs (anciens rôles → nouveaux)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role_migration_report (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    utilisateur_id VARCHAR(255) NOT NULL,
    matricule VARCHAR(255),
    nom_complet VARCHAR(500),
    email VARCHAR(255),
    ancien_role VARCHAR(255) NOT NULL,
    nouveau_role_propose VARCHAR(255),
    statut_migration VARCHAR(50) DEFAULT 'EN_ATTENTE',
    commentaire TEXT,
    date_migration TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Propositions automatiques
INSERT INTO role_migration_report (id, utilisateur_id, matricule, nom_complet, email, ancien_role, nouveau_role_propose, statut_migration, commentaire)
SELECT
    gen_random_uuid()::text,
    u.id,
    u.matricule,
    CONCAT(COALESCE(u.nom, ''), ' ', COALESCE(u.prenom, '')),
    u.email,
    r.nom,
    CASE r.nom
        WHEN 'CEEP' THEN 'CE'
        WHEN 'CEEE' THEN 'CE'
        WHEN 'DEMANDEUR' THEN 'CE'
        WHEN 'HMEP' THEN 'HM'
        WHEN 'HMEE' THEN 'HM'
        WHEN 'HCEP' THEN 'HC'
        WHEN 'HCEE' THEN 'HC'
        WHEN 'RESPONSABLE_OCP' THEN 'HC'
        WHEN 'RESPONSABLE_ENTREPRISE' THEN 'RESPONSABLE_EXTERIEUR'
        WHEN 'ADMIN' THEN 'ADMIN'
        ELSE NULL
    END,
    'EN_ATTENTE',
    'Migration V28 — rôles contextuels CE/HM/HC'
FROM utilisateurs u
JOIN utilisateur_roles ur ON ur.utilisateur_id = u.id
JOIN roles r ON r.id = ur.role_id
WHERE r.nom IN (
    'CEEP', 'CEEE', 'DEMANDEUR',
    'HMEP', 'HMEE',
    'HCEP', 'HCEE', 'RESPONSABLE_OCP',
    'RESPONSABLE_ENTREPRISE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_migration_report rm
    WHERE rm.utilisateur_id = u.id AND rm.ancien_role = r.nom
);

-- ------------------------------------------------------------
-- 5. Migration automatique des affectations (sauf cas ambigus)
-- ------------------------------------------------------------
-- CEEP / CEEE / DEMANDEUR → CE
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT DISTINCT ur.utilisateur_id, (SELECT id FROM roles WHERE nom = 'CE')
FROM utilisateur_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE r.nom IN ('CEEP', 'CEEE', 'DEMANDEUR')
  AND NOT EXISTS (
      SELECT 1 FROM utilisateur_roles ur2
      JOIN roles r2 ON r2.id = ur2.role_id
      WHERE ur2.utilisateur_id = ur.utilisateur_id AND r2.nom = 'CE'
  );

-- HMEP / HMEE → HM
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT DISTINCT ur.utilisateur_id, (SELECT id FROM roles WHERE nom = 'HM')
FROM utilisateur_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE r.nom IN ('HMEP', 'HMEE')
  AND NOT EXISTS (
      SELECT 1 FROM utilisateur_roles ur2
      JOIN roles r2 ON r2.id = ur2.role_id
      WHERE ur2.utilisateur_id = ur.utilisateur_id AND r2.nom = 'HM'
  );

-- HCEP / HCEE / RESPONSABLE_OCP → HC
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT DISTINCT ur.utilisateur_id, (SELECT id FROM roles WHERE nom = 'HC')
FROM utilisateur_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE r.nom IN ('HCEP', 'HCEE', 'RESPONSABLE_OCP')
  AND NOT EXISTS (
      SELECT 1 FROM utilisateur_roles ur2
      JOIN roles r2 ON r2.id = ur2.role_id
      WHERE ur2.utilisateur_id = ur.utilisateur_id AND r2.nom = 'HC'
  );

-- RESPONSABLE_ENTREPRISE → RESPONSABLE_EXTERIEUR (si pas déjà migré par UPDATE)
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT DISTINCT ur.utilisateur_id, (SELECT id FROM roles WHERE nom = 'RESPONSABLE_EXTERIEUR')
FROM utilisateur_roles ur
JOIN roles r ON r.id = ur.role_id
WHERE r.nom = 'RESPONSABLE_ENTREPRISE'
  AND NOT EXISTS (
      SELECT 1 FROM utilisateur_roles ur2
      JOIN roles r2 ON r2.id = ur2.role_id
      WHERE ur2.utilisateur_id = ur.utilisateur_id AND r2.nom = 'RESPONSABLE_EXTERIEUR'
  );

-- ------------------------------------------------------------
-- 6. Retirer les anciens rôles des utilisateurs
-- ------------------------------------------------------------
DELETE FROM utilisateur_roles
WHERE role_id IN (
    SELECT id FROM roles WHERE nom IN (
        'CEEP', 'CEEE', 'DEMANDEUR',
        'HMEP', 'HMEE',
        'HCEP', 'HCEE', 'RESPONSABLE_OCP',
        'RESPONSABLE_ENTREPRISE'
    )
);

DELETE FROM role_permissions
WHERE role_id IN (
    SELECT id FROM roles WHERE nom IN (
        'CEEP', 'CEEE', 'DEMANDEUR',
        'HMEP', 'HMEE',
        'HCEP', 'HCEE', 'RESPONSABLE_OCP',
        'RESPONSABLE_ENTREPRISE'
    )
);

-- Ne pas supprimer physiquement les anciens rôles tout de suite
-- (références éventuelles dans historiques / logs).
-- On les marque comme obsolètes via description.
UPDATE roles
SET description = CONCAT('[OBSOLÈTE V28] ', COALESCE(description, ''))
WHERE nom IN (
    'CEEP', 'CEEE', 'DEMANDEUR',
    'HMEP', 'HMEE',
    'HCEP', 'HCEE', 'RESPONSABLE_OCP',
    'RESPONSABLE_ENTREPRISE'
)
AND description NOT LIKE '[OBSOLÈTE V28]%';

-- ------------------------------------------------------------
-- 7. Marquer le rapport comme traité pour les migrations auto
-- ------------------------------------------------------------
UPDATE role_migration_report
SET statut_migration = 'TRAITE_AUTO',
    date_migration = CURRENT_TIMESTAMP
WHERE nouveau_role_propose IS NOT NULL
  AND statut_migration = 'EN_ATTENTE';
