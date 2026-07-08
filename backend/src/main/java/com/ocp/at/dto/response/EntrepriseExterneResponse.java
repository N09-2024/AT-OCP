package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseExterneResponse {
    private String id;
    private String nomEntreprise;
    private String adresse;
    private String telephone;
    private String responsable;
}

