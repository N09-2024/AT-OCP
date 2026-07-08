package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.StatutVisa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaResponse {
    private String id;
    private LocalDateTime dateVisa;
    private LocalDateTime dateSignature;
    private StatutVisa statut;
    private String commentaire;
    private Integer ordre;
    // signatureHash intentionnellement ABSENT des réponses publiques
    private boolean signaturePresente;
    private String adresseIP;
    private String utilisateurId;
    private String utilisateurNomComplet;
    private String autorisationTravailId;
}
