package com.ocp.at.mapper;

import com.ocp.at.dto.request.BonTravailRequest;
import com.ocp.at.dto.response.BonTravailResponse;
import com.ocp.at.entity.BonTravail;
import com.ocp.at.entity.EntrepriseExterne;
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
public class BonTravailMapperImpl implements BonTravailMapper {

    @Override
    public BonTravail toEntity(BonTravailRequest request) {
        if ( request == null ) {
            return null;
        }

        BonTravail.BonTravailBuilder bonTravail = BonTravail.builder();

        bonTravail.objet( request.getObjet() );
        bonTravail.description( request.getDescription() );
        bonTravail.typeIntervention( request.getTypeIntervention() );
        bonTravail.niveauIntervention( request.getNiveauIntervention() );

        return bonTravail.build();
    }

    @Override
    public BonTravailResponse toResponse(BonTravail entity) {
        if ( entity == null ) {
            return null;
        }

        BonTravailResponse.BonTravailResponseBuilder bonTravailResponse = BonTravailResponse.builder();

        bonTravailResponse.entrepriseExterneId( entityEntrepriseExterneId( entity ) );
        bonTravailResponse.entrepriseExterneNom( entityEntrepriseExterneNomEntreprise( entity ) );
        bonTravailResponse.demandeurId( entityDemandeurId( entity ) );
        bonTravailResponse.installationId( entityInstallationId( entity ) );
        bonTravailResponse.installationNom( entityInstallationNomInstallation( entity ) );
        bonTravailResponse.visitePrealableId( entityVisitePrealableId( entity ) );
        bonTravailResponse.visiteEffectuee( entityVisitePrealableEffectuee( entity ) );
        bonTravailResponse.id( entity.getId() );
        bonTravailResponse.numero( entity.getNumero() );
        bonTravailResponse.objet( entity.getObjet() );
        bonTravailResponse.description( entity.getDescription() );
        bonTravailResponse.dateEmission( entity.getDateEmission() );
        bonTravailResponse.statut( entity.getStatut() );
        bonTravailResponse.typeIntervention( entity.getTypeIntervention() );
        bonTravailResponse.niveauIntervention( entity.getNiveauIntervention() );

        bonTravailResponse.demandeurNomComplet( entity.getDemandeur() != null ? entity.getDemandeur().getNom() + " " + entity.getDemandeur().getPrenom() : null );

        return bonTravailResponse.build();
    }

    private String entityEntrepriseExterneId(BonTravail bonTravail) {
        if ( bonTravail == null ) {
            return null;
        }
        EntrepriseExterne entrepriseExterne = bonTravail.getEntrepriseExterne();
        if ( entrepriseExterne == null ) {
            return null;
        }
        String id = entrepriseExterne.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityEntrepriseExterneNomEntreprise(BonTravail bonTravail) {
        if ( bonTravail == null ) {
            return null;
        }
        EntrepriseExterne entrepriseExterne = bonTravail.getEntrepriseExterne();
        if ( entrepriseExterne == null ) {
            return null;
        }
        String nomEntreprise = entrepriseExterne.getNomEntreprise();
        if ( nomEntreprise == null ) {
            return null;
        }
        return nomEntreprise;
    }

    private String entityDemandeurId(BonTravail bonTravail) {
        if ( bonTravail == null ) {
            return null;
        }
        Utilisateur demandeur = bonTravail.getDemandeur();
        if ( demandeur == null ) {
            return null;
        }
        String id = demandeur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityInstallationId(BonTravail bonTravail) {
        if ( bonTravail == null ) {
            return null;
        }
        Installation installation = bonTravail.getInstallation();
        if ( installation == null ) {
            return null;
        }
        String id = installation.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityInstallationNomInstallation(BonTravail bonTravail) {
        if ( bonTravail == null ) {
            return null;
        }
        Installation installation = bonTravail.getInstallation();
        if ( installation == null ) {
            return null;
        }
        String nomInstallation = installation.getNomInstallation();
        if ( nomInstallation == null ) {
            return null;
        }
        return nomInstallation;
    }

    private String entityVisitePrealableId(BonTravail bonTravail) {
        if ( bonTravail == null ) {
            return null;
        }
        VisitePrealable visitePrealable = bonTravail.getVisitePrealable();
        if ( visitePrealable == null ) {
            return null;
        }
        String id = visitePrealable.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private boolean entityVisitePrealableEffectuee(BonTravail bonTravail) {
        if ( bonTravail == null ) {
            return false;
        }
        VisitePrealable visitePrealable = bonTravail.getVisitePrealable();
        if ( visitePrealable == null ) {
            return false;
        }
        boolean effectuee = visitePrealable.isEffectuee();
        return effectuee;
    }
}
