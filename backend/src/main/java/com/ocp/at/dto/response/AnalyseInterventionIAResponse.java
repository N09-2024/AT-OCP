package com.ocp.at.dto.response;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Suggestions IA alignées sur F-HSE-SEC-31-04 (sections A, B, D, E).
 * Non décisionnaire : le CEEP accepte ou corrige avant visa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyseInterventionIAResponse {

    /** Libellés risques section A (suggestions) */
    @Builder.Default
    private List<String> risques = new ArrayList<>();

    /** Mesures préparation section B */
    @Builder.Default
    private List<String> mesures = new ArrayList<>();

    /** EPI section D */
    @Builder.Default
    private List<String> epis = new ArrayList<>();

    /** Types de permis section E */
    @Builder.Default
    private List<String> permis = new ArrayList<>();

    /** Résumé court (agent rapport) */
    private String rapport;

    /** Alertes de contrôle dossier (CrewAI contrôleur) */
    @Builder.Default
    private List<String> alertes = new ArrayList<>();

    /** true si le dossier semble complet pour soumission */
    private boolean complet;

    /** Provider utilisé (MOCK / LANG_CHAIN / CREW_AI) */
    private String provider;

    private double tauxConfiance;
}
