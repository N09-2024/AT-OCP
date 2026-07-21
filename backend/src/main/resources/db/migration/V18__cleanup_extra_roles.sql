-- V18__cleanup_extra_roles.sql
-- Nettoyage des roles supplementaires pour ne garder que les 4 roles metier
-- (ADMIN, RESPONSABLE_OCP, RESPONSABLE_ENTREPRISE, DEMANDEUR)

DELETE FROM utilisateur_roles WHERE role_id IN (
    SELECT id FROM roles WHERE nom NOT IN ('ADMIN', 'RESPONSABLE_OCP', 'RESPONSABLE_ENTREPRISE', 'DEMANDEUR')
);

DELETE FROM role_permissions WHERE role_id IN (
    SELECT id FROM roles WHERE nom NOT IN ('ADMIN', 'RESPONSABLE_OCP', 'RESPONSABLE_ENTREPRISE', 'DEMANDEUR')
);

DELETE FROM roles WHERE nom NOT IN ('ADMIN', 'RESPONSABLE_OCP', 'RESPONSABLE_ENTREPRISE', 'DEMANDEUR');
