package com.ocp.at.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Requête d'Auto Save du brouillon d'AT")
public class AutoSaveRequest {

    @Schema(description = "Objet de l'intervention")
    private String objet;

    @Schema(description = "Description détaillée des travaux")
    private String descriptionTravaux;

    @Schema(description = "Date de début prévue")
    private LocalDate dateDebut;

    @Schema(description = "Date de fin prévue")
    private LocalDate dateFin;

    @Schema(description = "Heure de début prévue")
    private LocalTime heureDebut;

    @Schema(description = "Heure de fin prévue")
    private LocalTime heureFin;
}
