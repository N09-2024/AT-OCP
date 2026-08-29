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
public class EquipementRequest {
    @NotBlank(message = "Le champ nomEquipement est obligatoire")
    private String nomEquipement;
    @NotBlank(message = "Le champ codeEquipement est obligatoire")
    private String codeEquipement;
    private String descriptionEquipement;
}

