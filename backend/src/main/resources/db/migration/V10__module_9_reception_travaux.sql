-- ============================================================
-- V10__module_9_reception_travaux.sql
-- Permissions de réception des travaux
-- Note: Tables receptions_travaux, photos_reception, historiques_reception
--       sont déjà créées dans V1
-- ============================================================

INSERT INTO permissions (id, nom, description)
SELECT gen_random_uuid()::text, p.nom, p.description
FROM (VALUES
    ('CREATE_RECEPTION',   'Créer une réception des travaux'),
    ('EDIT_RECEPTION',     'Modifier une réception des travaux'),
    ('DELETE_RECEPTION',   'Supprimer une réception des travaux'),
    ('VIEW_RECEPTION',     'Consulter les réceptions des travaux'),
    ('SIGN_RECEPTION',     'Signer une réception des travaux')
) AS p(nom, description)
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE nom = p.nom
);
