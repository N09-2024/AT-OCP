-- ============================================================
-- V38 - Suppression du référentiel installations (jamais peuplé)
--   - La notion de localisation est couverte par Zone
--   - installationRemiseEnEtat (receptions_travaux) est conservée
-- ============================================================

-- 1. Supprimer les contraintes FK
ALTER TABLE IF EXISTS equipements DROP CONSTRAINT IF EXISTS fk_equipements_installation;
ALTER TABLE IF EXISTS demandes_intervention DROP CONSTRAINT IF EXISTS fk_di_installation;
ALTER TABLE IF EXISTS ordres_travail DROP CONSTRAINT IF EXISTS fk_ot_installation;
ALTER TABLE IF EXISTS bons_travail DROP CONSTRAINT IF EXISTS fk_bt_installation;
ALTER TABLE IF EXISTS installations DROP CONSTRAINT IF EXISTS fk_installations_service;

-- 2. Supprimer les colonnes installation_id (nullable, jamais peuplées)
ALTER TABLE IF EXISTS equipements DROP COLUMN IF EXISTS installation_id;
ALTER TABLE IF EXISTS demandes_intervention DROP COLUMN IF EXISTS installation_id;
ALTER TABLE IF EXISTS ordres_travail DROP COLUMN IF EXISTS installation_id;
ALTER TABLE IF EXISTS bons_travail DROP COLUMN IF EXISTS installation_id;

-- 3. Supprimer la table installations
DROP TABLE IF EXISTS installations;
