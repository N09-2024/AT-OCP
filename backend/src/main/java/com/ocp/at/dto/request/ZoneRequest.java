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
public class ZoneRequest {
    @NotBlank(message = "Le champ nomZone est obligatoire")
    private String nomZone;
    private String descriptionZone;
    @NotBlank(message = "Le champ codeZone est obligatoire")
    private String codeZone;
}

