-- ============================================================
-- V12__flyway_schema_cleanup_and_indexes.sql
-- Index de performance pour toutes les tables
-- ============================================================

-- autorisations_travail
CREATE INDEX IF NOT EXISTS idx_at_statut       ON autorisations_travail(statut);
CREATE INDEX IF NOT EXISTS idx_at_numero       ON autorisations_travail(numero);
CREATE INDEX IF NOT EXISTS idx_at_proprietaire ON autorisations_travail(proprietaire_brouillon_id);
CREATE INDEX IF NOT EXISTS idx_at_date_debut   ON autorisations_travail(date_debut);
CREATE INDEX IF NOT EXISTS idx_at_date_fin     ON autorisations_travail(date_fin);
CREATE INDEX IF NOT EXISTS idx_at_verrou       ON autorisations_travail(etat_verrou);

-- visas
CREATE INDEX IF NOT EXISTS idx_visa_at          ON visas(at_id);
CREATE INDEX IF NOT EXISTS idx_visa_utilisateur ON visas(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_visa_statut      ON visas(statut);
CREATE INDEX IF NOT EXISTS idx_visa_date        ON visas(date_visa);

-- permis
CREATE INDEX IF NOT EXISTS idx_permis_at           ON permis(at_id);
CREATE INDEX IF NOT EXISTS idx_permis_type         ON permis(type);
CREATE INDEX IF NOT EXISTS idx_permis_statut_verif ON permis(statut_verification);
CREATE INDEX IF NOT EXISTS idx_permis_expiration   ON permis(date_expiration);

-- historiques_at
CREATE INDEX IF NOT EXISTS idx_historique_at     ON historiques_at(at_id);
CREATE INDEX IF NOT EXISTS idx_historique_date   ON historiques_at(date_action);
CREATE INDEX IF NOT EXISTS idx_historique_action ON historiques_at(action);

-- notifications
CREATE INDEX IF NOT EXISTS idx_notification_utilisateur ON notifications(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_notification_lu          ON notifications(lu);
CREATE INDEX IF NOT EXISTS idx_notification_date        ON notifications(date_creation);

-- audit_logs
CREATE INDEX IF NOT EXISTS idx_audit_utilisateur ON audit_logs(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_audit_date        ON audit_logs(date);
CREATE INDEX IF NOT EXISTS idx_audit_action      ON audit_logs(action);

-- utilisateurs
CREATE INDEX IF NOT EXISTS idx_utilisateur_email    ON utilisateurs(email);
CREATE INDEX IF NOT EXISTS idx_utilisateur_matricule ON utilisateurs(matricule);
CREATE INDEX IF NOT EXISTS idx_utilisateur_actif     ON utilisateurs(actif);

-- receptions_travaux
CREATE INDEX IF NOT EXISTS idx_reception_at          ON receptions_travaux(autorisation_travail_id);
CREATE INDEX IF NOT EXISTS idx_reception_responsable ON receptions_travaux(responsable_id);
CREATE INDEX IF NOT EXISTS idx_reception_date        ON receptions_travaux(date_reception);

-- documents sources
CREATE INDEX IF NOT EXISTS idx_di_statut ON demandes_intervention(statut);
CREATE INDEX IF NOT EXISTS idx_ot_statut ON ordres_travail(statut);
CREATE INDEX IF NOT EXISTS idx_bt_statut ON bons_travail(statut);

-- refresh_tokens
CREATE INDEX IF NOT EXISTS idx_rt_utilisateur ON refresh_tokens(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_rt_token       ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_rt_revoked     ON refresh_tokens(revoked);
