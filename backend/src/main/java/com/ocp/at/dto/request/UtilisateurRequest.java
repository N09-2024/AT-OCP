package com.ocp.at.dto.request;

import com.ocp.at.validation.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UtilisateurRequest {

    // Matricule optionnel - auto-généré si non fourni
    @Size(max = 20, message = "Le matricule ne doit pas dépasser 20 caractères")
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

    private String serviceId;

    // Rôle demandé - utilisé pour la validation métier (service obligatoire hors ADMIN)
    private String roleNom;
}
