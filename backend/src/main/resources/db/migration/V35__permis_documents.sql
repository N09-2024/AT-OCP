-- V35: Table permis_documents pour validation IA des permis section E
CREATE TABLE IF NOT EXISTS permis_documents (
    id                    VARCHAR(36)  NOT NULL PRIMARY KEY,
    autorisation_travail_id VARCHAR(36) NOT NULL REFERENCES autorisations_travail(id) ON DELETE CASCADE,
    type_permis_attendu   VARCHAR(80)  NOT NULL,
    file_path             VARCHAR(500),
    file_original_name    VARCHAR(255),
    file_content_type     VARCHAR(60),
    statut                VARCHAR(30)  NOT NULL DEFAULT 'EN_ATTENTE_UPLOAD',
    date_upload           TIMESTAMP,
    date_analyse          TIMESTAMP,
    type_extrait          VARCHAR(120),
    date_debut_extrait    VARCHAR(20),
    date_fin_extrait      VARCHAR(20),
    responsables_extraits TEXT,
    motif_rejet           TEXT,
    score_confiance       DOUBLE PRECISION,
    commentaire_ia        TEXT
);

CREATE INDEX IF NOT EXISTS idx_permis_documents_at_id
    ON permis_documents(autorisation_travail_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_permis_documents_at_type
    ON permis_documents(autorisation_travail_id, type_permis_attendu);
