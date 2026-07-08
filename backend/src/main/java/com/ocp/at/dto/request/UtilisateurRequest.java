package com.ocp.at.dto.request;

import com.ocp.at.validation.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UtilisateurRequest {

    @NotBlank(message = "Le matricule est obligatoire")
    @Size(min = 3, max = 20, message = "Le matricule doit comporter entre 3 et 20 caractères")
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @PasswordPolicy
    private String motDePasse;

    private String photo;
}
