package com.ocp.at.dto.response;

import lombok.Data;

@Data
public class EssaiResponse {
    private String id;
    private String nom;
    private String description;
    private String resultat;
    private Boolean conforme;
    private String commentaire;
}
