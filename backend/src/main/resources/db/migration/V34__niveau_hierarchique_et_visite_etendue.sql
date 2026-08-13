-- V34: Standard S-HSE-SEC-31 §5 — niveau hiérarchique dynamique + §8.2 visite préalable étendue

-- 1. Ajouter la colonne niveau_hierarchique sur les utilisateurs
--    Valeur par défaut: CHEF_EQUIPE (rétro-compat).
ALTER TABLE utilisateurs
    ADD COLUMN IF NOT EXISTS niveau_hierarchique VARCHAR(30) DEFAULT 'CHEF_EQUIPE';

-- 2. Dériver le niveau depuis les rôles existants (migration des données)
UPDATE utilisateurs u
SET niveau_hierarchique = 'HORS_CADRE'
WHERE EXISTS (
    SELECT 1 FROM utilisateur_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE ur.utilisateur_id = u.id
    AND (r.nom ILIKE '%HC%' OR r.nom ILIKE '%HCEP%' OR r.nom ILIKE '%HCEE%' OR r.nom ILIKE '%RESPONSABLE_OCP%')
);

UPDATE utilisateurs u
SET niveau_hierarchique = 'HAUTE_MAITRISE'
WHERE EXISTS (
    SELECT 1 FROM utilisateur_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE ur.utilisateur_id = u.id
    AND (r.nom ILIKE '%HM%' OR r.nom ILIKE '%HMEP%' OR r.nom ILIKE '%HMEE%')
)
AND u.niveau_hierarchique = 'CHEF_EQUIPE'; -- ne pas écraser HORS_CADRE

UPDATE utilisateurs u
SET niveau_hierarchique = 'ADMIN'
WHERE EXISTS (
    SELECT 1 FROM utilisateur_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE ur.utilisateur_id = u.id
    AND r.nom ILIKE '%ADMIN%'
);

-- 3. Étendre visites_prealables pour capturer contenu §8.2
ALTER TABLE visites_prealables
    ADD COLUMN IF NOT EXISTS actions_prevention_identifiees TEXT,
    ADD COLUMN IF NOT EXISTS permis_requis TEXT,
    ADD COLUMN IF NOT EXISTS reference_plan_consignation VARCHAR(255),
    ADD COLUMN IF NOT EXISTS actions_prevention_suffisantes BOOLEAN DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS ceee_participant_id VARCHAR(255);

ALTER TABLE visites_prealables
    DROP CONSTRAINT IF EXISTS fk_visite_ceee_participant;

ALTER TABLE visites_prealables
    ADD CONSTRAINT fk_visite_ceee_participant
    FOREIGN KEY (ceee_participant_id) REFERENCES utilisateurs(id)
    ON DELETE SET NULL;

-- 4. Ajouter nouveaux statuts workflow AT conformes au Standard §7
--    (suppression des contraintes CHECK si elles existent et blocage l'ajout)
ALTER TABLE autorisations_travail
    DROP CONSTRAINT IF EXISTS autorisations_travail_statut_check,
    DROP CONSTRAINT IF EXISTS autorisations_travail_statut_workflow_check;

ALTER TABLE historiques_at
    DROP CONSTRAINT IF EXISTS historiques_at_ancien_statut_check,
    DROP CONSTRAINT IF EXISTS historiques_at_nouveau_statut_check;
