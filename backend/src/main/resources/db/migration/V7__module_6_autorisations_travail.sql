-- ============================================================
-- V7__module_6_autorisations_travail.sql
-- Insertion des règles de workflow de base
-- ============================================================

INSERT INTO workflows_at (id, etat_depart, etat_arrivee, action, role_autorise, actif)
VALUES
    (gen_random_uuid(), 'BROUILLON', 'SOUMISE',  'SOUMISSION',     'MANAGE_DOCUMENTS', true),
    (gen_random_uuid(), 'SOUMISE',   'VALIDEE',   'VALIDATION',     'VALIDATE_AT',      true),
    (gen_random_uuid(), 'SOUMISE',   'REJETEE',   'REFUS',          'VALIDATE_AT',      true),
    (gen_random_uuid(), 'REJETEE',   'BROUILLON', 'MODIFICATION',   'MANAGE_DOCUMENTS', true),
    (gen_random_uuid(), 'VALIDEE',   'SOUMISE',   'RENOUVELLEMENT', 'MANAGE_DOCUMENTS', true),
    (gen_random_uuid(), 'VALIDEE',   'CLOTUREE',  'CLOTURE',        'CLOSE_AT',         true),
    (gen_random_uuid(), 'VALIDEE',   'ANNULEE',   'ANNULATION',     'VALIDATE_AT',      true),
    (gen_random_uuid(), 'CLOTUREE',  'ARCHIVEE',  'EXPORT_PDF',     'EXPORT_PDF',       true);
