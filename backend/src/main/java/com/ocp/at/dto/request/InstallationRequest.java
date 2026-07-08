package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallationRequest {
    @NotBlank(message = "Le champ nomInstallation est obligatoire")
    private String nomInstallation;
    private String atelier;
    private String localisation;
    @NotBlank(message = "Le champ codeInstallation est obligatoire")
    private String codeInstallation;
    private String serviceId;
}

