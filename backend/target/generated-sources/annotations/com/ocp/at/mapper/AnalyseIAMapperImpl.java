package com.ocp.at.mapper;

import com.ocp.at.dto.response.AnalyseIAResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.Permis;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class AnalyseIAMapperImpl implements AnalyseIAMapper {

    @Override
    public AnalyseIAResponse toResponse(AnalyseIA analyseIA) {
        if ( analyseIA == null ) {
            return null;
        }

        AnalyseIAResponse analyseIAResponse = new AnalyseIAResponse();

        analyseIAResponse.setPermisId( analyseIAPermisId( analyseIA ) );
        analyseIAResponse.setId( analyseIA.getId() );
        analyseIAResponse.setDateAnalyse( analyseIA.getDateAnalyse() );
        analyseIAResponse.setOcrText( analyseIA.getOcrText() );
        analyseIAResponse.setJsonExtraction( analyseIA.getJsonExtraction() );
        analyseIAResponse.setTauxConfiance( analyseIA.getTauxConfiance() );
        analyseIAResponse.setResultat( analyseIA.getResultat() );
        analyseIAResponse.setCommentaireIA( analyseIA.getCommentaireIA() );
        analyseIAResponse.setTempsExecution( analyseIA.getTempsExecution() );
        analyseIAResponse.setModeleUtilise( analyseIA.getModeleUtilise() );
        analyseIAResponse.setVersionModele( analyseIA.getVersionModele() );

        return analyseIAResponse;
    }

    private String analyseIAPermisId(AnalyseIA analyseIA) {
        if ( analyseIA == null ) {
            return null;
        }
        Permis permis = analyseIA.getPermis();
        if ( permis == null ) {
            return null;
        }
        String id = permis.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
