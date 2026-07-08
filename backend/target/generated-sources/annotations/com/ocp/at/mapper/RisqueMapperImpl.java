package com.ocp.at.mapper;

import com.ocp.at.dto.request.RisqueRequest;
import com.ocp.at.dto.response.RisqueResponse;
import com.ocp.at.entity.Risque;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class RisqueMapperImpl implements RisqueMapper {

    @Override
    public Risque toEntity(RisqueRequest request) {
        if ( request == null ) {
            return null;
        }

        Risque.RisqueBuilder risque = Risque.builder();

        risque.nomRisque( request.getNomRisque() );
        risque.descriptionRisque( request.getDescriptionRisque() );
        risque.niveau( request.getNiveau() );

        return risque.build();
    }

    @Override
    public RisqueResponse toResponse(Risque entity) {
        if ( entity == null ) {
            return null;
        }

        RisqueResponse.RisqueResponseBuilder risqueResponse = RisqueResponse.builder();

        risqueResponse.id( entity.getId() );
        risqueResponse.nomRisque( entity.getNomRisque() );
        risqueResponse.descriptionRisque( entity.getDescriptionRisque() );
        risqueResponse.niveau( entity.getNiveau() );

        return risqueResponse.build();
    }

    @Override
    public void updateEntityFromRequest(RisqueRequest request, Risque entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomRisque( request.getNomRisque() );
        entity.setDescriptionRisque( request.getDescriptionRisque() );
        entity.setNiveau( request.getNiveau() );
    }
}
