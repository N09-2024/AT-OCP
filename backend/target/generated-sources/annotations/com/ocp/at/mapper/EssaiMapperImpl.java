package com.ocp.at.mapper;

import com.ocp.at.dto.request.EssaiRequest;
import com.ocp.at.dto.response.EssaiResponse;
import com.ocp.at.entity.Essai;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:13:13+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class EssaiMapperImpl implements EssaiMapper {

    @Override
    public Essai toEntity(EssaiRequest request) {
        if ( request == null ) {
            return null;
        }

        Essai.EssaiBuilder essai = Essai.builder();

        essai.nom( request.getNom() );
        essai.description( request.getDescription() );
        essai.resultat( request.getResultat() );
        essai.conforme( request.getConforme() );
        essai.commentaire( request.getCommentaire() );

        return essai.build();
    }

    @Override
    public EssaiResponse toResponse(Essai essai) {
        if ( essai == null ) {
            return null;
        }

        EssaiResponse essaiResponse = new EssaiResponse();

        essaiResponse.setId( essai.getId() );
        essaiResponse.setNom( essai.getNom() );
        essaiResponse.setDescription( essai.getDescription() );
        essaiResponse.setResultat( essai.getResultat() );
        essaiResponse.setConforme( essai.getConforme() );
        essaiResponse.setCommentaire( essai.getCommentaire() );

        return essaiResponse;
    }

    @Override
    public void updateFromRequest(EssaiRequest request, Essai essai) {
        if ( request == null ) {
            return;
        }

        if ( request.getNom() != null ) {
            essai.setNom( request.getNom() );
        }
        if ( request.getDescription() != null ) {
            essai.setDescription( request.getDescription() );
        }
        if ( request.getResultat() != null ) {
            essai.setResultat( request.getResultat() );
        }
        if ( request.getConforme() != null ) {
            essai.setConforme( request.getConforme() );
        }
        if ( request.getCommentaire() != null ) {
            essai.setCommentaire( request.getCommentaire() );
        }
    }
}
