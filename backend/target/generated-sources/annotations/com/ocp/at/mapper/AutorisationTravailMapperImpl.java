package com.ocp.at.mapper;

import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Utilisateur;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class AutorisationTravailMapperImpl implements AutorisationTravailMapper {

    @Override
    public AutorisationTravailResponse toResponse(AutorisationTravail entity) {
        if ( entity == null ) {
            return null;
        }

        AutorisationTravailResponse.AutorisationTravailResponseBuilder autorisationTravailResponse = AutorisationTravailResponse.builder();

        autorisationTravailResponse.proprietaireBrouillonId( entityProprietaireBrouillonId( entity ) );
        autorisationTravailResponse.id( entity.getId() );
        autorisationTravailResponse.numero( entity.getNumero() );
        autorisationTravailResponse.version( entity.getVersion() );
        autorisationTravailResponse.objet( entity.getObjet() );
        autorisationTravailResponse.descriptionTravaux( entity.getDescriptionTravaux() );
        autorisationTravailResponse.dateDebut( entity.getDateDebut() );
        autorisationTravailResponse.dateFin( entity.getDateFin() );
        autorisationTravailResponse.heureDebut( entity.getHeureDebut() );
        autorisationTravailResponse.heureFin( entity.getHeureFin() );
        autorisationTravailResponse.statut( entity.getStatut() );
        autorisationTravailResponse.etatVerrou( entity.getEtatVerrou() );
        autorisationTravailResponse.dateCreation( entity.getDateCreation() );
        autorisationTravailResponse.dateModification( entity.getDateModification() );
        autorisationTravailResponse.datePriseVerrou( entity.getDatePriseVerrou() );
        autorisationTravailResponse.dateLiberationVerrou( entity.getDateLiberationVerrou() );

        autorisationTravailResponse.proprietaireBrouillonNomComplet( entity.getProprietaireBrouillon() != null ? entity.getProprietaireBrouillon().getNom() + " " + entity.getProprietaireBrouillon().getPrenom() : null );

        return autorisationTravailResponse.build();
    }

    private String entityProprietaireBrouillonId(AutorisationTravail autorisationTravail) {
        if ( autorisationTravail == null ) {
            return null;
        }
        Utilisateur proprietaireBrouillon = autorisationTravail.getProprietaireBrouillon();
        if ( proprietaireBrouillon == null ) {
            return null;
        }
        String id = proprietaireBrouillon.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
