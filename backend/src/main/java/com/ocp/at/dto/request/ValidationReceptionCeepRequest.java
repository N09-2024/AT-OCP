package com.ocp.at.dto.request;

import com.ocp.at.entity.enums.ResultatReception;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationReceptionCeepRequest {

    @NotNull(message = "Le résultat du contrôle de réception est obligatoire")
    private ResultatReception resultat;

    private String reservesDescription;

    private String actionsCorrectives;

    private String observations;

    @Builder.Default
    private Boolean travauxConformes = true;

    @Builder.Default
    private Boolean zoneNettoyee = true;

    @Builder.Default
    private Boolean equipementRemisEnService = true;

    @Builder.Default
    private Boolean consignationRetiree = true;

    @Builder.Default
    private Boolean essaisEffectues = false;

    private String resultatEssais;
}
