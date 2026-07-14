-- ============================================================
-- V8__module_7_gestion_permis.sql
-- Insertion des rôles de base et permissions de gestion de permis
-- Note: Colonnes déjà dans V1 (est_obligatoire, commentaire, uploaded_by, json_extraction)
-- ============================================================

-- 1. Insérer les rôles de base
INSERT INTO roles (id, nom, description) VALUES
    ('role_1', 'ADMIN', 'Administrateur système'),
    ('role_2', 'SUPERVISEUR', 'Superviseur des travaux'),
    ('role_3', 'RESPONSABLE_OCP', 'Responsable OCP'),
    ('role_4', 'CHEF_DE_CHANTIER', 'Chef de chantier externe')
ON CONFLICT (nom) DO NOTHING;

-- 2. Insérer les permissions
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_upload_permis',   'UPLOAD_PERMIS',   'Uploader un fichier de permis'),
    ('perm_view_permis',     'VIEW_PERMIS',     'Consulter les permis'),
    ('perm_edit_permis',     'EDIT_PERMIS',     'Modifier les permis'),
    ('perm_delete_permis',   'DELETE_PERMIS',   'Supprimer les permis'),
    ('perm_analyse_permis',  'ANALYSE_PERMIS',  'Lancer l''analyse IA des permis')
ON CONFLICT (nom) DO NOTHING;

-- 3. Attribuer aux rôles
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('role_1', 'perm_upload_permis'),
    ('role_1', 'perm_view_permis'),
    ('role_1', 'perm_edit_permis'),
    ('role_1', 'perm_delete_permis'),
    ('role_1', 'perm_analyse_permis'),
    ('role_2', 'perm_upload_permis'),
    ('role_2', 'perm_view_permis'),
    ('role_2', 'perm_edit_permis'),
    ('role_2', 'perm_analyse_permis')
ON CONFLICT DO NOTHING;
