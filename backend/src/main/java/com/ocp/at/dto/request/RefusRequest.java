package com.ocp.at.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Requête de refus d'AT")
public class RefusRequest {

    @NotBlank(message = "Le commentaire est obligatoire en cas de refus")
    @Schema(description = "Motif du refus de l'Autorisation de Travail")
    private String commentaire;
}
