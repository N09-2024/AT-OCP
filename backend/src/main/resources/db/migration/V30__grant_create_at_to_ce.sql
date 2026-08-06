-- V30 : débloquer création AT — s'assurer que CE a CREATE_AT
-- et que les utilisateurs encore en CEEP/CEEE reçoivent aussi le rôle CE

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'CREATE_AT', 'Créer une autorisation de travail'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'CREATE_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'EDIT_AT', 'Modifier un brouillon AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'EDIT_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'SUBMIT_AT', 'Soumettre une AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'SUBMIT_AT');

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, 'READ_AT', 'Consulter les AT'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = 'READ_AT');

-- Rôle CE
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'CE', 'Chef d''Équipe'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'CE');

-- Permissions minimales CE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.nom = 'CE'
  AND p.nom IN (
    'CREATE_AT', 'EDIT_AT', 'SUBMIT_AT', 'READ_AT',
    'CREATE_VISITE', 'SIGN_AT', 'CLOSE_AT', 'RECEIVE_AT',
    'START_INTERVENTION', 'DECLARE_FIN_TRAVAUX', 'RENEW_AT',
    'VIEW_PERMIS', 'EDIT_PERMIS', 'UPLOAD_FILES', 'EXPORT_PDF',
    'RECEIVE_NOTIFICATION', 'TRANSFER_AT'
  )
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Utilisateurs avec ancien rôle CEEP ou CEEE → ajouter rôle CE
INSERT INTO utilisateur_roles (utilisateur_id, role_id)
SELECT DISTINCT ur.utilisateur_id, ce.id
FROM utilisateur_roles ur
JOIN roles r_old ON r_old.id = ur.role_id
CROSS JOIN roles ce
WHERE r_old.nom IN ('CEEP', 'CEEE', 'DEMANDEUR')
  AND ce.nom = 'CE'
  AND NOT EXISTS (
    SELECT 1 FROM utilisateur_roles ur2
    WHERE ur2.utilisateur_id = ur.utilisateur_id AND ur2.role_id = ce.id
  );

-- ADMIN a toutes les permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.nom = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
