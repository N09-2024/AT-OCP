-- Module 9 : Réception des Travaux
-- Migration Flyway V10

-- ============================================================
-- TABLE : receptions_travaux
-- ============================================================
CREATE TABLE receptions_travaux (
    id                        VARCHAR(36)  NOT NULL,
    autorisation_travail_id   VARCHAR(36)  NOT NULL,
    responsable_id            VARCHAR(36),
    date_reception            TIMESTAMP,
    date_debut_travaux_reelle TIMESTAMP,
    date_fin_travaux_reelle   TIMESTAMP,
    travaux_realises          TEXT,
    travaux_conformes         BOOLEAN      NOT NULL DEFAULT FALSE,
    equipement_remis_en_service BOOLEAN    NOT NULL DEFAULT FALSE,
    zone_nettoyee             BOOLEAN      NOT NULL DEFAULT FALSE,
    consignation_retiree      BOOLEAN      NOT NULL DEFAULT FALSE,
    essais_effectues          BOOLEAN      NOT NULL DEFAULT FALSE,
    resultat_essais           TEXT,
    observations              TEXT,
    commentaire_responsable   TEXT,
    signature_responsable     VARCHAR(500),
    date_signature            TIMESTAMP,
    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_receptions_travaux PRIMARY KEY (id),
    CONSTRAINT fk_reception_at FOREIGN KEY (autorisation_travail_id)
        REFERENCES autorisations_travail(id) ON DELETE CASCADE,
    CONSTRAINT fk_reception_responsable FOREIGN KEY (responsable_id)
        REFERENCES utilisateurs(id) ON DELETE SET NULL,
    CONSTRAINT uq_reception_at UNIQUE (autorisation_travail_id)
);

CREATE INDEX idx_reception_at ON receptions_travaux(autorisation_travail_id);
CREATE INDEX idx_reception_responsable ON receptions_travaux(responsable_id);

-- ============================================================
-- TABLE : historiques_reception
-- ============================================================
CREATE TABLE historiques_reception (
    id                        VARCHAR(36)  NOT NULL,
    reception_travaux_id      VARCHAR(36)  NOT NULL,
    utilisateur_id           VARCHAR(36),
    date_action               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action                    VARCHAR(100) NOT NULL,
    commentaire               TEXT,
    CONSTRAINT pk_historiques_reception PRIMARY KEY (id),
    CONSTRAINT fk_historique_reception FOREIGN KEY (reception_travaux_id)
        REFERENCES receptions_travaux(id) ON DELETE CASCADE,
    CONSTRAINT fk_historique_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateurs(id) ON DELETE SET NULL
);

CREATE INDEX idx_historique_reception ON historiques_reception(reception_travaux_id);
CREATE INDEX idx_historique_utilisateur ON historiques_reception(utilisateur_id);

-- ============================================================
-- TABLE : photos_reception
-- ============================================================
CREATE TABLE photos_reception (
    id                        VARCHAR(36)  NOT NULL,
    reception_travaux_id      VARCHAR(36)  NOT NULL,
    nom                       VARCHAR(255) NOT NULL,
    path                      VARCHAR(500) NOT NULL,
    taille                    BIGINT,
    mime_type                 VARCHAR(100),
    ordre                     INTEGER,
    legende                   TEXT,
    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_photos_reception PRIMARY KEY (id),
    CONSTRAINT fk_photo_reception FOREIGN KEY (reception_travaux_id)
        REFERENCES receptions_travaux(id) ON DELETE CASCADE
);

CREATE INDEX idx_photo_reception ON photos_reception(reception_travaux_id);

-- ============================================================
-- Nouvelles permissions Module 9
-- ============================================================
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, p.nom, p.description
FROM (VALUES
    ('CREATE_RECEPTION',   'Créer une réception des travaux'),
    ('EDIT_RECEPTION',     'Modifier une réception des travaux'),
    ('DELETE_RECEPTION',   'Supprimer une réception des travaux'),
    ('VIEW_RECEPTION',     'Consulter les réceptions des travaux'),
    ('SIGN_RECEPTION',     'Signer une réception des travaux'),
    ('CLOSE_AT',           'Clôturer une AT suite à réception')
) AS p(nom, description)
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE nom = p.nom
);
