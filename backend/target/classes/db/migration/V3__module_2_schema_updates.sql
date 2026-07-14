-- ============================================================
-- V3__module_2_schema_updates.sql
-- Ajout de la permission MANAGE_DOCUMENTS
-- Note: Toutes les colonnes/tables sont déjà dans V1
-- ============================================================

INSERT INTO permissions (id, nom, description)
VALUES (
    gen_random_uuid()::text,
    'MANAGE_DOCUMENTS',
    'Créer, modifier et gérer les documents d''intervention (DI, OT, BT)'
) ON CONFLICT (nom) DO NOTHING;
