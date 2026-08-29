package com.ocp.at.entity.enums;

/**
 * Actions possibles sur une Autorisation de Travail.
 * Conforme au workflow §7 du Standard S-HSE-SEC-31 v1.0.
 */
public enum TypeActionAT {

    // =========================================================================
    // Actions standard S-HSE-SEC-31 (workflow principal)
    // =========================================================================

    /** Étape 0 - HCEP classifie l'intervention en Niveau 2 (AT obligatoire). */
    CLASSIFICATION,

    /** Étape 1 - CEEP crée la demande d'intervention (DI/BT/OT). */
    CREATION_DEMANDE,

    /** Étape 2 - CEEP réalise la visite chantier (avec CEEE participant, HCEE+HMEP garants). */
    VISITE_CHANTIER,

    /** Étape 3 - CEEP rédige l'AT et les permis sur le terrain. */
    REDACTION_AT,

    /** Étape 4 - CEEE démarre l'intervention (HCEE+HMEP garants du démarrage). */
    DEBUT_INTERVENTION,

    /**
     * Étape 5b - CEEP reconduit/renouvelle l'AT (dépassement d'un poste).
     * Si > 24h : retour à l'Étape 2 (nouvelle visite obligatoire).
     */
    RECONDUCTION,

    /** Étape 6 - CEEE déclare la fin des travaux. */
    DECLARATION_FIN,

    /** Étape 7 - CEEP + CEEE réceptionnent les travaux conjointement. */
    RECEPTION_CONJOINTE,

    /** Étape 8 - HCEE archive officiellement (HCEP est garant). */
    ARCHIVAGE_OFFICIEL,

    // =========================================================================
    // Actions existantes (conservées)
    // =========================================================================

    CREATION,
    MODIFICATION,
    AUTO_SAVE,
    TRANSFERT,
    SOUMISSION,
    VALIDATION,
    REFUS,

    /** @deprecated Utiliser RECONDUCTION pour le workflow standard. */
    @Deprecated
    RENOUVELLEMENT,

    CLOTURE,
    EXPORT_PDF,
    ANNULATION,
    RECEPTION_TRAVAUX,
    VALIDATION_RECEPTION,
    VALIDATION_RECEPTION_CEEP,
    DEMANDE_RECONDUCTION,
    APPROBATION_RECONDUCTION,
    REFUS_RECONDUCTION
}

