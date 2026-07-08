package com.ocp.at.mapper;

import com.ocp.at.dto.response.VisaResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.Visa;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class VisaMapperImpl implements VisaMapper {

    @Override
    public VisaResponse toResponse(Visa visa) {
        if ( visa == null ) {
            return null;
        }

        VisaResponse.VisaResponseBuilder visaResponse = VisaResponse.builder();

        visaResponse.utilisateurId( visaUtilisateurId( visa ) );
        visaResponse.autorisationTravailId( visaAutorisationTravailId( visa ) );
        visaResponse.id( visa.getId() );
        visaResponse.dateVisa( visa.getDateVisa() );
        visaResponse.dateSignature( visa.getDateSignature() );
        visaResponse.statut( visa.getStatut() );
        visaResponse.commentaire( visa.getCommentaire() );
        visaResponse.ordre( visa.getOrdre() );
        visaResponse.adresseIP( visa.getAdresseIP() );

        visaResponse.utilisateurNomComplet( visa.getUtilisateur().getPrenom() + " " + visa.getUtilisateur().getNom() );
        visaResponse.signaturePresente( visa.getSignaturePath() != null );

        return visaResponse.build();
    }

    private String visaUtilisateurId(Visa visa) {
        if ( visa == null ) {
            return null;
        }
        Utilisateur utilisateur = visa.getUtilisateur();
        if ( utilisateur == null ) {
            return null;
        }
        String id = utilisateur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String visaAutorisationTravailId(Visa visa) {
        if ( visa == null ) {
            return null;
        }
        AutorisationTravail autorisationTravail = visa.getAutorisationTravail();
        if ( autorisationTravail == null ) {
            return null;
        }
        String id = autorisationTravail.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
