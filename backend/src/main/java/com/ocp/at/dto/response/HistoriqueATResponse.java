package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HistoriqueATResponse {
    private String id;
    private LocalDateTime dateAction;
    private TypeActionAT action;
    private StatutAT ancienStatut;
    private StatutAT nouveauStatut;
    private String commentaire;
    private String utilisateurId;
    private String utilisateurNomComplet;
    private String atId;
}
