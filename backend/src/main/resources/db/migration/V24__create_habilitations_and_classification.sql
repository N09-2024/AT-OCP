-- ============================================================
-- V24 - Habilitations AT (F-HSE-SEC-31-02) + Classifications (§6)
-- Standard OCP S-HSE-SEC-31 v1.0
-- ============================================================

-- -----------------------------------------------
-- Table : habilitations (Formulaire F-HSE-SEC-31-02)
-- Désignation officielle des agents habilités par HCEP à délivrer des AT
-- -----------------------------------------------
CREATE TABLE habilitations (
    id                  VARCHAR(255) NOT NULL,
    utilisateur_id      VARCHAR(255) NOT NULL,
    designe_par_id      VARCHAR(255),
    date_habilitation   DATE         NOT NULL DEFAULT CURRENT_DATE,
    valide_jusqu_au     DATE         NOT NULL DEFAULT (CURRENT_DATE + INTERVAL '1 year'),
    actif               BOOLEAN      NOT NULL DEFAULT TRUE,
    observations        TEXT,
    date_creation       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_hab_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    CONSTRAINT fk_hab_designe_par FOREIGN KEY (designe_par_id) REFERENCES utilisateurs(id),
    CONSTRAINT uq_habilitation_utilisateur UNIQUE (utilisateur_id)
);

-- -----------------------------------------------
-- Table : classifications_interventions (§6 Standard OCP S-HSE-SEC-31)
-- Décision HCEP : Niveau 1 (pas d'AT) ou Niveau 2 (AT obligatoire)
-- -----------------------------------------------
CREATE TABLE classifications_interventions (
    id                  VARCHAR(255) NOT NULL,
    reference           VARCHAR(100) NOT NULL UNIQUE,
    niveau              VARCHAR(20)  NOT NULL CHECK (niveau IN ('NIVEAU_1', 'NIVEAU_2')),
    est_tiers           BOOLEAN      NOT NULL DEFAULT FALSE,
    nature_intervention TEXT,
    zone_id             VARCHAR(255),
    service_id          VARCHAR(255),
    classifie_par_id    VARCHAR(255),
    date_classification TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observations        TEXT,
    statut              VARCHAR(50)  NOT NULL DEFAULT 'EFFECTUEE',
    -- Si Niveau 2 → AT liée créée
    autorisation_travail_id VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_classif_zone       FOREIGN KEY (zone_id)     REFERENCES zones(id),
    CONSTRAINT fk_classif_service    FOREIGN KEY (service_id)  REFERENCES services(id),
    CONSTRAINT fk_classif_par        FOREIGN KEY (classifie_par_id) REFERENCES utilisateurs(id),
    CONSTRAINT fk_classif_at         FOREIGN KEY (autorisation_travail_id) REFERENCES autorisations_travail(id)
);

-- Indexes
CREATE INDEX idx_hab_utilisateur ON habilitations(utilisateur_id);
CREATE INDEX idx_classif_niveau  ON classifications_interventions(niveau);
CREATE INDEX idx_classif_date    ON classifications_interventions(date_classification);
CREATE INDEX idx_classif_par     ON classifications_interventions(classifie_par_id);
