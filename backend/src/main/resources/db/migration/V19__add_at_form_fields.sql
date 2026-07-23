-- Add new string fields to autorisations_travail
ALTER TABLE autorisations_travail
    ADD COLUMN services_intervenants VARCHAR(255),
    ADD COLUMN entreprises_intervenantes VARCHAR(255),
    ADD COLUMN mesures_securite_executant TEXT;

-- Create join tables for referentials
CREATE TABLE at_risques (
    at_id VARCHAR(255) NOT NULL,
    risque_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (at_id, risque_id),
    FOREIGN KEY (at_id) REFERENCES autorisations_travail (id) ON DELETE CASCADE,
    FOREIGN KEY (risque_id) REFERENCES risques (id) ON DELETE CASCADE
);

CREATE TABLE at_mesures (
    at_id VARCHAR(255) NOT NULL,
    mesure_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (at_id, mesure_id),
    FOREIGN KEY (at_id) REFERENCES autorisations_travail (id) ON DELETE CASCADE,
    FOREIGN KEY (mesure_id) REFERENCES mesures_preparation (id) ON DELETE CASCADE
);

CREATE TABLE at_epis (
    at_id VARCHAR(255) NOT NULL,
    epi_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (at_id, epi_id),
    FOREIGN KEY (at_id) REFERENCES autorisations_travail (id) ON DELETE CASCADE,
    FOREIGN KEY (epi_id) REFERENCES epis (id) ON DELETE CASCADE
);

CREATE TABLE at_moyens_acces (
    at_id VARCHAR(255) NOT NULL,
    moyen_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (at_id, moyen_id),
    FOREIGN KEY (at_id) REFERENCES autorisations_travail (id) ON DELETE CASCADE,
    FOREIGN KEY (moyen_id) REFERENCES moyens_acces (id) ON DELETE CASCADE
);
