-- ============================================================
-- V29 : Correctifs permissions (audit 04/08/2026)
-- 1. HM : ajouter VIEW_PERMIS (manquant vs standard HMEP/HMEE)
-- 2. Doublon MANAGE_REFERENTIELS → fusion vers MANAGE_REFERENTIALS
-- ============================================================

-- 1) VIEW_PERMIS pour HM
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.nom = 'HM'
  AND p.nom = 'VIEW_PERMIS'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 2) Doublon orthographique MANAGE_REFERENTIELS
--    a) Transférer les liaisons role → MANAGE_REFERENTIALS
INSERT INTO role_permissions (role_id, permission_id)
SELECT rp.role_id, p_correct.id
FROM role_permissions rp
JOIN permissions p_wrong ON p_wrong.id = rp.permission_id AND p_wrong.nom = 'MANAGE_REFERENTIELS'
JOIN permissions p_correct ON p_correct.nom = 'MANAGE_REFERENTIALS'
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp2
    WHERE rp2.role_id = rp.role_id AND rp2.permission_id = p_correct.id
);

--    b) Supprimer les liaisons vers l''ancien nom
DELETE FROM role_permissions
WHERE permission_id IN (SELECT id FROM permissions WHERE nom = 'MANAGE_REFERENTIELS');

--    c) Supprimer la permission obsolète
DELETE FROM permissions WHERE nom = 'MANAGE_REFERENTIELS';
