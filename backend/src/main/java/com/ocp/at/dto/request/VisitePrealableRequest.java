package com.ocp.at.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Requête de création ou modification d'une Visite Préalable")
public class VisitePrealableRequest {

    @NotBlank(message = "L'ID du document source est obligatoire")
    @Schema(description = "ID du document source (DI, OT ou BT)")
    private String documentSourceId;

    @NotNull(message = "Le type du document source est obligatoire")
    @Schema(description = "Type du document source: DI, OT, BT", allowableValues = {"DI", "OT", "BT"})
    private String typeDocumentSource;

    @Schema(description = "ID de l'utilisateur visiteur (optionnel à la création)")
    private String visiteurId;

    @Schema(description = "Date et heure de début de la visite")
    private LocalDateTime dateHeureDebut;

    @Schema(description = "Date et heure de fin de la visite")
    private LocalDateTime dateHeureFin;

    @Schema(description = "Latitude GPS du lieu de visite")
    private Double latitude;

    @Schema(description = "Longitude GPS du lieu de visite")
    private Double longitude;

    @Schema(description = "Commentaire ou observations de la visite")
    private String commentaire;

    @Schema(description = "IDs des risques identifiés lors de la visite")
    private List<String> risquesIdentifiesIds;
}
