package com.ocp.at.entity.enums;

/**
 * Position résolue dynamiquement pour un utilisateur sur une AT donnée (Standard S-HSE-SEC-31 §5).
 *
 * <ul>
 *   <li>PROPRIETAIRE (P) : l'utilisateur appartient au service/zone propriétaire de l'installation</li>
 *   <li>EXECUTANT (E) : l'utilisateur appartient au service/zone exécutante qui intervient</li>
 *   <li>AUCUNE : l'utilisateur n'appartient à aucune des deux zones</li>
 * </ul>
 */
public enum PositionAT {
    PROPRIETAIRE,   // Position P
    EXECUTANT,      // Position E
    AUCUNE
}
