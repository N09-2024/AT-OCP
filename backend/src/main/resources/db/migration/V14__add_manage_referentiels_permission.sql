-- ============================================================
-- V14__add_manage_referentiels_permission.sql
-- Ajout de la permission MANAGE_REFERENTIELS manquante
-- et attribution au rôle ADMIN (role_1)
-- ============================================================

-- 1. Insérer la permission MANAGE_REFERENTIELS
INSERT INTO permissions (id, nom, description)
VALUES (
    'perm_manage_referentiels',
    'MANAGE_REFERENTIELS',
    'Créer, modifier et supprimer les référentiels (zones, installations, services, équipements, EPI, risques, etc.)'
) ON CONFLICT (nom) DO NOTHING;

-- 2. Attribuer la permission MANAGE_REFERENTIELS au rôle ADMIN
INSERT INTO role_permissions (role_id, permission_id)
VALUES ('role_1', 'perm_manage_referentiels')
ON CONFLICT DO NOTHING;

-- 3. S'assurer que l'admin a aussi les autres permissions essentielles
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'role_1', id FROM permissions
WHERE nom IN (
    'MANAGE_DOCUMENTS',
    'MANAGE_REFERENTIELS',
    'UPLOAD_PERMIS',
    'VIEW_PERMIS',
    'EDIT_PERMIS',
    'DELETE_PERMIS',
    'ANALYSE_PERMIS'
)
ON CONFLICT DO NOTHING;
