package com.ocp.at.entity.enums;

/**
 * Statuts d'une demande de reconduction d'AT.
 */
public enum StatutReconduction {
    /** Demande soumise par le CEEE, en attente de décision du HMEP (Responsable OCP). */
    REQUESTED,

    /** Demande approuvée par le HMEP (Responsable OCP). */
    APPROVED,

    /** Demande refusée par le HMEP (Responsable OCP) avec motif obligatoire. */
    REJECTED,

    /** Demande annulée par le demandeur. */
    CANCELLED
}
