package com.ocp.at.mapper;

import com.ocp.at.dto.response.HistoriqueATResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.HistoriqueAT;
import com.ocp.at.entity.Utilisateur;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:12:15+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class HistoriqueATMapperImpl implements HistoriqueATMapper {

    @Override
    public HistoriqueATResponse toResponse(HistoriqueAT h) {
        if ( h == null ) {
            return null;
        }

        HistoriqueATResponse.HistoriqueATResponseBuilder historiqueATResponse = HistoriqueATResponse.builder();

        historiqueATResponse.utilisateurId( hUtilisateurId( h ) );
        historiqueATResponse.atId( hAutorisationTravailId( h ) );
        historiqueATResponse.id( h.getId() );
        historiqueATResponse.dateAction( h.getDateAction() );
        historiqueATResponse.action( h.getAction() );
        historiqueATResponse.ancienStatut( h.getAncienStatut() );
        historiqueATResponse.nouveauStatut( h.getNouveauStatut() );
        historiqueATResponse.commentaire( h.getCommentaire() );

        historiqueATResponse.utilisateurNomComplet( h.getUtilisateur() != null ? h.getUtilisateur().getPrenom() + " " + h.getUtilisateur().getNom() : null );

        return historiqueATResponse.build();
    }

    private String hUtilisateurId(HistoriqueAT historiqueAT) {
        if ( historiqueAT == null ) {
            return null;
        }
        Utilisateur utilisateur = historiqueAT.getUtilisateur();
        if ( utilisateur == null ) {
            return null;
        }
        String id = utilisateur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String hAutorisationTravailId(HistoriqueAT historiqueAT) {
        if ( historiqueAT == null ) {
            return null;
        }
        AutorisationTravail autorisationTravail = historiqueAT.getAutorisationTravail();
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
