-- V31 : stocker les cases du formulaire F-HSE en JSON (fiable, sans jointures Hibernate)
ALTER TABLE autorisations_travail
    ADD COLUMN IF NOT EXISTS form_risques_ids TEXT,
    ADD COLUMN IF NOT EXISTS form_mesures_ids TEXT,
    ADD COLUMN IF NOT EXISTS form_epis_ids TEXT,
    ADD COLUMN IF NOT EXISTS form_moyens_ids TEXT,
    ADD COLUMN IF NOT EXISTS form_permis_ids TEXT;
