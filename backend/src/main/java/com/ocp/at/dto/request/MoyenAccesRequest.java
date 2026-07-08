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
public class MoyenAccesRequest {
    @NotBlank(message = "Le champ nomMoyen est obligatoire")
    private String nomMoyen;
    private String descriptionMoyen;
}

