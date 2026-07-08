package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {

    @NotBlank(message = "Le nom de la permission est obligatoire")
    private String nom;

    private String description;
}
