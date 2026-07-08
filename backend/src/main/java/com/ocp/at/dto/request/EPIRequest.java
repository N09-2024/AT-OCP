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
public class EPIRequest {
    @NotBlank(message = "Le champ nomEPI est obligatoire")
    private String nomEPI;
    private String descriptionEPI;
}

