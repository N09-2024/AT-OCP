package com.ocp.at.entity.enums;

/**
 * Résultat du contrôle de réception conjointe des travaux par le CEEP et CEEE.
 */
public enum ResultatReception {
    /** Travaux entièrement conformes aux exigences de l'AT et permis. */
    CONFORME,

    /** Travaux acceptés avec réserves documentées et actions correctives. */
    CONFORME_AVEC_RESERVES,

    /** Travaux non conformes - l'AT ne peut pas être clôturée. */
    NON_CONFORME
}
