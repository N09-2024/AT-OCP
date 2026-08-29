package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.StatutReconduction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconductionResponse {
    private String id;
    private String autorisationTravailId;
    private String autorisationTravailNumero;
    private String demandeurId;
    private String demandeurNomComplet;
    private String demandeurRole;
    private LocalDateTime dateDemande;
    private LocalDateTime dateFinInitiale;
    private LocalDateTime nouvelleDateFin;
    private String motif;
    private StatutReconduction statut;
    private String decisionParId;
    private String decisionParNomComplet;
    private LocalDateTime dateDecision;
    private String motifRefus;
    private String commentaire;
    private String analyseIaJson;
    private LocalDateTime createdAt;
}
