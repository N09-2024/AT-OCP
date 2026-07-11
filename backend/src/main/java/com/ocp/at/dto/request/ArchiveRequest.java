package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveRequest {

    @NotNull(message = "L'ID de l'autorisation de travail est obligatoire")
    private String autorisationTravailId;

    private String commentaire;
}
