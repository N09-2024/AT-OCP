package com.ocp.at.security;

import com.ocp.at.entity.Utilisateur;

public final class RoleUtils {

    private RoleUtils() {
        // utilitaire - non instanciable
    }

    /**
     * Vérifie si l'utilisateur possède un rôle correspondant au pattern donné.
     *
     * Règles d'équivalence S-HSE-SEC-31 V28/V30 :
     * - Pattern "CE"   : matche CE, CEEP, CEEE, DEMANDEUR (Chef d'Équipe générique)
     * - Pattern "CEEP" : matche CEEP et CE uniquement (Chef d'Équipe Propriétaire)
     *                    - NE MATCHE PAS CEEE (rôle exécutant distinct)
     * - Pattern "CEEE" : matche CEEE et CE uniquement (Chef d'Équipe Exécutant)
     *                    - NE MATCHE PAS CEEP (rôle propriétaire distinct)
     * - Pattern "HM"   : matche HM, HMEP, HMEE (Haute Maîtrise générique)
     * - Pattern "HC"   : matche HC, HCEP, HCEE, RESPONSABLE_OCP (Hors Cadre générique)
     * - Autres patterns : correspondance par sous-chaîne (nom.contains(pattern))
     */
    public static boolean userHasRolePattern(Utilisateur user, String pattern) {
        if (user == null || user.getRoles() == null || pattern == null) {
            return false;
        }
        String target = pattern.toUpperCase();
        return user.getRoles().stream().anyMatch(r -> {
            if (r.getNom() == null) return false;
            String nom = r.getNom().toUpperCase();

            // Correspondance exacte ou sous-chaîne directe (cas normal)
            if (nom.contains(target)) {
                return true;
            }

            // Équivalences métier explicites - CEEP et CEEE sont des rôles DISTINCTS
            switch (target) {
                case "CE":
                    // CE générique : matche tout Chef d'Équipe (propriétaire ET exécutant)
                    return "CE".equals(nom) || "CEEP".equals(nom) || "CEEE".equals(nom) || "DEMANDEUR".equals(nom);
                case "CEEP":
                    // Chef d'Équipe Propriétaire : matche CEEP et le rôle générique CE
                    // NE matche PAS CEEE (rôle exécutant distinct - règle §8 OCP)
                    return "CEEP".equals(nom) || "CE".equals(nom) || "DEMANDEUR".equals(nom);
                case "CEEE":
                    // Chef d'Équipe Exécutant : matche CEEE et le rôle générique CE
                    // NE matche PAS CEEP (rôle propriétaire distinct - règle §8 OCP)
                    return "CEEE".equals(nom) || "CE".equals(nom);
                case "HM":
                    // Haute Maîtrise générique
                    return "HM".equals(nom) || "HMEP".equals(nom) || "HMEE".equals(nom);
                case "HMEP":
                    return "HMEP".equals(nom) || "HM".equals(nom);
                case "HMEE":
                    return "HMEE".equals(nom) || "HM".equals(nom);
                case "HC":
                    // Hors Cadre générique
                    return "HC".equals(nom) || "HCEP".equals(nom) || "HCEE".equals(nom) || "RESPONSABLE_OCP".equals(nom);
                case "HCEP":
                    return "HCEP".equals(nom) || "HC".equals(nom);
                case "HCEE":
                    return "HCEE".equals(nom) || "HC".equals(nom) || "RESPONSABLE_OCP".equals(nom);
                default:
                    return false;
            }
        });
    }

    public static boolean isCeep(Utilisateur user) {
        return userHasRolePattern(user, "CEEP");
    }

    public static boolean isCeee(Utilisateur user) {
        return userHasRolePattern(user, "CEEE");
    }

    public static boolean isHm(Utilisateur user) {
        return userHasRolePattern(user, "HM");
    }

    public static boolean isHc(Utilisateur user) {
        return userHasRolePattern(user, "HC");
    }
}