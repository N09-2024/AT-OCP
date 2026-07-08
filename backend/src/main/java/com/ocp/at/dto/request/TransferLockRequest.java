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
@Schema(description = "Requête de transfert de verrou d'AT")
public class TransferLockRequest {

    @NotBlank(message = "L'ID de l'utilisateur destinataire est obligatoire")
    @Schema(description = "ID de l'utilisateur à qui transférer le verrou")
    private String nouvelUtilisateurId;
}
