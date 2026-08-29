package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptionTravauxResponse {

    private String id;
    private String autorisationTravailId;
    private String autorisationTravailNumero;
    private String responsableId;
    private String responsableMatricule;
    private String responsableNom;
    private LocalDateTime dateReception;
    private LocalDateTime dateDebutTravauxReelle;
    private LocalDateTime dateFinTravauxReelle;
    private String travauxRealises;
    private Boolean travauxConformes;
    private Boolean equipementRemisEnService;
    private Boolean zoneNettoyee;
    private Boolean consignationRetiree;
    private Boolean essaisEffectues;
    private String resultatEssais;
    private String observations;
    private String commentaireResponsable;
    private String signatureResponsable;
    private LocalDateTime dateSignature;
    private com.ocp.at.entity.enums.ResultatReception resultatReception;
    private String reservesDescription;
    private String actionsCorrectives;
    private Boolean receptionConjointeValidee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PhotoReceptionResponse> photos;
    private Boolean atCloturee;
}
