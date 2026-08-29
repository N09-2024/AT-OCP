package com.ocp.at.dto.request;

import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.TypeIntervention;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonTravailRequest {

    @NotBlank(message = "L'objet du bon de travail est obligatoire")
    private String objet;

    private String description;

    @NotNull(message = "Le type d'intervention est obligatoire")
    private TypeIntervention typeIntervention;

    @NotNull(message = "Le niveau d'intervention est obligatoire")
    private NiveauIntervention niveauIntervention;

    @NotBlank(message = "L'entreprise externe est obligatoire pour un Bon de Travail")
    private String entrepriseExterneId;
}
