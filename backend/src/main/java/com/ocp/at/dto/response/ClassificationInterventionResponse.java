package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationInterventionResponse {
    private String id;
    private String reference;
    private String niveau;
    private Boolean estTiers;
    private String natureIntervention;
    private String zoneId;
    private String zoneNom;
    private String serviceId;
    private String serviceNom;
    private String classifieParId;
    private String classifieParNomComplet;
    private LocalDateTime dateClassification;
    private String observations;
    private String statut;
    private String autorisationTravailId;
    private String autorisationTravailNumero;
}
