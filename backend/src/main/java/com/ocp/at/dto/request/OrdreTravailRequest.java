package com.ocp.at.dto.request;

import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.TypeIntervention;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreTravailRequest {

    @NotBlank(message = "L'objet de l'ordre de travail est obligatoire")
    private String objet;

    private String description;

    @NotNull(message = "Le type d'intervention est obligatoire")
    private TypeIntervention typeIntervention;

    @NotNull(message = "Le niveau d'intervention est obligatoire")
    private NiveauIntervention niveauIntervention;

    private LocalDateTime dateExecution;

    /** ID de l'installation concernée */
    private String installationId;
}
