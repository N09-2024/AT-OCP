-- Module 9 : Réception des Travaux
-- Migration Flyway V10

-- ============================================================
-- TABLE : receptions_travaux
-- ============================================================
CREATE TABLE receptions_travaux (
    id                        VARCHAR(36)  NOT NULL,
    autorisation_travail_id   VARCHAR(36)  NOT NULL,
    date_reception            TIMESTAMP,
    commentaire               TEXT,
    travaux_conformes         BOOLEAN      NOT NULL DEFAULT FALSE,
    installation_remise_en_etat BOOLEAN    NOT NULL DEFAULT FALSE,
    essais_effectues          BOOLEAN      NOT NULL DEFAULT FALSE,
    essais_conformes          BOOLEAN      NOT NULL DEFAULT FALSE,
    date_validation           TIMESTAMP,
    validee                   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by                VARCHAR(36),
    created_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_receptions_travaux PRIMARY KEY (id),
    CONSTRAINT fk_reception_at FOREIGN KEY (autorisation_travail_id)
        REFERENCES autorisations_travail(id) ON DELETE CASCADE,
    CONSTRAINT uq_reception_at UNIQUE (autorisation_travail_id)
);

CREATE INDEX idx_reception_at ON receptions_travaux(autorisation_travail_id);

-- ============================================================
-- TABLE : essais
-- ============================================================
CREATE TABLE essais (
    id              VARCHAR(36)  NOT NULL,
    reception_id    VARCHAR(36)  NOT NULL,
    nom             VARCHAR(255) NOT NULL,
    description     TEXT,
    resultat        VARCHAR(500),
    conforme        BOOLEAN      NOT NULL DEFAULT FALSE,
    commentaire     TEXT,
    CONSTRAINT pk_essais PRIMARY KEY (id),
    CONSTRAINT fk_essai_reception FOREIGN KEY (reception_id)
        REFERENCES receptions_travaux(id) ON DELETE CASCADE
);

CREATE INDEX idx_essai_reception ON essais(reception_id);

-- ============================================================
-- TABLE : remises_etat
-- ============================================================
CREATE TABLE remises_etat (
    id                     VARCHAR(36)  NOT NULL,
    reception_id           VARCHAR(36)  NOT NULL,
    zone_nettoyee          BOOLEAN      NOT NULL DEFAULT FALSE,
    materiel_retire        BOOLEAN      NOT NULL DEFAULT FALSE,
    protections_retirees   BOOLEAN      NOT NULL DEFAULT FALSE,
    consignation_retiree   BOOLEAN      NOT NULL DEFAULT FALSE,
    commentaire            TEXT,
    CONSTRAINT pk_remises_etat PRIMARY KEY (id),
    CONSTRAINT fk_remise_reception FOREIGN KEY (reception_id)
        REFERENCES receptions_travaux(id) ON DELETE CASCADE,
    CONSTRAINT uq_remise_reception UNIQUE (reception_id)
);

CREATE INDEX idx_remise_reception ON remises_etat(reception_id);

-- ============================================================
-- Nouvelles permissions Module 9
-- ============================================================
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, p.nom, p.description
FROM (VALUES
    ('CREATE_RECEPTION',   'Créer une réception des travaux'),
    ('EDIT_RECEPTION',     'Modifier une réception des travaux'),
    ('VALIDATE_RECEPTION', 'Valider une réception des travaux'),
    ('DELETE_RECEPTION',   'Supprimer une réception des travaux'),
    ('VIEW_RECEPTION',     'Consulter les réceptions des travaux')
) AS p(nom, description)
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE nom = p.nom
);

-- Nouvelles valeurs TypeActionAT (enum en Java, pas besoin de migration SQL)
