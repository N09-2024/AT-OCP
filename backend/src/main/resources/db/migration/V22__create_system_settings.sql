-- ============================================================
-- V22 — Création de la table system_settings
-- Requise par l'entité SystemSetting.java (hibernate validate)
-- ============================================================

CREATE TABLE IF NOT EXISTS system_settings (
    setting_key   VARCHAR(255) NOT NULL PRIMARY KEY,
    setting_value TEXT
);

-- Valeurs initiales : paramétrage par défaut de l'application
INSERT INTO system_settings (setting_key, setting_value)
VALUES
    ('app.version',                '1.0.0'),
    ('at.duree_validite_defaut',   '8'),
    ('at.niveau_par_defaut',       '1'),
    ('notification.email.enabled', 'false')
ON CONFLICT (setting_key) DO NOTHING;
