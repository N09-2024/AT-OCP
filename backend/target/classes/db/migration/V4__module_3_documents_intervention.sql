-- ============================================================
-- V4 : Module 3 — Documents d'Intervention (DI / OT / BT)
-- Corrige le schéma V1 pour respecter le processus métier OCP
-- ============================================================

-- ====================================================
-- 1. Séquences de numérotation automatique (reset annuel)
-- ====================================================
CREATE SEQUENCE IF NOT EXISTS seq_di_2026 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_ot_2026 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_bt_2026 START 1 INCREMENT 1;

-- Séquences pour les années futures (prévision 5 ans)
CREATE SEQUENCE IF NOT EXISTS seq_di_2027 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_ot_2027 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_bt_2027 START 1 INCREMENT 1;

CREATE SEQUENCE IF NOT EXISTS seq_di_2028 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_ot_2028 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_bt_2028 START 1 INCREMENT 1;

CREATE SEQUENCE IF NOT EXISTS seq_di_2029 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_ot_2029 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_bt_2029 START 1 INCREMENT 1;

CREATE SEQUENCE IF NOT EXISTS seq_di_2030 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_ot_2030 START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS seq_bt_2030 START 1 INCREMENT 1;

-- ====================================================
-- 2. Enrichissement de la table demandes_intervention
-- ====================================================
ALTER TABLE demandes_intervention
    ADD COLUMN IF NOT EXISTS numero            VARCHAR(30) UNIQUE,
    ADD COLUMN IF NOT EXISTS demandeur_id      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS installation_id   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS equipement_id     VARCHAR(255),
    ADD COLUMN IF NOT EXISTS visite_prealable_id VARCHAR(255) UNIQUE;

-- Convertir la colonne statut vers l'enum StatutDocument
ALTER TABLE demandes_intervention
    ALTER COLUMN statut TYPE VARCHAR(50);

-- FK demandeur → utilisateurs
ALTER TABLE demandes_intervention
    ADD CONSTRAINT fk_di_demandeur
    FOREIGN KEY (demandeur_id) REFERENCES utilisateurs(id);

-- FK installation → installations
ALTER TABLE demandes_intervention
    ADD CONSTRAINT fk_di_installation
    FOREIGN KEY (installation_id) REFERENCES installations(id);

-- FK equipement → equipements
ALTER TABLE demandes_intervention
    ADD CONSTRAINT fk_di_equipement
    FOREIGN KEY (equipement_id) REFERENCES equipements(id);

-- FK visite_prealable → visites_prealables
ALTER TABLE demandes_intervention
    ADD CONSTRAINT fk_di_visite_prealable
    FOREIGN KEY (visite_prealable_id) REFERENCES visites_prealables(id);

-- ====================================================
-- 3. Enrichissement de la table ordres_travail
-- ====================================================
ALTER TABLE ordres_travail
    ADD COLUMN IF NOT EXISTS numero               VARCHAR(30) UNIQUE,
    ADD COLUMN IF NOT EXISTS objet                VARCHAR(500),
    ADD COLUMN IF NOT EXISTS description          TEXT,
    ADD COLUMN IF NOT EXISTS demandeur_id         VARCHAR(255),
    ADD COLUMN IF NOT EXISTS installation_id      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS visite_prealable_id  VARCHAR(255) UNIQUE;

ALTER TABLE ordres_travail
    ALTER COLUMN statut TYPE VARCHAR(50);

ALTER TABLE ordres_travail
    ADD CONSTRAINT fk_ot_demandeur
    FOREIGN KEY (demandeur_id) REFERENCES utilisateurs(id);

ALTER TABLE ordres_travail
    ADD CONSTRAINT fk_ot_installation
    FOREIGN KEY (installation_id) REFERENCES installations(id);

ALTER TABLE ordres_travail
    ADD CONSTRAINT fk_ot_visite_prealable
    FOREIGN KEY (visite_prealable_id) REFERENCES visites_prealables(id);

-- ====================================================
-- 4. Enrichissement de la table bons_travail
-- ====================================================
ALTER TABLE bons_travail
    ADD COLUMN IF NOT EXISTS numero                 VARCHAR(30) UNIQUE,
    ADD COLUMN IF NOT EXISTS description            TEXT,
    ADD COLUMN IF NOT EXISTS entreprise_externe_id  VARCHAR(255) NOT NULL DEFAULT 'INCONNU',
    ADD COLUMN IF NOT EXISTS demandeur_id           VARCHAR(255),
    ADD COLUMN IF NOT EXISTS installation_id        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS visite_prealable_id    VARCHAR(255) UNIQUE;

-- Retirer la valeur DEFAULT temporaire maintenant que la colonne existe
ALTER TABLE bons_travail
    ALTER COLUMN entreprise_externe_id DROP DEFAULT;

ALTER TABLE bons_travail
    ALTER COLUMN statut TYPE VARCHAR(50);

ALTER TABLE bons_travail
    ADD CONSTRAINT fk_bt_entreprise_externe
    FOREIGN KEY (entreprise_externe_id) REFERENCES entreprises_externes(id);

ALTER TABLE bons_travail
    ADD CONSTRAINT fk_bt_demandeur
    FOREIGN KEY (demandeur_id) REFERENCES utilisateurs(id);

ALTER TABLE bons_travail
    ADD CONSTRAINT fk_bt_installation
    FOREIGN KEY (installation_id) REFERENCES installations(id);

ALTER TABLE bons_travail
    ADD CONSTRAINT fk_bt_visite_prealable
    FOREIGN KEY (visite_prealable_id) REFERENCES visites_prealables(id);

-- ====================================================
-- 5. Correction architecturale : VisitePrealable
--    La visite est liée au document (DI/OT/BT), pas à l'AT
--    On supprime le lien AT → VisitePrealable (il est maintenant inversé)
-- ====================================================

-- Enrichissement de visites_prealables avec visiteur et risques identifiés
ALTER TABLE visites_prealables
    ADD COLUMN IF NOT EXISTS visiteur_id  VARCHAR(255);

ALTER TABLE visites_prealables
    ADD CONSTRAINT fk_vp_visiteur
    FOREIGN KEY (visiteur_id) REFERENCES utilisateurs(id);

-- Table de jonction : risques identifiés lors de la visite
CREATE TABLE IF NOT EXISTS visite_prealable_risques (
    visite_prealable_id  VARCHAR(255) NOT NULL,
    risque_id            VARCHAR(255) NOT NULL,
    PRIMARY KEY (visite_prealable_id, risque_id),
    FOREIGN KEY (visite_prealable_id) REFERENCES visites_prealables(id),
    FOREIGN KEY (risque_id)           REFERENCES risques(id)
);

-- Suppression du lien AT → VisitePrealable (remplacé par DI/OT/BT → VisitePrealable)
ALTER TABLE autorisations_travail
    DROP CONSTRAINT IF EXISTS FKks9lysruo2e44ql8hxs03c84d;

ALTER TABLE autorisations_travail
    DROP COLUMN IF EXISTS visite_prealable_id;

-- ====================================================
-- 6. Correction architecturale : AnalyseRisque
--    L'analyse est liée à la VisitePrealable (One-to-One)
-- ====================================================
ALTER TABLE analyses_risques
    ADD COLUMN IF NOT EXISTS visite_prealable_id  VARCHAR(255) UNIQUE,
    ADD COLUMN IF NOT EXISTS analyseur_id         VARCHAR(255);

ALTER TABLE analyses_risques
    ADD CONSTRAINT fk_ar_visite_prealable
    FOREIGN KEY (visite_prealable_id) REFERENCES visites_prealables(id);

ALTER TABLE analyses_risques
    ADD CONSTRAINT fk_ar_analyseur
    FOREIGN KEY (analyseur_id) REFERENCES utilisateurs(id);

-- Suppression du lien AT → AnalyseRisque (remplacé par VisitePrealable → AnalyseRisque)
ALTER TABLE autorisations_travail
    DROP CONSTRAINT IF EXISTS FKauada9m4kokbruhss81d01uy8;

ALTER TABLE autorisations_travail
    DROP COLUMN IF EXISTS analyse_risque_id;

-- ====================================================
-- 7. Ajout de la permission MANAGE_DOCUMENTS
-- ====================================================
INSERT INTO permissions (id, nom, description)
VALUES (
    gen_random_uuid()::text,
    'MANAGE_DOCUMENTS',
    'Créer, modifier et gérer les documents d''intervention (DI, OT, BT)'
) ON CONFLICT (nom) DO NOTHING;
