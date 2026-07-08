package com.ocp.at.mapper;

import com.ocp.at.dto.response.PhotoResponse;
import com.ocp.at.dto.response.VisitePrealableResponse;
import com.ocp.at.entity.AnalyseRisque;
import com.ocp.at.entity.Photo;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.VisitePrealable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class VisitePrealableMapperImpl implements VisitePrealableMapper {

    @Autowired
    private PhotoMapper photoMapper;

    @Override
    public VisitePrealableResponse toResponse(VisitePrealable entity) {
        if ( entity == null ) {
            return null;
        }

        VisitePrealableResponse.VisitePrealableResponseBuilder visitePrealableResponse = VisitePrealableResponse.builder();

        visitePrealableResponse.visiteurId( entityVisiteurId( entity ) );
        visitePrealableResponse.analyseRisqueId( entityAnalyseRisqueId( entity ) );
        visitePrealableResponse.photos( photoListToPhotoResponseList( entity.getPhotos() ) );
        visitePrealableResponse.id( entity.getId() );
        visitePrealableResponse.dateHeureDebut( entity.getDateHeureDebut() );
        visitePrealableResponse.dateHeureFin( entity.getDateHeureFin() );
        visitePrealableResponse.latitude( entity.getLatitude() );
        visitePrealableResponse.longitude( entity.getLongitude() );
        visitePrealableResponse.commentaire( entity.getCommentaire() );
        visitePrealableResponse.effectuee( entity.isEffectuee() );

        visitePrealableResponse.visiteurNomComplet( entity.getVisiteur() != null ? entity.getVisiteur().getNom() + " " + entity.getVisiteur().getPrenom() : null );
        visitePrealableResponse.analyseCreee( entity.getAnalyseRisque() != null );
        visitePrealableResponse.nombrePhotos( entity.getPhotos() != null ? entity.getPhotos().size() : 0 );
        visitePrealableResponse.risquesIdentifiesIds( mapRisquesIds(entity.getRisquesIdentifies()) );

        return visitePrealableResponse.build();
    }

    private String entityVisiteurId(VisitePrealable visitePrealable) {
        if ( visitePrealable == null ) {
            return null;
        }
        Utilisateur visiteur = visitePrealable.getVisiteur();
        if ( visiteur == null ) {
            return null;
        }
        String id = visiteur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityAnalyseRisqueId(VisitePrealable visitePrealable) {
        if ( visitePrealable == null ) {
            return null;
        }
        AnalyseRisque analyseRisque = visitePrealable.getAnalyseRisque();
        if ( analyseRisque == null ) {
            return null;
        }
        String id = analyseRisque.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<PhotoResponse> photoListToPhotoResponseList(List<Photo> list) {
        if ( list == null ) {
            return null;
        }

        List<PhotoResponse> list1 = new ArrayList<PhotoResponse>( list.size() );
        for ( Photo photo : list ) {
            list1.add( photoMapper.toResponse( photo ) );
        }

        return list1;
    }
}
