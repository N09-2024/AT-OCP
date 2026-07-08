-- V6__module_5_analyse_risque.sql
-- Ajout des colonnes dans la table analyses_risques pour lier à la visite et à l'analyseur

ALTER TABLE analyses_risques ADD COLUMN visite_prealable_id VARCHAR(255) UNIQUE;
ALTER TABLE analyses_risques ADD COLUMN analyseur_id VARCHAR(255);

ALTER TABLE analyses_risques ADD CONSTRAINT fk_analyse_visite
    FOREIGN KEY (visite_prealable_id) REFERENCES visites_prealables(id);
ALTER TABLE analyses_risques ADD CONSTRAINT fk_analyse_analyseur
    FOREIGN KEY (analyseur_id) REFERENCES utilisateurs(id);

-- Tables de liaison ManyToMany pour l'Analyse des Risques

CREATE TABLE IF NOT EXISTS analyse_risque_mesures (
    analyse_risque_id VARCHAR(255) NOT NULL,
    mesure_id         VARCHAR(255) NOT NULL,
    PRIMARY KEY (analyse_risque_id, mesure_id)
);
ALTER TABLE analyse_risque_mesures ADD CONSTRAINT fk_arm_analyse FOREIGN KEY (analyse_risque_id) REFERENCES analyses_risques(id);
ALTER TABLE analyse_risque_mesures ADD CONSTRAINT fk_arm_mesure  FOREIGN KEY (mesure_id) REFERENCES mesures_preparation(id);

CREATE TABLE IF NOT EXISTS analyse_risque_epis (
    analyse_risque_id VARCHAR(255) NOT NULL,
    epi_id            VARCHAR(255) NOT NULL,
    PRIMARY KEY (analyse_risque_id, epi_id)
);
ALTER TABLE analyse_risque_epis ADD CONSTRAINT fk_are_analyse FOREIGN KEY (analyse_risque_id) REFERENCES analyses_risques(id);
ALTER TABLE analyse_risque_epis ADD CONSTRAINT fk_are_epi     FOREIGN KEY (epi_id) REFERENCES epis(id);

CREATE TABLE IF NOT EXISTS analyse_risque_moyens_acces (
    analyse_risque_id VARCHAR(255) NOT NULL,
    moyen_id          VARCHAR(255) NOT NULL,
    PRIMARY KEY (analyse_risque_id, moyen_id)
);
ALTER TABLE analyse_risque_moyens_acces ADD CONSTRAINT fk_arma_analyse FOREIGN KEY (analyse_risque_id) REFERENCES analyses_risques(id);
ALTER TABLE analyse_risque_moyens_acces ADD CONSTRAINT fk_arma_moyen   FOREIGN KEY (moyen_id) REFERENCES moyens_acces(id);
