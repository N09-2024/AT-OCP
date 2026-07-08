package com.ocp.at.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReceptionTravauxResponse {

    private String id;
    private String autorisationTravailId;
    private String autorisationTravailNumero;
    private LocalDateTime dateReception;
    private String commentaire;
    private Boolean travauxConformes;
    private Boolean installationRemiseEnEtat;
    private Boolean essaisEffectues;
    private Boolean essaisConformes;
    private Boolean validee;
    private LocalDateTime dateValidation;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EssaiResponse> essais;
    private RemiseEtatResponse remiseEtat;
}
