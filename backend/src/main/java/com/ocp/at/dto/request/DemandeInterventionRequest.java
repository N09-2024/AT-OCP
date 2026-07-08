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
public class DemandeInterventionRequest {

    @NotBlank(message = "L'objet de la demande est obligatoire")
    private String objet;

    private String description;

    private String priorite;

    @NotNull(message = "Le type d'intervention est obligatoire")
    private TypeIntervention typeIntervention;

    @NotNull(message = "Le niveau d'intervention est obligatoire")
    private NiveauIntervention niveauIntervention;

    /** ID de l'installation concernée */
    private String installationId;

    /** ID de l'équipement concerné (optionnel) */
    private String equipementId;
}
