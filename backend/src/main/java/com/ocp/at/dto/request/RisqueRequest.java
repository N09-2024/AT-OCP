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
public class RisqueRequest {
    @NotBlank(message = "Le champ nomRisque est obligatoire")
    private String nomRisque;
    private String descriptionRisque;
    private String niveau;
}

