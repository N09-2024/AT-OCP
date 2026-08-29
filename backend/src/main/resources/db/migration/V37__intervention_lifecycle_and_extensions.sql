-- ============================================================
-- Migration V37: Cycle de vie complet AT post-validation
-- Reconductions (CEEE -> HMEP Responsable OCP), Réception conjointe,
-- Démarrage/Fin réels et nouvelles transitions workflow
-- ============================================================

-- 1. Nouvelles colonnes sur autorisations_travail
ALTER TABLE autorisations_travail
    ADD COLUMN IF NOT EXISTS date_demarrage TIMESTAMP,
    ADD COLUMN IF NOT EXISTS date_fin_reelle TIMESTAMP,
    ADD COLUMN IF NOT EXISTS ceee_id VARCHAR(36) REFERENCES utilisateurs(id);

-- 2. Nouvelles colonnes sur receptions_travaux
ALTER TABLE receptions_travaux
    ADD COLUMN IF NOT EXISTS resultat_reception VARCHAR(30) DEFAULT 'CONFORME',
    ADD COLUMN IF NOT EXISTS reserves_description TEXT,
    ADD COLUMN IF NOT EXISTS actions_correctives TEXT,
    ADD COLUMN IF NOT EXISTS reception_conjointe_validee BOOLEAN DEFAULT FALSE;

-- 3. Colonne type_visa sur la table visas pour qualifier le visa (STANDARD, RECEPTION_CEEP, RECEPTION_CEEE, etc.)
ALTER TABLE visas
    ADD COLUMN IF NOT EXISTS type_visa VARCHAR(50) DEFAULT 'STANDARD';

-- 4. Création de la table des demandes de reconduction
CREATE TABLE IF NOT EXISTS reconductions (
    id VARCHAR(36) PRIMARY KEY,
    autorisation_travail_id VARCHAR(36) NOT NULL REFERENCES autorisations_travail(id) ON DELETE CASCADE,
    demandeur_id VARCHAR(36) NOT NULL REFERENCES utilisateurs(id),
    date_demande TIMESTAMP NOT NULL,
    date_fin_initiale TIMESTAMP NOT NULL,
    nouvelle_date_fin TIMESTAMP NOT NULL,
    motif TEXT NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    decision_par_id VARCHAR(36) REFERENCES utilisateurs(id),
    date_decision TIMESTAMP,
    motif_refus TEXT,
    commentaire TEXT,
    analyse_ia_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reconductions_at_id ON reconductions(autorisation_travail_id);
CREATE INDEX IF NOT EXISTS idx_reconductions_statut ON reconductions(statut);

-- 5. Nouvelles permissions pour la gestion des reconductions
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, p.nom, p.description
FROM (VALUES
    ('REQUEST_EXTENSION', 'Demander la reconduction d''une AT (CEEE)'),
    ('APPROVE_EXTENSION', 'Approuver la reconduction d''une AT (HMEP / Responsable OCP)'),
    ('REJECT_EXTENSION',  'Refuser la reconduction d''une AT (HMEP / Responsable OCP)')
) AS p(nom, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = p.nom);

-- 6. Attribution des permissions aux rôles correspondants
-- CEEE et CE : REQUEST_EXTENSION
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom IN ('CEEE', 'CE')
  AND p.nom = 'REQUEST_EXTENSION'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- HMEP, HM (Responsable OCP) : APPROVE_EXTENSION, REJECT_EXTENSION
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom IN ('HMEP', 'HM')
  AND p.nom IN ('APPROVE_EXTENSION', 'REJECT_EXTENSION')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ADMIN : toutes les nouvelles permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'ADMIN'
  AND p.nom IN ('REQUEST_EXTENSION', 'APPROVE_EXTENSION', 'REJECT_EXTENSION')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 7. Insertion des nouvelles transitions de workflow standard
INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise, validation_obligatoire, actif)
SELECT gen_random_uuid()::text, w.depart, w.arrivee, w.act, w.perm, w.val, true
FROM (VALUES
    ('AT_VALIDEE',            'INTERVENTION_EN_COURS', 'DEBUT_INTERVENTION',      'START_INTERVENTION', false),
    ('INTERVENTION_EN_COURS', 'AT_RECONDUITE',         'APPROBATION_RECONDUCTION','APPROVE_EXTENSION',  true),
    ('INTERVENTION_EN_COURS', 'AT_RECONDUITE',         'RECONDUCTION',             'RENEW_AT',           true),
    ('INTERVENTION_EN_COURS', 'FIN_TRAVAUX_DECLAREE',  'DECLARATION_FIN',         'DECLARE_FIN_TRAVAUX',false),
    ('AT_RECONDUITE',         'FIN_TRAVAUX_DECLAREE',  'DECLARATION_FIN',         'DECLARE_FIN_TRAVAUX',false),
    ('AT_RECONDUITE',         'INTERVENTION_EN_COURS', 'DEBUT_INTERVENTION',      'START_INTERVENTION', false),
    ('FIN_TRAVAUX_DECLAREE',  'TRAVAUX_RECEPTIONES',   'RECEPTION_CONJOINTE',     'RECEIVE_AT',         true),
    ('TRAVAUX_RECEPTIONES',   'CLOTUREE',              'CLOTURE',                 'CLOSE_AT',           false),
    ('CLOTUREE',              'ARCHIVEE',              'ARCHIVAGE_OFFICIEL',      'ARCHIVE_AT',         false)
) AS w(depart, arrivee, act, perm, val)
WHERE NOT EXISTS (
    SELECT 1 FROM workflows_at
    WHERE etat_depart = w.depart AND etat_arrivee = w.arrivee AND action = w.act AND actif = true
);
