package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PhotoReceptionRequest {

    @NotBlank(message = "Le nom de la photo est obligatoire")
    @Size(max = 255, message = "Le nom ne peut pas dépasser 255 caractères")
    private String nom;

    @NotBlank(message = "Le chemin de la photo est obligatoire")
    @Size(max = 500, message = "Le chemin ne peut pas dépasser 500 caractères")
    private String path;

    private Long taille;

    @Size(max = 100, message = "Le type MIME ne peut pas dépasser 100 caractères")
    private String mimeType;

    private Integer ordre;

    @Size(max = 500, message = "La légende ne peut pas dépasser 500 caractères")
    private String legende;
}
