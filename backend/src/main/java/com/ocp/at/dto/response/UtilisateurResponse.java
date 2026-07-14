package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponse {
    private String id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String photo;
    private boolean actif;
    private boolean compteVerrouille;
    private boolean motDePasseExpire;
    private boolean enAttenteValidation;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private LocalDateTime derniereConnexion;
    private Set<RoleResponse> roles;
}
