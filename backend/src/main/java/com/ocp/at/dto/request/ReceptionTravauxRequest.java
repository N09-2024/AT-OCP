package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptionTravauxRequest {

    @NotNull(message = "L'identifiant de l'AT est obligatoire")
    private String autorisationTravailId;

    private String responsableId;

    private String dateReception;

    // Dates envoyées depuis le frontend au format YYYY-MM-DD
    private String dateDebutTravauxReelle;

    private String dateFinTravauxReelle;

    @Size(max = 2000, message = "La description des travaux réalisés ne peut pas dépasser 2000 caractères")
    private String travauxRealises;

    private Boolean travauxConformes = false;

    private Boolean equipementRemisEnService = false;

    private Boolean zoneNettoyee = false;

    private Boolean consignationRetiree = false;

    private Boolean essaisEffectues = false;

    // Champs additionnels (checklist frontend)
    private Boolean essaisConformes = false;

    private Boolean installationRemiseEnEtat = false;

    @Size(max = 1000, message = "Le résultat des essais ne peut pas dépasser 1000 caractères")
    private String resultatEssais;

    @Size(max = 2000, message = "Les observations ne peuvent pas dépasser 2000 caractères")
    private String observations;

    @Size(max = 1000, message = "Le commentaire du responsable ne peut pas dépasser 1000 caractères")
    @com.fasterxml.jackson.annotation.JsonAlias("commentaires")
    private String commentaireResponsable;
}
