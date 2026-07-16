package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TypePermisRequest {

    @NotBlank(message = "Le nom du type est obligatoire")
    private String nom;

    private String description;
}
