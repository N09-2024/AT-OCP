-- ============================================================
-- V11__module_10_archivage_pdf.sql
-- Table archives_at + permissions d'archivage
-- ============================================================

-- Table archives_at (nouvelle - pas dans V1)
CREATE TABLE IF NOT EXISTS archives_at (
    id                      VARCHAR(36)  NOT NULL,
    numero_archive          VARCHAR(100) NOT NULL,
    version                 INTEGER      NOT NULL DEFAULT 1,
    date_archivage          TIMESTAMP    NOT NULL,
    archive_par_id          VARCHAR(36),
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    autorisation_travail_id VARCHAR(36)  NOT NULL,
    path_pdf                VARCHAR(500) NOT NULL,
    hashsha256             VARCHAR(64)  NOT NULL,
    taille                  BIGINT,
    mime_type               VARCHAR(100) NOT NULL DEFAULT 'application/pdf',
    qr_code_path            VARCHAR(500),
    archive_status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    commentaire             TEXT,
    deleted                 BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_archives_at PRIMARY KEY (id),
    CONSTRAINT uq_archive_numero UNIQUE (numero_archive),
    CONSTRAINT fk_archive_at FOREIGN KEY (autorisation_travail_id)
        REFERENCES autorisations_travail(id) ON DELETE RESTRICT,
    CONSTRAINT fk_archive_par FOREIGN KEY (archive_par_id)
        REFERENCES utilisateurs(id) ON DELETE SET NULL,
    CONSTRAINT uq_archive_at_version UNIQUE (autorisation_travail_id, version)
);

CREATE INDEX IF NOT EXISTS idx_archive_numero   ON archives_at(numero_archive);
CREATE INDEX IF NOT EXISTS idx_archive_at_id    ON archives_at(autorisation_travail_id);
CREATE INDEX IF NOT EXISTS idx_archive_par_id   ON archives_at(archive_par_id);
CREATE INDEX IF NOT EXISTS idx_archive_hash     ON archives_at(hashsha256);
CREATE INDEX IF NOT EXISTS idx_archive_status   ON archives_at(archive_status);
CREATE INDEX IF NOT EXISTS idx_archive_deleted  ON archives_at(deleted);
CREATE INDEX IF NOT EXISTS idx_archive_date     ON archives_at(date_archivage);

-- Permissions Module 10
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, p.nom, p.description
FROM (VALUES
    ('EXPORT_PDF',       'Exporter une AT clôturée en PDF'),
    ('DOWNLOAD_ARCHIVE', 'Télécharger une archive PDF'),
    ('VIEW_ARCHIVE',     'Consulter les archives'),
    ('VERIFY_ARCHIVE',   'Vérifier l''intégrité d''une archive'),
    ('SEARCH_ARCHIVE',   'Rechercher dans les archives')
) AS p(nom, description)
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE nom = p.nom
);
