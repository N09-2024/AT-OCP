package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeAtResponse {

    private String summary;

    @Builder.Default
    private List<String> missingInformation = new ArrayList<>();

    @Builder.Default
    private List<String> identifiedRisks = new ArrayList<>();

    @Builder.Default
    private List<String> recommendedMeasures = new ArrayList<>();

    @Builder.Default
    private List<String> inconsistencies = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    @Builder.Default
    private List<String> sources = new ArrayList<>();

    private String confidence;

    // Rétrocompatibilité avec AnalyseInterventionIAResponse
    @Builder.Default
    private List<String> risques = new ArrayList<>();

    @Builder.Default
    private List<String> mesures = new ArrayList<>();

    @Builder.Default
    private List<String> epis = new ArrayList<>();

    @Builder.Default
    private List<String> permis = new ArrayList<>();

    private String rapport;

    @Builder.Default
    private List<String> alertes = new ArrayList<>();

    private boolean complet;
    private String provider;
    private double tauxConfiance;
}
