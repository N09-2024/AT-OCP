package com.ocp.at.dto.request;

import com.ocp.at.entity.enums.TypePermis;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermisRequest {
    
    @NotNull(message = "Le type de permis est obligatoire")
    private TypePermis type;
    
    private Boolean estObligatoire = false;
    
    private String commentaire;
    
    @NotNull(message = "L'ID de l'Autorisation de Travail est obligatoire")
    private String autorisationTravailId;
}
