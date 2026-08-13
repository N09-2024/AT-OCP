package com.ocp.at.entity.enums;

/**
 * Niveaux hiérarchiques institutionnels de l'organisation OCP (Standard S-HSE-SEC-31 §5).
 *
 * <p>Chaque niveau est décliné dynamiquement en 2 positions selon la zone de l'AT :
 * <ul>
 *   <li>CHEF_EQUIPE &rarr; CEEP (Propriétaire) ou CEEE (Exécutant)</li>
 *   <li>HORS_CADRE &rarr; HCEP (Propriétaire) ou HCEE (Exécutant)</li>
 *   <li>HAUTE_MAITRISE &rarr; HMEP (Propriétaire) ou HMEE (Exécutant)</li>
 *   <li>ADMIN &rarr; Privilèges totaux</li>
 * </ul>
 * </p>
 */
public enum NiveauHierarchique {
    CHEF_EQUIPE,      // CE (CEEP / CEEE)
    HORS_CADRE,       // HC (HCEP / HCEE)
    HAUTE_MAITRISE,   // HM (HMEP / HMEE)
    ADMIN;

    public static NiveauHierarchique fromRoleName(String roleNom) {
        if (roleNom == null) return CHEF_EQUIPE;
        String upper = roleNom.toUpperCase();
        if (upper.contains("ADMIN")) return ADMIN;
        if (upper.contains("HC") || upper.contains("HCEP") || upper.contains("HCEE") || upper.contains("RESPONSABLE_OCP")) return HORS_CADRE;
        if (upper.contains("HM") || upper.contains("HMEP") || upper.contains("HMEE")) return HAUTE_MAITRISE;
        return CHEF_EQUIPE;
    }
}
