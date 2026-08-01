-- Fix : CREATE_AT, EDIT_AT, READ_AT, UPLOAD_FILES manquants sur CEEP
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CEEP'
  AND p.nom IN ('CREATE_AT', 'EDIT_AT', 'READ_AT', 'UPLOAD_FILES')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Fix : assigner CEEP à tous les utilisateurs sans rôle
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT u.id, (SELECT id FROM roles WHERE nom = 'CEEP')
FROM utilisateurs u
WHERE NOT EXISTS (
    SELECT 1 FROM utilisateur_roles ur WHERE ur.utilisateur_id = u.id
);