package com.ocp.at.mapper;

import com.ocp.at.dto.request.DemandeInterventionRequest;
import com.ocp.at.dto.response.DemandeInterventionResponse;
import com.ocp.at.entity.DemandeIntervention;
import com.ocp.at.entity.Equipement;
import com.ocp.at.entity.Installation;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.VisitePrealable;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class DemandeInterventionMapperImpl implements DemandeInterventionMapper {

    @Override
    public DemandeIntervention toEntity(DemandeInterventionRequest request) {
        if ( request == null ) {
            return null;
        }

        DemandeIntervention.DemandeInterventionBuilder demandeIntervention = DemandeIntervention.builder();

        demandeIntervention.objet( request.getObjet() );
        demandeIntervention.description( request.getDescription() );
        demandeIntervention.priorite( request.getPriorite() );
        demandeIntervention.typeIntervention( request.getTypeIntervention() );
        demandeIntervention.niveauIntervention( request.getNiveauIntervention() );

        return demandeIntervention.build();
    }

    @Override
    public DemandeInterventionResponse toResponse(DemandeIntervention entity) {
        if ( entity == null ) {
            return null;
        }

        DemandeInterventionResponse.DemandeInterventionResponseBuilder demandeInterventionResponse = DemandeInterventionResponse.builder();

        demandeInterventionResponse.demandeurId( entityDemandeurId( entity ) );
        demandeInterventionResponse.installationId( entityInstallationId( entity ) );
        demandeInterventionResponse.installationNom( entityInstallationNomInstallation( entity ) );
        demandeInterventionResponse.equipementId( entityEquipementId( entity ) );
        demandeInterventionResponse.equipementNom( entityEquipementNomEquipement( entity ) );
        demandeInterventionResponse.visitePrealableId( entityVisitePrealableId( entity ) );
        demandeInterventionResponse.visiteEffectuee( entityVisitePrealableEffectuee( entity ) );
        demandeInterventionResponse.id( entity.getId() );
        demandeInterventionResponse.numero( entity.getNumero() );
        demandeInterventionResponse.objet( entity.getObjet() );
        demandeInterventionResponse.description( entity.getDescription() );
        demandeInterventionResponse.priorite( entity.getPriorite() );
        demandeInterventionResponse.dateDemande( entity.getDateDemande() );
        demandeInterventionResponse.statut( entity.getStatut() );
        demandeInterventionResponse.typeIntervention( entity.getTypeIntervention() );
        demandeInterventionResponse.niveauIntervention( entity.getNiveauIntervention() );

        demandeInterventionResponse.demandeurNomComplet( entity.getDemandeur() != null ? entity.getDemandeur().getNom() + " " + entity.getDemandeur().getPrenom() : null );

        return demandeInterventionResponse.build();
    }

    private String entityDemandeurId(DemandeIntervention demandeIntervention) {
        if ( demandeIntervention == null ) {
            return null;
        }
        Utilisateur demandeur = demandeIntervention.getDemandeur();
        if ( demandeur == null ) {
            return null;
        }
        String id = demandeur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityInstallationId(DemandeIntervention demandeIntervention) {
        if ( demandeIntervention == null ) {
            return null;
        }
        Installation installation = demandeIntervention.getInstallation();
        if ( installation == null ) {
            return null;
        }
        String id = installation.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityInstallationNomInstallation(DemandeIntervention demandeIntervention) {
        if ( demandeIntervention == null ) {
            return null;
        }
        Installation installation = demandeIntervention.getInstallation();
        if ( installation == null ) {
            return null;
        }
        String nomInstallation = installation.getNomInstallation();
        if ( nomInstallation == null ) {
            return null;
        }
        return nomInstallation;
    }

    private String entityEquipementId(DemandeIntervention demandeIntervention) {
        if ( demandeIntervention == null ) {
            return null;
        }
        Equipement equipement = demandeIntervention.getEquipement();
        if ( equipement == null ) {
            return null;
        }
        String id = equipement.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityEquipementNomEquipement(DemandeIntervention demandeIntervention) {
        if ( demandeIntervention == null ) {
            return null;
        }
        Equipement equipement = demandeIntervention.getEquipement();
        if ( equipement == null ) {
            return null;
        }
        String nomEquipement = equipement.getNomEquipement();
        if ( nomEquipement == null ) {
            return null;
        }
        return nomEquipement;
    }

    private String entityVisitePrealableId(DemandeIntervention demandeIntervention) {
        if ( demandeIntervention == null ) {
            return null;
        }
        VisitePrealable visitePrealable = demandeIntervention.getVisitePrealable();
        if ( visitePrealable == null ) {
            return null;
        }
        String id = visitePrealable.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private boolean entityVisitePrealableEffectuee(DemandeIntervention demandeIntervention) {
        if ( demandeIntervention == null ) {
            return false;
        }
        VisitePrealable visitePrealable = demandeIntervention.getVisitePrealable();
        if ( visitePrealable == null ) {
            return false;
        }
        boolean effectuee = visitePrealable.isEffectuee();
        return effectuee;
    }
}
