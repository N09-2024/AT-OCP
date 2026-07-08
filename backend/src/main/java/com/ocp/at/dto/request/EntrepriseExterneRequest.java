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
public class EntrepriseExterneRequest {
    @NotBlank(message = "Le champ nomEntreprise est obligatoire")
    private String nomEntreprise;
    private String adresse;
    private String telephone;
    private String responsable;
}

