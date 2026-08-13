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

    // Nom CEEP et CEEE pour la section G
    private String g1NomCeep;
    private String g1NomCeee;
    private LocalDateTime dateReceptionCeee;

    // Visite Préalable (§8.2 Standard OCP)
    private Double latitude;
    private Double longitude;
    private String visiteCommentaire;
    private Boolean visiteEffectuee;
    private String photoPath;

    private java.util.List<String> risquesIds;
    private java.util.List<String> mesuresIds;
    private java.util.List<String> episIds;
    private java.util.List<String> moyensAccesIds;
    private java.util.List<String> permisIds;

    // Export PDF conditionnel (HM + HC + Permis conformes)
    private Boolean exportPdfAutorise;
    private java.util.List<String> exportPdfMotifsRefus;
}

