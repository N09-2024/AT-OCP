package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.StatutDocument;
import com.ocp.at.entity.enums.TypeIntervention;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonTravailResponse {

    private String id;
    private String numero;
    private String objet;
    private String description;
    private LocalDateTime dateEmission;
    private StatutDocument statut;
    private TypeIntervention typeIntervention;
    private NiveauIntervention niveauIntervention;

    private boolean atCreable;

    private String entrepriseExterneId;
    private String entrepriseExterneNom;

    private String demandeurId;
    private String demandeurNomComplet;

    private String visitePrealableId;
    private boolean visiteEffectuee;
}
