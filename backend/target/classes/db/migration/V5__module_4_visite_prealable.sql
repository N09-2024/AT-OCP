-- V5__module_4_visite_prealable.sql

-- 1. Ajout de la relation vers l'utilisateur (visiteur) dans visites_prealables
ALTER TABLE visites_prealables ADD COLUMN visiteur_id VARCHAR(255);
ALTER TABLE visites_prealables ADD CONSTRAINT fk_visite_visiteur FOREIGN KEY (visiteur_id) REFERENCES utilisateurs(id);

-- 2. Création de la table de liaison ManyToMany pour les risques identifiés
CREATE TABLE visite_prealable_risques (
    visite_prealable_id VARCHAR(255) NOT NULL,
    risque_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (visite_prealable_id, risque_id)
);

ALTER TABLE visite_prealable_risques ADD CONSTRAINT fk_visite_risque_v FOREIGN KEY (visite_prealable_id) REFERENCES visites_prealables(id);
ALTER TABLE visite_prealable_risques ADD CONSTRAINT fk_visite_risque_r FOREIGN KEY (risque_id) REFERENCES risques(id);

-- Note: La table photos existe déjà depuis V1
