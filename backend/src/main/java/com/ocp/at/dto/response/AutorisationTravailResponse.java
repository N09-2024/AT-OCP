package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.EtatVerrou;
import com.ocp.at.entity.enums.StatutAT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorisationTravailResponse {

    private String id;
    private String numero;
    private Integer version;
    
    private String objet;
    private String descriptionTravaux;
    
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    
    private StatutAT statut;
    private EtatVerrou etatVerrou;
    
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    
    private String proprietaireBrouillonId;
    private String proprietaireBrouillonNomComplet;
    
    private LocalDateTime datePriseVerrou;
    private LocalDateTime dateLiberationVerrou;

    // Document source (DI / OT / BT)
    private String typeDocumentSource;
    private String documentSourceId;
    private String documentSourceNumero;
}
