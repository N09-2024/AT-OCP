package com.ocp.at.entity.enums;

/**
 * Statuts de l'Autorisation de Travail.
 *
 * Les statuts standard (CLASSIFICATION_EFFECTUEE → ARCHIVEE) reflètent fidèlement
 * le workflow §7 du Standard OCP S-HSE-SEC-31 v1.0.
 *
 * Les statuts legacy (BROUILLON, SOUMISE, VALIDEE, RENOUVELEE, CLOTUREE) sont conservés
 * pour la rétrocompatibilité avec les AT existantes. Voir le champ `statutWorkflow` sur
 * AutorisationTravail pour le suivi conforme au standard.
 */
public enum StatutAT {

    // =========================================================================
    // Statuts standard S-HSE-SEC-31 §7 (workflow principal)
    // =========================================================================

    /** Étape 0 — HCEP a classifié l'intervention en Niveau 2 → AT obligatoire. */
    CLASSIFICATION_EFFECTUEE,

    /** Étape 1 — CEEP a créé la demande (DI/BT/OT) : zone, date, durée définis. */
    DEMANDE_CREEE,

    /** Étape 2 — Visite chantier réalisée par CEEP, garantie par HCEE + HMEP. */
    VISITE_REALISEE,

    /** Étape 3 — AT et permis rédigés et signés sur le terrain (CEEP E, HCEE G, CEEE P). */
    AT_REDIGEE,

    /** Étape 4 — Travaux en cours (démarrés par CEEE E, garantis par HCEE G + HMEP G). */
    INTERVENTION_EN_COURS,

    /**
     * Étape 5b — AT reconduite (dépassement d'un poste).
     * CEEP revérifie et vise (E), HCEE garantit (G), CEEE participe (P).
     * Si dépassement > 24h → retour à l'Étape 2.
     */
    AT_RECONDUITE,

    /** Étape 6 — Fin des travaux déclarée par CEEE (E). CEEP est informé (I). */
    FIN_TRAVAUX_DECLAREE,

    /**
     * Étape 7 — Réception des travaux effectuée : CEEP (E) + CEEE (P).
     * Essais concluants, AT et permis clôturés.
     * Permis de feu : clôture 2h après fin travaux par points chauds.
     */
    TRAVAUX_RECEPTIONES,

    /**
     * Étape 8 — Documents archivés par l'entité propriétaire.
     * HCEE exécute (E), HCEP est garant (G). Durée minimale : 1 an.
     */
    ARCHIVEE,

    // =========================================================================
    // Statuts d'exception (conservés)
    // =========================================================================

    /** AT rejetée par HCEE lors de la validation. */
    REJETEE,

    /** AT annulée (avant intervention ou incident grave). */
    ANNULEE,

    // =========================================================================
    // Statuts legacy (conservés pour compatibilité historique — ne pas utiliser
    // pour les nouvelles AT ; utiliser le champ `statutWorkflow` à la place)
    // =========================================================================

    /** @deprecated Utiliser DEMANDE_CREEE dans le workflow standard. */
    @Deprecated
    BROUILLON,

    /** @deprecated Utiliser VISITE_REALISEE dans le workflow standard. */
    @Deprecated
    SOUMISE,

    /** @deprecated Utiliser AT_REDIGEE dans le workflow standard. */
    @Deprecated
    VALIDEE,

    /** @deprecated Utiliser AT_RECONDUITE dans le workflow standard. */
    @Deprecated
    RENOUVELEE,

    /** @deprecated Utiliser TRAVAUX_RECEPTIONES dans le workflow standard. */
    @Deprecated
    CLOTUREE
}

