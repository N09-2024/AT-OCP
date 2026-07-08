package com.ocp.at.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Réponse contenant les informations d'une Analyse des Risques")
public class AnalyseRisqueResponse {

    private String id;

    private LocalDateTime dateAnalyse;

    private String commentaire;

    // Visite préalable associée
    private String visitePrealableId;

    // Analyseur
    private String analyseurId;
    private String analyseurNomComplet;

    // Référentiels associés (IDs + noms)
    private List<String> risquesIds;
    private List<String> risquesNoms;

    private List<String> mesuresIds;
    private List<String> mesuresNoms;

    private List<String> episIds;
    private List<String> episNoms;

    private List<String> moyensAccesIds;
    private List<String> moyensAccesNoms;
}
