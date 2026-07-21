package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.ocp.at.validation.PasswordPolicy;

@Data
public class ChangePasswordRequest {
    
    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String ancienMotDePasse;
    
    @PasswordPolicy
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    private String nouveauMotDePasse;
}
