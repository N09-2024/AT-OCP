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
    
    // Zones territoriales P/E (standard OCP S-HSE-SEC-31)
    private String zoneProprietaireId;
    private String zoneProprietaireNom;
    private String zoneExecutanteId;
    private String zoneExecutanteNom;

    private LocalDateTime datePriseVerrou;
    private LocalDateTime dateLiberationVerrou;

    // Document source (DI / OT / BT)
    private String typeDocumentSource;
    private String documentSourceId;
    private String documentSourceNumero;

    private String servicesIntervenants;
    private String entreprisesIntervenantes;
    private String mesuresSecuriteExecutant;

    // We can just return basic DTOs or lists of IDs. Let's return objects or just IDs to keep it simple.
    // Assuming simple Response inner classes or basic types for simplicity. For now, returning objects might be complex. Let's return IDs or basic info.
    // Actually the easiest is to just return a map or list of strings/objects if they exist in the project, but we don't have them here. I will just add lists of strings for the IDs for the frontend to know what's selected.
    private java.util.List<String> risquesIds;
    private java.util.List<String> mesuresIds;
    private java.util.List<String> episIds;
    private java.util.List<String> moyensAccesIds;
    private java.util.List<String> permisIds;

    // Export PDF conditionnel (HM + HC + Permis conformes)
    private Boolean exportPdfAutorise;
    private java.util.List<String> exportPdfMotifsRefus;
}

