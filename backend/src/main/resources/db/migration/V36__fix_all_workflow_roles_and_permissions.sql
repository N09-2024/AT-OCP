-- ============================================================
-- Migration V36: Harmonisation complète des permissions pour
-- tous les rôles du workflow Standard S-HSE-SEC-31:
-- CEEP, CEEE, HCEP, HCEE, HMEP, HMEE, CE, HM, HC, ADMIN
-- ============================================================

-- 1. S'assurer que tous les rôles standard existent
INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'CEEP', 'Chef d''Équipe de l''Entité Propriétaire'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'CEEP');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'CEEE', 'Chef d''Équipe de l''Entité Exécutante'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'CEEE');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HCEP', 'Hors Cadre Responsable de l''Entité Propriétaire'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HCEP');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HCEE', 'Hors Cadre Responsable de l''Entité Exécutante'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HCEE');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HMEP', 'Haute Maîtrise de l''Entité Propriétaire'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HMEP');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HMEE', 'Haute Maîtrise de l''Entité Exécutante'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HMEE');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'CE', 'Chef d''Équipe (polyvalent Propriétaire / Exécutant)'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'CE');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HM', 'Haute Maîtrise (polyvalent Propriétaire / Exécutant)'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HM');

INSERT INTO roles (id, nom, description)
SELECT gen_random_uuid()::text, 'HC', 'Hors Cadre (polyvalent Propriétaire / Exécutant)'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nom = 'HC');

-- 2. S'assurer que toutes les permissions requises existent
INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, p.nom, p.description
FROM (VALUES
    ('READ_AT',              'Consulter les autorisations de travail'),
    ('CREATE_AT',            'Créer une autorisation de travail'),
    ('EDIT_AT',              'Modifier une autorisation de travail'),
    ('SUBMIT_AT',            'Soumettre une autorisation de travail'),
    ('VALIDATE_AT',          'Valider une autorisation de travail'),
    ('REJECT_AT',            'Rejeter une autorisation de travail'),
    ('CLOSE_AT',             'Clôturer une autorisation de travail'),
    ('SIGN_AT',              'Signer ou viser une AT ou un permis'),
    ('CREATE_VISITE',        'Créer et réaliser une visite préalable'),
    ('VALIDATE_VISITE',      'Valider/garantir une visite préalable'),
    ('START_INTERVENTION',   'Démarrer une intervention'),
    ('DECLARE_FIN_TRAVAUX',  'Déclarer la fin des travaux'),
    ('RECEIVE_AT',           'Réceptionner les travaux'),
    ('RENEW_AT',             'Reconduire une AT'),
    ('CLASSIFY_INTERVENTION','Classifier une intervention Niveau 1 / 2'),
    ('ARCHIVE_AT',           'Archiver officiellement une AT'),
    ('VIEW_ARCHIVE',         'Consulter les archives AT'),
    ('MANAGE_HABILITATIONS', 'Gérer les habilitations des agents'),
    ('TRANSFER_AT',          'Transférer le verrou d''édition d''une AT'),
    ('VIEW_RECEPTION',       'Consulter les réceptions des travaux'),
    ('CREATE_RECEPTION',     'Créer une réception des travaux'),
    ('EDIT_RECEPTION',       'Modifier une réception des travaux'),
    ('SIGN_RECEPTION',       'Signer une réception des travaux'),
    ('DELETE_RECEPTION',     'Supprimer une réception des travaux'),
    ('VIEW_PERMIS',          'Consulter les permis'),
    ('CREATE_PERMIS',        'Créer un permis'),
    ('EDIT_PERMIS',          'Modifier un permis'),
    ('DELETE_PERMIS',        'Supprimer un permis'),
    ('UPLOAD_PERMIS',        'Uploader un permis'),
    ('ANALYSE_PERMIS',       'Analyser un permis avec l''IA'),
    ('UPLOAD_FILES',         'Uploader des fichiers'),
    ('EXPORT_PDF',           'Exporter une AT en PDF'),
    ('MANAGE_REFERENTIALS',  'Gérer les référentiels'),
    ('VIEW_AUDIT',           'Consulter l''audit'),
    ('MANAGE_USERS',         'Gérer les utilisateurs'),
    ('MANAGE_ROLES',         'Gérer les rôles et permissions'),
    ('RECEIVE_NOTIFICATION', 'Recevoir des notifications système')
) AS p(nom, description)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE nom = p.nom);

-- 3. Attribuer les permissions à CEEP (Chef d'Équipe Propriétaire)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CEEP'
  AND p.nom IN (
      'READ_AT', 'CREATE_AT', 'EDIT_AT', 'SUBMIT_AT', 'SIGN_AT', 'VALIDATE_AT',
      'RENEW_AT', 'RECEIVE_AT', 'CLOSE_AT', 'CREATE_VISITE', 'TRANSFER_AT',
      'VIEW_RECEPTION', 'CREATE_RECEPTION', 'EDIT_RECEPTION', 'SIGN_RECEPTION',
      'VIEW_PERMIS', 'CREATE_PERMIS', 'EDIT_PERMIS', 'UPLOAD_PERMIS', 'ANALYSE_PERMIS',
      'UPLOAD_FILES', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 4. Attribuer les permissions à CEEE (Chef d'Équipe Exécutant)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CEEE'
  AND p.nom IN (
      'READ_AT', 'EDIT_AT', 'SIGN_AT', 'VALIDATE_AT', 'START_INTERVENTION',
      'DECLARE_FIN_TRAVAUX', 'RECEIVE_AT', 'CLOSE_AT', 'CREATE_VISITE', 'RENEW_AT',
      'VIEW_RECEPTION', 'CREATE_RECEPTION', 'SIGN_RECEPTION',
      'VIEW_PERMIS', 'CREATE_PERMIS', 'EDIT_PERMIS', 'UPLOAD_PERMIS', 'ANALYSE_PERMIS',
      'UPLOAD_FILES', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 5. Attribuer les permissions à HCEP (Hors Cadre Propriétaire)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HCEP'
  AND p.nom IN (
      'READ_AT', 'CLASSIFY_INTERVENTION', 'SIGN_AT', 'VALIDATE_AT', 'REJECT_AT',
      'VALIDATE_VISITE', 'VIEW_ARCHIVE', 'ARCHIVE_AT', 'MANAGE_HABILITATIONS',
      'VIEW_RECEPTION', 'VIEW_PERMIS', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION',
      'MANAGE_REFERENTIALS', 'VIEW_AUDIT'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 6. Attribuer les permissions à HCEE (Hors Cadre Exécutant)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HCEE'
  AND p.nom IN (
      'READ_AT', 'SIGN_AT', 'VALIDATE_AT', 'REJECT_AT', 'VALIDATE_VISITE',
      'START_INTERVENTION', 'ARCHIVE_AT', 'VIEW_ARCHIVE', 'VIEW_RECEPTION',
      'VIEW_PERMIS', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION', 'MANAGE_REFERENTIALS', 'VIEW_AUDIT'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 7. Attribuer les permissions à HMEP (Haute Maîtrise Propriétaire)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HMEP'
  AND p.nom IN (
      'READ_AT', 'SIGN_AT', 'VALIDATE_AT', 'REJECT_AT', 'VALIDATE_VISITE',
      'VIEW_ARCHIVE', 'ARCHIVE_AT', 'VIEW_RECEPTION', 'VIEW_PERMIS', 'EXPORT_PDF',
      'RECEIVE_NOTIFICATION'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 8. Attribuer les permissions à HMEE (Haute Maîtrise Exécutante)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HMEE'
  AND p.nom IN (
      'READ_AT', 'SIGN_AT', 'VALIDATE_AT', 'REJECT_AT', 'VALIDATE_VISITE',
      'START_INTERVENTION', 'VIEW_ARCHIVE', 'VIEW_RECEPTION', 'VIEW_PERMIS',
      'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 9. Attribuer les permissions complètes aux rôles synthétiques CE, HM, HC
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'CE'
  AND p.nom IN (
      'CREATE_AT', 'EDIT_AT', 'SUBMIT_AT', 'READ_AT', 'CREATE_VISITE', 'SIGN_AT',
      'CLOSE_AT', 'RECEIVE_AT', 'START_INTERVENTION', 'DECLARE_FIN_TRAVAUX',
      'RENEW_AT', 'VALIDATE_AT', 'REJECT_AT', 'VIEW_RECEPTION', 'CREATE_RECEPTION',
      'EDIT_RECEPTION', 'SIGN_RECEPTION', 'VIEW_PERMIS', 'CREATE_PERMIS', 'EDIT_PERMIS',
      'UPLOAD_PERMIS', 'ANALYSE_PERMIS', 'UPLOAD_FILES', 'EXPORT_PDF', 'RECEIVE_NOTIFICATION',
      'TRANSFER_AT'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HM'
  AND p.nom IN (
      'READ_AT', 'VALIDATE_VISITE', 'SIGN_AT', 'START_INTERVENTION', 'VALIDATE_AT',
      'REJECT_AT', 'VIEW_ARCHIVE', 'ARCHIVE_AT', 'VIEW_RECEPTION', 'VIEW_PERMIS',
      'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'HC'
  AND p.nom IN (
      'READ_AT', 'CLASSIFY_INTERVENTION', 'VALIDATE_AT', 'REJECT_AT', 'VALIDATE_VISITE',
      'SIGN_AT', 'START_INTERVENTION', 'ARCHIVE_AT', 'VIEW_ARCHIVE', 'VIEW_RECEPTION',
      'MANAGE_HABILITATIONS', 'MANAGE_REFERENTIALS', 'VIEW_AUDIT', 'VIEW_PERMIS',
      'EXPORT_PDF', 'RECEIVE_NOTIFICATION'
  )
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- ADMIN a toutes les permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.nom = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
