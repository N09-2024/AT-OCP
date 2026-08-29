package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndInterventionRequest {

    @NotBlank(message = "La description des travaux réalisés est obligatoire")
    private String travauxRealises;

    private String travauxNonRealises;

    private String anomalies;

    private String observations;

    @Builder.Default
    private Boolean zoneNettoyee = true;

    @Builder.Default
    private Boolean materielRetire = true;

    @Builder.Default
    private Boolean outilsRetires = true;

    @Builder.Default
    private Boolean protectionsRetablies = true;

    @Builder.Default
    private Boolean personnelEvacue = true;
}
