-- ============================================================
-- Module 8 : Workflow, Visa, Signature, Notifications, Audit
-- ============================================================

-- 1. Enrichissement table VISAS (signature manuscrite)
ALTER TABLE visas
    ADD COLUMN IF NOT EXISTS ordre INTEGER DEFAULT 1,
    ADD COLUMN IF NOT EXISTS date_signature TIMESTAMP,
    ADD COLUMN IF NOT EXISTS signature_path VARCHAR(512),
    ADD COLUMN IF NOT EXISTS signature_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS adresse_ip VARCHAR(64),
    ADD COLUMN IF NOT EXISTS navigateur VARCHAR(255);

-- Mise à jour du statut visa (EN_ATTENTE, VALIDE, REFUSE au lieu de VALIDATION, SIGNATURE, REFUS)
-- On met à jour les contraintes existantes
ALTER TABLE visas DROP CONSTRAINT IF EXISTS visas_statut_check;
ALTER TABLE visas ADD CONSTRAINT visas_statut_check
    CHECK (statut IN ('EN_ATTENTE','VALIDE','REFUSE','VALIDATION','SIGNATURE','REFUS'));

-- 2. Enrichissement table WORKFLOWS_AT
ALTER TABLE workflows_at
    ADD COLUMN IF NOT EXISTS ordre_validation INTEGER DEFAULT 1,
    ADD COLUMN IF NOT EXISTS validation_obligatoire BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS actif BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS role_suivant VARCHAR(255),
    ADD COLUMN IF NOT EXISTS notification_suivante VARCHAR(512);

-- 3. Enrichissement table NOTIFICATIONS (type + lien)
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS type VARCHAR(50) DEFAULT 'INFO',
    ADD COLUMN IF NOT EXISTS lien VARCHAR(512);

-- 4. Enrichissement table HISTORIQUES_AT (utilisateur tracé)
ALTER TABLE historiques_at
    ADD COLUMN IF NOT EXISTS utilisateur_id VARCHAR(255);

ALTER TABLE historiques_at
    ADD CONSTRAINT IF NOT EXISTS fk_historique_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id);

-- 5. Enrichissement table AUDIT_LOGS (système exploitation)
ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS systeme_exploitation VARCHAR(255);

-- 6. Insertion des permissions Module 8
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_submit_at',   'SUBMIT_AT',   'Soumettre une AT pour validation')
    ON CONFLICT (nom) DO NOTHING;
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_validate_at', 'VALIDATE_AT', 'Valider une AT')
    ON CONFLICT (nom) DO NOTHING;
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_refuse_at',   'REFUSE_AT',   'Refuser une AT')
    ON CONFLICT (nom) DO NOTHING;
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_sign_at',     'SIGN_AT',     'Signer électroniquement un visa AT')
    ON CONFLICT (nom) DO NOTHING;
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_renew_at',    'RENEW_AT',    'Renouveler une AT')
    ON CONFLICT (nom) DO NOTHING;
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_close_at',    'CLOSE_AT',    'Clôturer une AT')
    ON CONFLICT (nom) DO NOTHING;

-- 7. Affectation au rôle ADMIN (role_1) et SUPERVISEUR (role_2)
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_1', 'perm_submit_at')   ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_1', 'perm_validate_at') ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_1', 'perm_refuse_at')   ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_1', 'perm_sign_at')     ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_1', 'perm_renew_at')    ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_1', 'perm_close_at')    ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_2', 'perm_submit_at')   ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_2', 'perm_validate_at') ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_2', 'perm_refuse_at')   ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_2', 'perm_sign_at')     ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_2', 'perm_renew_at')    ON CONFLICT DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id) VALUES ('role_2', 'perm_close_at')    ON CONFLICT DO NOTHING;
