-- ============================================================
-- V9__module_8_workflow_visa_signature.sql
-- Permissions workflow + attribution aux rôles
-- Note: Toutes les colonnes (ordre, signature_path, etc.) sont déjà dans V1
-- ============================================================

-- Permissions Module 8
INSERT INTO permissions (id, nom, description) VALUES
    ('perm_submit_at',   'SUBMIT_AT',   'Soumettre une AT pour validation'),
    ('perm_validate_at', 'VALIDATE_AT', 'Valider une AT'),
    ('perm_refuse_at',   'REFUSE_AT',   'Refuser une AT'),
    ('perm_sign_at',     'SIGN_AT',     'Signer électroniquement un visa AT'),
    ('perm_renew_at',    'RENEW_AT',    'Renouveler une AT'),
    ('perm_close_at',    'CLOSE_AT',    'Clôturer une AT')
ON CONFLICT (nom) DO NOTHING;

-- Attribution au rôle ADMIN (role_1) et SUPERVISEUR (role_2)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('role_1', 'perm_submit_at'),
    ('role_1', 'perm_validate_at'),
    ('role_1', 'perm_refuse_at'),
    ('role_1', 'perm_sign_at'),
    ('role_1', 'perm_renew_at'),
    ('role_1', 'perm_close_at'),
    ('role_2', 'perm_submit_at'),
    ('role_2', 'perm_validate_at'),
    ('role_2', 'perm_refuse_at'),
    ('role_2', 'perm_sign_at'),
    ('role_2', 'perm_renew_at'),
    ('role_2', 'perm_close_at')
ON CONFLICT DO NOTHING;
