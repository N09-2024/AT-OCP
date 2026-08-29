package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipementResponse {
    private String id;
    private String nomEquipement;
    private String codeEquipement;
    private String descriptionEquipement;
}

