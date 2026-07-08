package com.ocp.at.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Requête de création ou modification d'une Analyse des Risques")
public class AnalyseRisqueRequest {

    @NotBlank(message = "L'ID de la visite préalable est obligatoire")
    @Schema(description = "ID de la VisitePrealable associée (doit être effectuée)")
    private String visitePrealableId;

    @Schema(description = "ID de l'analyseur (utilisateur)")
    private String analyseurId;

    @Schema(description = "Commentaire ou conclusion de l'analyse")
    private String commentaire;

    @NotEmpty(message = "Au moins un risque est obligatoire")
    @Schema(description = "IDs des risques identifiés")
    private List<String> risquesIds;

    @NotEmpty(message = "Au moins une mesure est obligatoire")
    @Schema(description = "IDs des mesures de prévention")
    private List<String> mesuresIds;

    @NotEmpty(message = "Au moins un EPI est obligatoire")
    @Schema(description = "IDs des EPI requis")
    private List<String> episIds;

    @NotEmpty(message = "Au moins un moyen d'accès est obligatoire")
    @Schema(description = "IDs des moyens d'accès")
    private List<String> moyensAccesIds;
}
