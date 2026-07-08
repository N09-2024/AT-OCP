-- Ajout des colonnes pour les Permis
ALTER TABLE permis
ADD COLUMN est_obligatoire BOOLEAN DEFAULT false,
ADD COLUMN commentaire TEXT;

-- Ajout de la colonne pour FichierJoint
ALTER TABLE fichiers_joints
ADD COLUMN uploaded_by VARCHAR(255);

-- Ajout de la contrainte unique sur permis_id pour garantir la relation 1:1
ALTER TABLE fichiers_joints
ADD CONSTRAINT uk_fichiers_joints_permis UNIQUE (permis_id);

-- Ajout de la colonne pour AnalyseIA
ALTER TABLE analyses_ia
ADD COLUMN json_extraction TEXT;

-- Ajout de la contrainte unique sur permis_id pour garantir la relation 1:1
ALTER TABLE analyses_ia
ADD CONSTRAINT uk_analyses_ia_permis UNIQUE (permis_id);

-- Insertion des nouvelles permissions
INSERT INTO permissions (id, nom, description) VALUES 
('perm_upload_permis', 'UPLOAD_PERMIS', 'Uploader un fichier de permis'),
('perm_view_permis', 'VIEW_PERMIS', 'Consulter les permis'),
('perm_edit_permis', 'EDIT_PERMIS', 'Modifier les permis'),
('perm_delete_permis', 'DELETE_PERMIS', 'Supprimer les permis'),
('perm_analyse_permis', 'ANALYSE_PERMIS', 'Lancer l''analyse IA des permis');

-- Affectation des permissions aux roles ADMIN (role_1) et SUPERVISEUR (role_2)
INSERT INTO role_permissions (role_id, permission_id) VALUES 
('role_1', 'perm_upload_permis'),
('role_1', 'perm_view_permis'),
('role_1', 'perm_edit_permis'),
('role_1', 'perm_delete_permis'),
('role_1', 'perm_analyse_permis'),
('role_2', 'perm_upload_permis'),
('role_2', 'perm_view_permis'),
('role_2', 'perm_edit_permis'),
('role_2', 'perm_analyse_permis');
