package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EssaiRequest {

    @NotBlank(message = "Le nom de l'essai est obligatoire")
    private String nom;

    private String description;

    private String resultat;

    private Boolean conforme = false;

    private String commentaire;
}
