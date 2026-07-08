package com.ocp.at.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'une Visite Préalable")
public class VisitePrealableResponse {

    private String id;

    private LocalDateTime dateHeureDebut;

    private LocalDateTime dateHeureFin;

    private Double latitude;

    private Double longitude;

    private String commentaire;

    /** Indique si la visite a été finalisée (verrouillée) */
    private boolean effectuee;

    /** Indique si l'analyse des risques a été créée après cette visite */
    private boolean analyseCreee;

    // Visiteur
    private String visiteurId;
    private String visiteurNomComplet;

    // Document source
    private String documentSourceId;
    private String typeDocumentSource;
    private String documentSourceNumero;

    // Photos
    private List<PhotoResponse> photos;
    private int nombrePhotos;

    // Risques pré-identifiés
    private List<String> risquesIdentifiesIds;

    // ID de l'analyse si créée
    private String analyseRisqueId;
}
