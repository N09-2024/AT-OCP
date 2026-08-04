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

    @Schema(description = "Services intervenants (libellé)")
    private String servicesIntervenants;

    @Schema(description = "ID du service intervenant (E) — fixe zoneExecutante pour CEEE")
    private String serviceIntervenantId;

    @Schema(description = "ID zone propriétaire (P) optionnel")
    private String zoneProprietaireId;

    @Schema(description = "Entreprises intervenantes")
    private String entreprisesIntervenantes;

    @Schema(description = "Mesures de sécurité de l'exécutant")
    private String mesuresSecuriteExecutant;

    @Schema(description = "Liste des IDs des risques")
    private java.util.List<String> risquesIds;

    @Schema(description = "Liste des IDs des mesures")
    private java.util.List<String> mesuresIds;

    @Schema(description = "Liste des IDs des EPIs")
    private java.util.List<String> episIds;

    @Schema(description = "Liste des IDs des moyens d'accès")
    private java.util.List<String> moyensAccesIds;

    @Schema(description = "Liste des IDs des types de permis nécessaires")
    private java.util.List<String> permisIds;
}
