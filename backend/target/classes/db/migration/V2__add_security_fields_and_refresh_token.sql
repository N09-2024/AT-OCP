-- V2: Add refresh_tokens table and extend utilisateurs table
-- Add security columns to utilisateurs
ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS date_creation TIMESTAMP DEFAULT NOW() NOT NULL,
    ADD COLUMN IF NOT EXISTS date_modification TIMESTAMP,
    ADD COLUMN IF NOT EXISTS derniere_connexion TIMESTAMP,
    ADD COLUMN IF NOT EXISTS compteur_echecs_connexion INTEGER DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS compte_verrouille BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN IF NOT EXISTS mot_de_passe_expire BOOLEAN DEFAULT FALSE NOT NULL;

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id VARCHAR(255) NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    adresse_ip VARCHAR(255),
    user_agent VARCHAR(512),
    revoked BOOLEAN DEFAULT FALSE NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_utilisateur ON refresh_tokens(utilisateur_id);
