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
public class DemandeInterventionResponse {

    private String id;
    private String numero;
    private String objet;
    private String description;
    private String priorite;
    private LocalDateTime dateDemande;
    private StatutDocument statut;
    private TypeIntervention typeIntervention;
    private NiveauIntervention niveauIntervention;

    /** Indique si l'AT peut être créée (NIVEAU_2 + visite + analyse terminées) */
    private boolean atCreable;

    // Sous-objets résumés
    private String demandeurId;
    private String demandeurNomComplet;
    private String equipementId;
    private String equipementNom;

    /** ID de la visite préalable si elle existe */
    private String visitePrealableId;
    private boolean visiteEffectuee;
}
