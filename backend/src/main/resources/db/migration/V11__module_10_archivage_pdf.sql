-- Module 10 : Export PDF, Archivage, Audit Final
-- Migration Flyway V11
-- Dernière étape du cycle de vie d'une Autorisation de Travail

-- ============================================================
-- AJOUT DES CHAMPS MANQUANTS À receptions_travaux
-- ============================================================
ALTER TABLE receptions_travaux ADD COLUMN IF NOT EXISTS validee BOOLEAN DEFAULT FALSE;
ALTER TABLE receptions_travaux ADD COLUMN IF NOT EXISTS essais_conformes BOOLEAN DEFAULT FALSE;
ALTER TABLE receptions_travaux ADD COLUMN IF NOT EXISTS installation_remise_en_etat BOOLEAN DEFAULT FALSE;
ALTER TABLE receptions_travaux ADD COLUMN IF NOT EXISTS signature_path VARCHAR(500);
ALTER TABLE receptions_travaux ADD COLUMN IF NOT EXISTS signature_date TIMESTAMP;
ALTER TABLE receptions_travaux ADD COLUMN IF NOT EXISTS signature_by VARCHAR(255);

-- ============================================================
-- TABLE : archives_at
-- ============================================================
CREATE TABLE archives_at (
    id                          VARCHAR(36)   NOT NULL,
    numero_archive              VARCHAR(100)  NOT NULL,
    version                     INTEGER       NOT NULL DEFAULT 1,
    date_archivage              TIMESTAMP     NOT NULL,
    archive_par_id              VARCHAR(36),
    created_at                  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    autorisation_travail_id     VARCHAR(36)   NOT NULL,
    path_pdf                    VARCHAR(500)  NOT NULL,
    hash_sha256                 VARCHAR(64)   NOT NULL,
    taille                      BIGINT,
    mime_type                   VARCHAR(100)  NOT NULL DEFAULT 'application/pdf',
    qr_code_path                VARCHAR(500),
    archive_status              VARCHAR(50)   NOT NULL DEFAULT 'ACTIVE',
    commentaire                 TEXT,
    deleted                     BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_archives_at PRIMARY KEY (id),
    CONSTRAINT uq_archive_numero UNIQUE (numero_archive),
    CONSTRAINT fk_archive_at FOREIGN KEY (autorisation_travail_id)
        REFERENCES autorisations_travail(id) ON DELETE RESTRICT,
    CONSTRAINT fk_archive_par FOREIGN KEY (archive_par_id)
        REFERENCES utilisateurs(id) ON DELETE SET NULL,
    CONSTRAINT uq_archive_at_version UNIQUE (autorisation_travail_id, version)
);

CREATE INDEX idx_archive_numero ON archives_at(numero_archive);
CREATE INDEX idx_archive_at ON archives_at(autorisation_travail_id);
CREATE INDEX idx_archive_par ON archives_at(archive_par_id);
CREATE INDEX idx_archive_hash ON archives_at(hash_sha256);
CREATE INDEX idx_archive_status ON archives_at(archive_status);
CREATE INDEX idx_archive_deleted ON archives_at(deleted);

-- ============================================================
-- MISE À JOUR DE L'ÉTAT STATUT AT
-- Ajout du statut ARCHIVEE
-- ============================================================
-- Le statut ARCHIVEE existe déjà dans l'enum, pas de modification nécessaire

-- ============================================================
-- NOUVELLES PERMISSIONS MODULE 10
-- ============================================================
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, p.nom, p.description
FROM (VALUES
    ('EXPORT_PDF',        'Exporter une AT clôturée en PDF'),
    ('DOWNLOAD_ARCHIVE',  'Télécharger une archive PDF'),
    ('VIEW_ARCHIVE',      'Consulter les archives'),
    ('VERIFY_ARCHIVE',    'Vérifier l intégrité d une archive'),
    ('SEARCH_ARCHIVE',    'Rechercher dans les archives')
) AS p(nom, description)
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE nom = p.nom
);

-- ============================================================
-- MISE À JOUR DES RÔLES EXISTANTS
-- Ajout des permissions Module 10 aux rôles appropriés
-- ============================================================

-- Ajout des permissions au rôle RESPONSABLE_OCP
INSERT INTO role_permissions (permission_id, role_id)
SELECT p.id, r.id
FROM permissions p, roles r
WHERE r.nom = 'RESPONSABLE_OCP'
AND p.nom IN ('EXPORT_PDF', 'DOWNLOAD_ARCHIVE', 'VIEW_ARCHIVE', 'VERIFY_ARCHIVE', 'SEARCH_ARCHIVE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.permission_id = p.id AND rp.role_id = r.id
);

-- Ajout des permissions au rôle ADMIN
INSERT INTO role_permissions (permission_id, role_id)
SELECT p.id, r.id
FROM permissions p, roles r
WHERE r.nom = 'ADMIN'
AND p.nom IN ('EXPORT_PDF', 'DOWNLOAD_ARCHIVE', 'VIEW_ARCHIVE', 'VERIFY_ARCHIVE', 'SEARCH_ARCHIVE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.permission_id = p.id AND rp.role_id = r.id
);

-- Ajout des permissions au rôle CHEF_DE_CHANTIER
INSERT INTO role_permissions (permission_id, role_id)
SELECT p.id, r.id
FROM permissions p, roles r
WHERE r.nom = 'CHEF_DE_CHANTIER'
AND p.nom IN ('EXPORT_PDF', 'DOWNLOAD_ARCHIVE', 'VIEW_ARCHIVE', 'SEARCH_ARCHIVE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.permission_id = p.id AND rp.role_id = r.id
);

-- ============================================================
-- MISE À JOUR DU WORKFLOW
-- Ajout de l'action ARCHIVAGE
-- ============================================================
-- L'action ARCHIVAGE est gérée par l'application, pas besoin de modification du workflow
