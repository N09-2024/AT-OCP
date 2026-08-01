package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabilitationResponse {
    private String id;
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private String utilisateurMatricule;
    private String serviceNom;
    private String serviceCode;
    private String designeParId;
    private String designeParNomComplet;
    private LocalDate dateHabilitation;
    private LocalDate valideJusquAu;
    private Boolean actif;
    private String observations;
    private LocalDateTime dateCreation;
}
