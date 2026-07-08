package com.ocp.at.mapper;

import com.ocp.at.dto.request.OrdreTravailRequest;
import com.ocp.at.dto.response.OrdreTravailResponse;
import com.ocp.at.entity.Installation;
import com.ocp.at.entity.OrdreTravail;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.VisitePrealable;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class OrdreTravailMapperImpl implements OrdreTravailMapper {

    @Override
    public OrdreTravail toEntity(OrdreTravailRequest request) {
        if ( request == null ) {
            return null;
        }

        OrdreTravail.OrdreTravailBuilder ordreTravail = OrdreTravail.builder();

        ordreTravail.objet( request.getObjet() );
        ordreTravail.description( request.getDescription() );
        ordreTravail.dateExecution( request.getDateExecution() );
        ordreTravail.typeIntervention( request.getTypeIntervention() );
        ordreTravail.niveauIntervention( request.getNiveauIntervention() );

        return ordreTravail.build();
    }

    @Override
    public OrdreTravailResponse toResponse(OrdreTravail entity) {
        if ( entity == null ) {
            return null;
        }

        OrdreTravailResponse.OrdreTravailResponseBuilder ordreTravailResponse = OrdreTravailResponse.builder();

        ordreTravailResponse.demandeurId( entityDemandeurId( entity ) );
        ordreTravailResponse.installationId( entityInstallationId( entity ) );
        ordreTravailResponse.installationNom( entityInstallationNomInstallation( entity ) );
        ordreTravailResponse.visitePrealableId( entityVisitePrealableId( entity ) );
        ordreTravailResponse.visiteEffectuee( entityVisitePrealableEffectuee( entity ) );
        ordreTravailResponse.id( entity.getId() );
        ordreTravailResponse.numero( entity.getNumero() );
        ordreTravailResponse.objet( entity.getObjet() );
        ordreTravailResponse.description( entity.getDescription() );
        ordreTravailResponse.dateCreation( entity.getDateCreation() );
        ordreTravailResponse.dateExecution( entity.getDateExecution() );
        ordreTravailResponse.statut( entity.getStatut() );
        ordreTravailResponse.typeIntervention( entity.getTypeIntervention() );
        ordreTravailResponse.niveauIntervention( entity.getNiveauIntervention() );

        ordreTravailResponse.demandeurNomComplet( entity.getDemandeur() != null ? entity.getDemandeur().getNom() + " " + entity.getDemandeur().getPrenom() : null );

        return ordreTravailResponse.build();
    }

    private String entityDemandeurId(OrdreTravail ordreTravail) {
        if ( ordreTravail == null ) {
            return null;
        }
        Utilisateur demandeur = ordreTravail.getDemandeur();
        if ( demandeur == null ) {
            return null;
        }
        String id = demandeur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityInstallationId(OrdreTravail ordreTravail) {
        if ( ordreTravail == null ) {
            return null;
        }
        Installation installation = ordreTravail.getInstallation();
        if ( installation == null ) {
            return null;
        }
        String id = installation.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityInstallationNomInstallation(OrdreTravail ordreTravail) {
        if ( ordreTravail == null ) {
            return null;
        }
        Installation installation = ordreTravail.getInstallation();
        if ( installation == null ) {
            return null;
        }
        String nomInstallation = installation.getNomInstallation();
        if ( nomInstallation == null ) {
            return null;
        }
        return nomInstallation;
    }

    private String entityVisitePrealableId(OrdreTravail ordreTravail) {
        if ( ordreTravail == null ) {
            return null;
        }
        VisitePrealable visitePrealable = ordreTravail.getVisitePrealable();
        if ( visitePrealable == null ) {
            return null;
        }
        String id = visitePrealable.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private boolean entityVisitePrealableEffectuee(OrdreTravail ordreTravail) {
        if ( ordreTravail == null ) {
            return false;
        }
        VisitePrealable visitePrealable = ordreTravail.getVisitePrealable();
        if ( visitePrealable == null ) {
            return false;
        }
        boolean effectuee = visitePrealable.isEffectuee();
        return effectuee;
    }
}
