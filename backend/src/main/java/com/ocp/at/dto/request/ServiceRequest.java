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
public class ServiceRequest {
    @NotBlank(message = "Le champ nomService est obligatoire")
    private String nomService;
    private String descriptionService;
    @NotBlank(message = "Le champ codeService est obligatoire")
    private String codeService;
    private String zoneId;
}

