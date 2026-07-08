-- V7__module_6_autorisations_travail.sql
-- Insertion des règles métier de base pour le workflow des Autorisations de Travail

-- 1. BROUILLON -> SOUMISE (Soumission)
INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise)
VALUES (gen_random_uuid(), 'BROUILLON', 'SOUMISE', 'SOUMISSION', 'MANAGE_DOCUMENTS');

-- 2. SOUMISE -> VALIDEE (Validation)
INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise)
VALUES (gen_random_uuid(), 'SOUMISE', 'VALIDEE', 'VALIDATION', 'VALIDATE_AT');

-- 3. SOUMISE -> REJETEE (Refus)
INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise)
VALUES (gen_random_uuid(), 'SOUMISE', 'REJETEE', 'REFUS', 'VALIDATE_AT');

-- 4. VALIDEE -> SOUMISE (Renouvellement)
INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise)
VALUES (gen_random_uuid(), 'VALIDEE', 'SOUMISE', 'RENOUVELLEMENT', 'MANAGE_DOCUMENTS');

-- 5. VALIDEE -> CLOTUREE (Clôture / Réception des travaux)
INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise)
VALUES (gen_random_uuid(), 'VALIDEE', 'CLOTUREE', 'CLOTURE', 'CLOSE_AT');
