-- ============================================================
-- V26__standard_workflow_transitions.sql
-- Transitions workflow conformes au Standard S-HSE-SEC-31 §7
-- ============================================================

-- Supprimer les contraintes CHECK legacy sur etat_depart / etat_arrivee
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        WHERE rel.relname = 'workflows_at'
          AND con.contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE workflows_at DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;

-- Désactiver les anciennes transitions legacy
UPDATE workflows_at SET actif = false WHERE actif = true;

-- Insérer les transitions standard S-HSE-SEC-31
-- role_autorise = permission (hasAuthority), pas le nom de rôle

INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise, validation_obligatoire, actif)
VALUES
-- Étape 0 → 1 : Classification Niveau 2 → demande
(gen_random_uuid()::text, 'CLASSIFICATION_EFFECTUEE', 'DEMANDE_CREEE', 'CREATION_DEMANDE', 'CREATE_AT', false, true),

-- Étape 1 → 2 : Visite chantier
(gen_random_uuid()::text, 'DEMANDE_CREEE', 'VISITE_REALISEE', 'VISITE_CHANTIER', 'CREATE_VISITE', false, true),

-- Étape 2 → 3 : Rédaction AT + permis (sur terrain)
(gen_random_uuid()::text, 'VISITE_REALISEE', 'AT_REDIGEE', 'REDACTION_AT', 'SIGN_AT', true, true),

-- Étape 3 → 4 : Début intervention
(gen_random_uuid()::text, 'AT_REDIGEE', 'INTERVENTION_EN_COURS', 'DEBUT_INTERVENTION', 'START_INTERVENTION', false, true),

-- Étape 4 → 5b : Dépassement d'un poste → reconduction
(gen_random_uuid()::text, 'INTERVENTION_EN_COURS', 'AT_RECONDUITE', 'RECONDUCTION', 'RENEW_AT', true, true),

-- Étape 5b → 4 : Reprise après reconduction (≤ 24h)
(gen_random_uuid()::text, 'AT_RECONDUITE', 'INTERVENTION_EN_COURS', 'DEBUT_INTERVENTION', 'START_INTERVENTION', false, true),

-- Étape 5b → 2 : Dépassement 24h → nouvelle visite obligatoire
(gen_random_uuid()::text, 'AT_RECONDUITE', 'VISITE_REALISEE', 'VISITE_CHANTIER', 'CREATE_VISITE', false, true),

-- Étape 4 → 2 : Incident / changement de condition
(gen_random_uuid()::text, 'INTERVENTION_EN_COURS', 'VISITE_REALISEE', 'VISITE_CHANTIER', 'CREATE_VISITE', false, true),

-- Étape 4 → 6 : Déclaration fin (sans dépassement poste prolongé)
(gen_random_uuid()::text, 'INTERVENTION_EN_COURS', 'FIN_TRAVAUX_DECLAREE', 'DECLARATION_FIN', 'DECLARE_FIN_TRAVAUX', false, true),

-- Étape 5b → 6 : Déclaration fin après reconduction
(gen_random_uuid()::text, 'AT_RECONDUITE', 'FIN_TRAVAUX_DECLAREE', 'DECLARATION_FIN', 'DECLARE_FIN_TRAVAUX', false, true),

-- Étape 6 → 7 : Réception conjointe
(gen_random_uuid()::text, 'FIN_TRAVAUX_DECLAREE', 'TRAVAUX_RECEPTIONES', 'RECEPTION_CONJOINTE', 'RECEIVE_AT', true, true),

-- Étape 7 → 8 : Archivage
(gen_random_uuid()::text, 'TRAVAUX_RECEPTIONES', 'ARCHIVEE', 'ARCHIVAGE_OFFICIEL', 'ARCHIVE_AT', false, true),

-- Exceptions
(gen_random_uuid()::text, 'VISITE_REALISEE', 'REJETEE', 'REFUS', 'REJECT_AT', false, true),
(gen_random_uuid()::text, 'AT_REDIGEE', 'REJETEE', 'REFUS', 'REJECT_AT', false, true),
(gen_random_uuid()::text, 'DEMANDE_CREEE', 'ANNULEE', 'ANNULATION', 'REJECT_AT', false, true),
(gen_random_uuid()::text, 'VISITE_REALISEE', 'ANNULEE', 'ANNULATION', 'REJECT_AT', false, true),
(gen_random_uuid()::text, 'AT_REDIGEE', 'ANNULEE', 'ANNULATION', 'REJECT_AT', false, true),

-- Compatibilité legacy (pont vers le standard)
(gen_random_uuid()::text, 'BROUILLON', 'DEMANDE_CREEE', 'CREATION_DEMANDE', 'CREATE_AT', false, true),
(gen_random_uuid()::text, 'BROUILLON', 'SOUMISE', 'SOUMISSION', 'SUBMIT_AT', false, true),
(gen_random_uuid()::text, 'SOUMISE', 'AT_REDIGEE', 'VALIDATION', 'VALIDATE_AT', true, true),
(gen_random_uuid()::text, 'SOUMISE', 'VISITE_REALISEE', 'VISITE_CHANTIER', 'CREATE_VISITE', false, true),
(gen_random_uuid()::text, 'SOUMISE', 'REJETEE', 'REFUS', 'REJECT_AT', false, true),
(gen_random_uuid()::text, 'VALIDEE', 'INTERVENTION_EN_COURS', 'DEBUT_INTERVENTION', 'START_INTERVENTION', false, true),
(gen_random_uuid()::text, 'VALIDEE', 'AT_REDIGEE', 'REDACTION_AT', 'SIGN_AT', false, true),
(gen_random_uuid()::text, 'VALIDEE', 'AT_RECONDUITE', 'RECONDUCTION', 'RENEW_AT', true, true),
(gen_random_uuid()::text, 'RENOUVELEE', 'INTERVENTION_EN_COURS', 'DEBUT_INTERVENTION', 'START_INTERVENTION', false, true),
(gen_random_uuid()::text, 'CLOTUREE', 'TRAVAUX_RECEPTIONES', 'RECEPTION_CONJOINTE', 'RECEIVE_AT', false, true),
(gen_random_uuid()::text, 'CLOTUREE', 'ARCHIVEE', 'ARCHIVAGE_OFFICIEL', 'ARCHIVE_AT', false, true);
