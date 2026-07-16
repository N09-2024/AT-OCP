-- ============================================================
-- V15__convert_type_permis_to_entity.sql
-- Conversion de l'enum TypePermis en entité gérée en BDD
-- ============================================================

-- 1. Création de la table types_permis
CREATE TABLE types_permis (
    id VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    PRIMARY KEY (id)
);

-- 2. Insertion des données par défaut basées sur l'ancien enum
INSERT INTO types_permis (id, nom, description) VALUES
    ('tp_feu', 'FEU', 'Permis de feu'),
    ('tp_fouille', 'FOUILLE', 'Permis de fouille'),
    ('tp_hauteur', 'TRAVAIL_HAUTEUR', 'Permis pour travail en hauteur'),
    ('tp_confine', 'ESPACE_CONFINE', 'Permis pour espace confiné'),
    ('tp_consignation', 'CONSIGNATION', 'Plan de consignation');

-- 3. Ajout de la colonne type_permis_id à la table permis
ALTER TABLE permis ADD COLUMN type_permis_id VARCHAR(255);

-- 4. Migration des données existantes (le cas échéant)
UPDATE permis p
SET type_permis_id = tp.id
FROM types_permis tp
WHERE p.type = tp.nom;

-- 5. Ajouter la contrainte de clé étrangère
ALTER TABLE permis ADD CONSTRAINT fk_permis_type_permis FOREIGN KEY (type_permis_id) REFERENCES types_permis;

-- 6. Supprimer l'ancienne colonne type
ALTER TABLE permis DROP COLUMN type;
