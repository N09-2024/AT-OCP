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
public class MesurePreparationRequest {
    @NotBlank(message = "Le champ nomMesure est obligatoire")
    private String nomMesure;
    private String descriptionMesure;
}

