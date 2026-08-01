-- ============================================================
-- Migration V20: Ajout des 6 rôles standard OCP + concept territorial
-- Conforme au Standard S-HSE-SEC-31 v1.0
-- ============================================================

-- ============================================================
-- PARTIE 1: Ajouter les nouvelles permissions standard
-- ============================================================
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'CREATE_VISITE', 'Créer une visite chantier'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'CREATE_VISITE');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'VALIDATE_VISITE', 'Valider une visite chantier'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'VALIDATE_VISITE');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'SIGN_AT', 'Signer une autorisation de travail'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'SIGN_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'RECEIVE_AT', 'Réceptionner une autorisation de travail'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'RECEIVE_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'DECLARE_FIN_TRAVAUX', 'Déclarer la fin des travaux'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'DECLARE_FIN_TRAVAUX');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'START_INTERVENTION', 'Démarrer une intervention'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'START_INTERVENTION');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'MANAGE_HABILITATIONS', 'Gérer les habilitations AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'MANAGE_HABILITATIONS');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'REJECT_AT', 'Rejeter une autorisation de travail'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'REJECT_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'RECEIVE_NOTIFICATION', 'Recevoir des notifications'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'RECEIVE_NOTIFICATION');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'RENEW_AT', 'Renouveler une autorisation de travail'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'RENEW_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'TRANSFER_AT', 'Transférer le verrou d''édition'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'TRANSFER_AT');

-- ============================================================
-- PARTIE 2: Créer les 6 nouveaux rôles standard OCP
-- ============================================================

-- 2.1: CEEP - Chef d'Équipe Entité Propriétaire
-- Permissions: Exécute les étapes 8.1, 8.2, 8.3, 8.4, 8.5 (réception)
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'CEEP', 'Chef d''Équipe de l''Entité Propriétaire'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'CEEP');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CEEP'
  AND p.nom IN ('CREATE_AT', 'EDIT_AT', 'SUBMIT_AT', 'READ_AT', 'CREATE_VISITE', 'SIGN_AT', 'CLOSE_AT', 'RECEIVE_AT', 'UPLOAD_FILES', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- 2.2: CEEE - Chef d'Équipe Entité Exécutante
-- Permissions: Participe étapes 8.2, 8.3, 8.4, 8.5; Exécute début intervention, déclaration fin
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'CEEE', 'Chef d''Équipe de l''Entité Exécutante'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'CEEE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CEEE'
  AND p.nom IN ('READ_AT', 'EDIT_AT', 'SIGN_AT', 'VIEW_PERMIS', 'EDIT_PERMIS', 'START_INTERVENTION', 'DECLARE_FIN_TRAVAUX', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- 2.3: HCEP - Hors Cadre Entité Propriétaire
-- Permissions: Garant archivage (8.6), désignation agents habilités (§9), gestion liste Niveau 1
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HCEP', 'Hors Cadre Responsable de l''Entité Propriétaire'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HCEP');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HCEP'
  AND p.nom IN ('READ_AT', 'EXPORT_PDF', 'MANAGE_HABILITATIONS', 'MANAGE_REFERENTIALS', 'VIEW_AUDIT', 'RECEIVE_NOTIFICATION')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- 2.4: HCEE - Hors Cadre Entité Exécutante
-- Permissions: Garant visites, rédaction AT, début intervention, visa, archivage (exécute)
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HCEE', 'Hors Cadre Responsable de l''Entité Exécutante'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HCEE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HCEE'
  AND p.nom IN ('READ_AT', 'VALIDATE_AT', 'REJECT_AT', 'SIGN_AT', 'VALIDATE_VISITE', 'VIEW_PERMIS', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- 2.5: HMEP - Haute Maîtrise Entité Propriétaire
-- Permissions: Garant visite chantier (8.2), garant début intervention
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HMEP', 'Haute Maîtrise de l''Entité Propriétaire'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HMEP');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HMEP'
  AND p.nom IN ('READ_AT', 'VALIDATE_VISITE', 'SIGN_AT', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- 2.6: HMEE - Haute Maîtrise Entité Exécutante
-- TODO: à valider avec OCP - permissions minimales (lecture seule) en attendant clarification
-- Le standard ne définit pas clairement les responsabilités du HMEE
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HMEE', 'Haute Maîtrise de l''Entité Exécutante'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HMEE');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HMEE'
  AND p.nom IN ('READ_AT', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- ============================================================
-- PARTIE 3: Créer la table de rapport de migration des rôles
-- ============================================================
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

-- Lister tous les utilisateurs ayant DEMANDEUR ou RESPONSABLE_OCP
INSERT INTO role_migration_report (id, utilisateur_id, matricule, nom_complet, email, ancien_role, nouveau_role_propose, statut_migration)
SELECT
    gen_random_uuid()::text,
    u.id,
    u.matricule,
    u.prenom || ' ' || u.nom,
    u.email,
    r.nom,
    NULL, -- Nécessite validation humaine OCP
    'EN_ATTENTE'
FROM utilisateurs u
JOIN utilisateur_roles ur ON u.id = ur.utilisateur_id
JOIN roles r ON ur.role_id = r.id
WHERE r.nom IN ('DEMANDEUR', 'RESPONSABLE_OCP');

-- ============================================================
-- PARTIE 4: Ajouter les colonnes territoriales sur autorisations_travail
-- zone_proprietaire_id = Entité Propriétaire (P) - le service/zone où se déroule l'intervention
-- zone_executante_id = Entité Exécutante (E) - le service qui intervient
-- Les deux référencent la table zones (même type d'objet, rôles différents)
-- ============================================================
ALTER TABLE autorisations_travail
ADD COLUMN IF NOT EXISTS zone_proprietaire_id VARCHAR(255),
ADD COLUMN IF NOT EXISTS zone_executante_id VARCHAR(255);

-- Contraintes de clés étrangères vers la table zones
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_at_zone_proprietaire' AND table_name = 'autorisations_travail'
    ) THEN
        ALTER TABLE autorisations_travail
        ADD CONSTRAINT fk_at_zone_proprietaire
        FOREIGN KEY (zone_proprietaire_id) REFERENCES zones(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_at_zone_executante' AND table_name = 'autorisations_travail'
    ) THEN
        ALTER TABLE autorisations_travail
        ADD CONSTRAINT fk_at_zone_executante
        FOREIGN KEY (zone_executante_id) REFERENCES zones(id);
    END IF;
END
$$;

-- Index pour les recherches par zone
CREATE INDEX IF NOT EXISTS idx_at_zone_proprietaire ON autorisations_travail(zone_proprietaire_id);
CREATE INDEX IF NOT EXISTS idx_at_zone_executante ON autorisations_travail(zone_executante_id);

-- ============================================================
-- NOTE: Les anciens rôles DEMANDEUR et RESPONSABLE_OCP ne sont PAS supprimés
-- automatiquement. Ils seront supprimés uniquement après validation humaine
-- que la table role_migration_report est vide.
-- Voir MIGRATION_ROLES.md pour la procédure de requalification manuelle.
-- ============================================================