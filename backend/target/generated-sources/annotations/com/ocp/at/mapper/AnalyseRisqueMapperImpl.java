package com.ocp.at.mapper;

import com.ocp.at.dto.response.AnalyseRisqueResponse;
import com.ocp.at.entity.AnalyseRisque;
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
public class AnalyseRisqueMapperImpl implements AnalyseRisqueMapper {

    @Override
    public AnalyseRisqueResponse toResponse(AnalyseRisque entity) {
        if ( entity == null ) {
            return null;
        }

        AnalyseRisqueResponse.AnalyseRisqueResponseBuilder analyseRisqueResponse = AnalyseRisqueResponse.builder();

        analyseRisqueResponse.visitePrealableId( entityVisitePrealableId( entity ) );
        analyseRisqueResponse.analyseurId( entityAnalyseurId( entity ) );
        analyseRisqueResponse.id( entity.getId() );
        analyseRisqueResponse.dateAnalyse( entity.getDateAnalyse() );
        analyseRisqueResponse.commentaire( entity.getCommentaire() );

        analyseRisqueResponse.analyseurNomComplet( entity.getAnalyseur() != null ? entity.getAnalyseur().getNom() + " " + entity.getAnalyseur().getPrenom() : null );
        analyseRisqueResponse.risquesIds( mapRisquesIds(entity.getRisques()) );
        analyseRisqueResponse.risquesNoms( mapRisquesNoms(entity.getRisques()) );
        analyseRisqueResponse.mesuresIds( mapMesuresIds(entity.getMesures()) );
        analyseRisqueResponse.mesuresNoms( mapMesuresNoms(entity.getMesures()) );
        analyseRisqueResponse.episIds( mapEpisIds(entity.getEpis()) );
        analyseRisqueResponse.episNoms( mapEpisNoms(entity.getEpis()) );
        analyseRisqueResponse.moyensAccesIds( mapMoyensIds(entity.getMoyensAcces()) );
        analyseRisqueResponse.moyensAccesNoms( mapMoyensNoms(entity.getMoyensAcces()) );

        return analyseRisqueResponse.build();
    }

    private String entityVisitePrealableId(AnalyseRisque analyseRisque) {
        if ( analyseRisque == null ) {
            return null;
        }
        VisitePrealable visitePrealable = analyseRisque.getVisitePrealable();
        if ( visitePrealable == null ) {
            return null;
        }
        String id = visitePrealable.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityAnalyseurId(AnalyseRisque analyseRisque) {
        if ( analyseRisque == null ) {
            return null;
        }
        Utilisateur analyseur = analyseRisque.getAnalyseur();
        if ( analyseur == null ) {
            return null;
        }
        String id = analyseur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
