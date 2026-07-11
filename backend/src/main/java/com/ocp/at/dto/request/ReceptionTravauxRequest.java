package com.ocp.at.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptionTravauxRequest {

    @NotNull(message = "L'identifiant de l'AT est obligatoire")
    private String autorisationTravailId;

    private String responsableId;

    @PastOrPresent(message = "La date de réception ne peut pas être dans le futur")
    private LocalDateTime dateReception;

    @PastOrPresent(message = "La date de début réelle ne peut pas être dans le futur")
    private LocalDateTime dateDebutTravauxReelle;

    @PastOrPresent(message = "La date de fin réelle ne peut pas être dans le futur")
    private LocalDateTime dateFinTravauxReelle;

    @Size(max = 2000, message = "La description des travaux réalisés ne peut pas dépasser 2000 caractères")
    private String travauxRealises;

    private Boolean travauxConformes = false;

    private Boolean equipementRemisEnService = false;

    private Boolean zoneNettoyee = false;

    private Boolean consignationRetiree = false;

    private Boolean essaisEffectues = false;

    @Size(max = 1000, message = "Le résultat des essais ne peut pas dépasser 1000 caractères")
    private String resultatEssais;

    @Size(max = 2000, message = "Les observations ne peuvent pas dépasser 2000 caractères")
    private String observations;

    @Size(max = 1000, message = "Le commentaire du responsable ne peut pas dépasser 1000 caractères")
    private String commentaireResponsable;
}
