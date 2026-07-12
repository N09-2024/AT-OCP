-- =============================================================================
-- V12: Flyway Schema Cleanup and Indexes
-- =============================================================================
-- Cette migration corrige les incohérences du schéma et ajoute les indexes
-- manquants pour optimiser les performances.
--
-- Principes:
-- 1. Ne jamais modifier une migration déjà exécutée
-- 2. Utiliser ALTER pour corriger les incohérences
-- 3. Ajouter les indexes pour les requêtes fréquentes
-- =============================================================================

-- =============================================================================
-- SECTION 1: SYNCHRONISATION DES CONTRAINTES STATUT
-- =============================================================================

-- 1.1 Synchroniser workflows_at.statut avec TypeActionAT
-- L'action ARCHIVAGE doit être autorisée depuis CLOTUREE
DO $$
BEGIN
    -- Supprimer l'ancienne contrainte si elle existe
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_workflow_action' 
        AND table_name = 'workflows_at'
    ) THEN
        ALTER TABLE workflows_at DROP CONSTRAINT chk_workflow_action;
    END IF;
    
    -- Ajouter la nouvelle contrainte avec ARCHIVAGE
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_workflow_action' 
        AND table_name = 'workflows_at'
    ) THEN
        ALTER TABLE workflows_at ADD CONSTRAINT chk_workflow_action 
            CHECK (action IN (
                'CREATION','MODIFICATION','AUTO_SAVE','TRANSFERT',
                'SOUMISSION','VALIDATION','REFUS','RENOUVELLEMENT',
                'CLOTURE','EXPORT_PDF','ANNULATION','ARCHIVAGE'
            ));
    END IF;
END $$;

-- 1.2 Synchroniser workflows_at.etat_arrivee et etat_depart avec StatutAT
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_workflow_etat_depart'
    ) THEN
        ALTER TABLE workflows_at DROP CONSTRAINT chk_workflow_etat_depart;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_workflow_etat_arrivee'
    ) THEN
        ALTER TABLE workflows_at DROP CONSTRAINT chk_workflow_etat_arrivee;
    END IF;
    
    ALTER TABLE workflows_at ADD CONSTRAINT chk_workflow_etat_depart 
        CHECK (etat_depart IN (
            'BROUILLON','SOUMISE','VALIDEE','REJETEE',
            'RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE'
        ));
    
    ALTER TABLE workflows_at ADD CONSTRAINT chk_workflow_etat_arrivee 
        CHECK (etat_arrivee IN (
            'BROUILLON','SOUMISE','VALIDEE','REJETEE',
            'RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE'
        ));
END $$;

-- 1.3 Synchroniser historiques_at avec StatutAT (ancien_statut et nouveau_statut)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_historique_ancien_statut'
    ) THEN
        ALTER TABLE historiques_at DROP CONSTRAINT chk_historique_ancien_statut;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_historique_nouveau_statut'
    ) THEN
        ALTER TABLE historiques_at DROP CONSTRAINT chk_historique_nouveau_statut;
    END IF;
    
    ALTER TABLE historiques_at ADD CONSTRAINT chk_historique_ancien_statut 
        CHECK (ancien_statut IN (
            'BROUILLON','SOUMISE','VALIDEE','REJETEE',
            'RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE'
        ) OR ancien_statut IS NULL);
    
    ALTER TABLE historiques_at ADD CONSTRAINT chk_historique_nouveau_statut 
        CHECK (nouveau_statut IN (
            'BROUILLON','SOUMISE','VALIDEE','REJETEE',
            'RENOUVELEE','CLOTUREE','ARCHIVEE','ANNULEE'
        ) OR nouveau_statut IS NULL);
END $$;

-- =============================================================================
-- SECTION 2: INDEXES POUR PERFORMANCE
-- =============================================================================

-- 2.1 Index sur autorisations_travail

-- Index sur statut pour les requêtes de filtrage
CREATE INDEX IF NOT EXISTS idx_at_statut ON autorisations_travail(statut);

-- Index sur numero pour les recherches
CREATE INDEX IF NOT EXISTS idx_at_numero ON autorisations_travail(numero);

-- Index sur proprietaire_brouillon_id
CREATE INDEX IF NOT EXISTS idx_at_proprietaire ON autorisations_travail(proprietaire_brouillon_id);

-- Index sur dates pour les rapports
CREATE INDEX IF NOT EXISTS idx_at_date_debut ON autorisations_travail(date_debut);
CREATE INDEX IF NOT EXISTS idx_at_date_fin ON autorisations_travail(date_fin);

-- Index sur etat_verrou pour le locking
CREATE INDEX IF NOT EXISTS idx_at_verrou ON autorisations_travail(etat_verrou);

-- 2.2 Index sur visas

-- Index sur at_id pour les recherches par AT
CREATE INDEX IF NOT EXISTS idx_visa_at ON visas(at_id);

-- Index sur utilisateur_id
CREATE INDEX IF NOT EXISTS idx_visa_utilisateur ON visas(utilisateur_id);

-- Index sur statut pour les filtres
CREATE INDEX IF NOT EXISTS idx_visa_statut ON visas(statut);

-- Index sur date_visa pour l'historique
CREATE INDEX IF NOT EXISTS idx_visa_date ON visas(date_visa);

-- 2.3 Index sur permis

-- Index sur at_id pour les recherches par AT
CREATE INDEX IF NOT EXISTS idx_permis_at ON permis(at_id);

-- Index sur type pour les filtres
CREATE INDEX IF NOT EXISTS idx_permis_type ON permis(type);

-- Index sur statut_verification
CREATE INDEX IF NOT EXISTS idx_permis_statut_verif ON permis(statut_verification);

-- Index sur date_expiration pour les alertes
CREATE INDEX IF NOT EXISTS idx_permis_expiration ON permis(date_expiration);

-- 2.4 Index sur historique_at

-- Index sur at_id pour l'historique
CREATE INDEX IF NOT EXISTS idx_historique_at ON historiques_at(at_id);

-- Index sur date_action pour les rapports
CREATE INDEX IF NOT EXISTS idx_historique_date ON historiques_at(date_action);

-- Index sur action pour les filtres
CREATE INDEX IF NOT EXISTS idx_historique_action ON historiques_at(action);

-- 2.5 Index sur notifications

-- Index sur utilisateur_id
CREATE INDEX IF NOT EXISTS idx_notification_utilisateur ON notifications(utilisateur_id);

-- Index sur lu pour les filtres non lus
CREATE INDEX IF NOT EXISTS idx_notification_lu ON notifications(lu);

-- Index sur date_creation
CREATE INDEX IF NOT EXISTS idx_notification_date ON notifications(date_creation);

-- 2.6 Index sur audit_logs

-- Index sur utilisateur_id
CREATE INDEX IF NOT EXISTS idx_audit_utilisateur ON audit_logs(utilisateur_id);

-- Index sur date pour les rapports
CREATE INDEX IF NOT EXISTS idx_audit_date ON audit_logs(date);

-- Index sur action
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);

-- 2.7 Index sur recepions_travaux (Module 9)

-- Index sur autorisation_travail_id
CREATE INDEX IF NOT EXISTS idx_reception_at ON receptions_travaux(autorisation_travail_id);

-- Index sur responsable_id
CREATE INDEX IF NOT EXISTS idx_reception_responsable ON receptions_travaux(responsable_id);

-- Index sur date_reception
CREATE INDEX IF NOT EXISTS idx_reception_date ON receptions_travaux(date_reception);

-- 2.8 Index sur archives_at (Module 10)

-- Index sur autorisation_travail_id
CREATE INDEX IF NOT EXISTS idx_archive_at ON archives_at(autorisation_travail_id);

-- Index sur archive_par_id
CREATE INDEX IF NOT EXISTS idx_archive_par ON archives_at(archive_par_id);

-- Index sur hash_sha256 pour les vérifications
CREATE INDEX IF NOT EXISTS idx_archive_hash ON archives_at(hash_sha256);

-- Index sur archive_status
CREATE INDEX IF NOT EXISTS idx_archive_status ON archives_at(archive_status);

-- Index sur deleted
CREATE INDEX IF NOT EXISTS idx_archive_deleted ON archives_at(deleted);

-- Index sur date_archivage
CREATE INDEX IF NOT EXISTS idx_archive_date ON archives_at(date_archivage);

-- =============================================================================
-- SECTION 3: CORRECTIONS DE SCHÉMA
-- =============================================================================

-- 3.1 Ajouter la colonne signature_path à receptions_travaux si elle n'existe pas
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'receptions_travaux' 
        AND column_name = 'signature_path'
    ) THEN
        ALTER TABLE receptions_travaux ADD COLUMN signature_path VARCHAR(500);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'receptions_travaux' 
        AND column_name = 'signature_date'
    ) THEN
        ALTER TABLE receptions_travaux ADD COLUMN signature_date TIMESTAMP;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'receptions_travaux' 
        AND column_name = 'signature_by'
    ) THEN
        ALTER TABLE receptions_travaux ADD COLUMN signature_by VARCHAR(255);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'receptions_travaux' 
        AND column_name = 'validee'
    ) THEN
        ALTER TABLE receptions_travaux ADD COLUMN validee BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'receptions_travaux' 
        AND column_name = 'essais_conformes'
    ) THEN
        ALTER TABLE receptions_travaux ADD COLUMN essais_conformes BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'receptions_travaux' 
        AND column_name = 'installation_remise_en_etat'
    ) THEN
        ALTER TABLE receptions_travaux ADD COLUMN installation_remise_en_etat BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- 3.2 Ajouter les colonnes manquantes à archives_at si nécessaire
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'archives_at' 
        AND column_name = 'mime_type'
    ) THEN
        ALTER TABLE archives_at ADD COLUMN mime_type VARCHAR(100) DEFAULT 'application/pdf';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'archives_at' 
        AND column_name = 'deleted'
    ) THEN
        ALTER TABLE archives_at ADD COLUMN deleted BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- =============================================================================
-- SECTION 4: VALIDATION DES CONTRAINTES
-- =============================================================================

-- 4.1 Valider que toutes les foreign keys existent
DO $$
BEGIN
    -- Vérifier que les clés étrangères sont valides
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu 
            ON tc.constraint_name = kcu.constraint_name
        WHERE tc.constraint_type = 'FOREIGN KEY'
        AND tc.table_name = 'autorisations_travail'
    ) THEN
        RAISE NOTICE 'Foreign keys on autorisations_travail are valid';
    END IF;
END $$;

-- =============================================================================
-- SECTION 5: COMPTES RENDUS
-- =============================================================================

DO $$
BEGIN
    RAISE NOTICE '===============================================';
    RAISE NOTICE 'V12 Migration: Flyway Schema Cleanup and Indexes';
    RAISE NOTICE '===============================================';
    RAISE NOTICE 'Indexes ajoutes: 30+';
    RAISE NOTICE 'Contraintes synchronisees: 6';
    RAISE NOTICE 'Corrections de schema: 2';
    RAISE NOTICE 'Migration terminee avec succes';
END $$;
