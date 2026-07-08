package com.ocp.at.mapper;

import com.ocp.at.dto.request.MesurePreparationRequest;
import com.ocp.at.dto.response.MesurePreparationResponse;
import com.ocp.at.entity.MesurePreparation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class MesurePreparationMapperImpl implements MesurePreparationMapper {

    @Override
    public MesurePreparation toEntity(MesurePreparationRequest request) {
        if ( request == null ) {
            return null;
        }

        MesurePreparation.MesurePreparationBuilder mesurePreparation = MesurePreparation.builder();

        mesurePreparation.nomMesure( request.getNomMesure() );
        mesurePreparation.descriptionMesure( request.getDescriptionMesure() );

        return mesurePreparation.build();
    }

    @Override
    public MesurePreparationResponse toResponse(MesurePreparation entity) {
        if ( entity == null ) {
            return null;
        }

        MesurePreparationResponse.MesurePreparationResponseBuilder mesurePreparationResponse = MesurePreparationResponse.builder();

        mesurePreparationResponse.id( entity.getId() );
        mesurePreparationResponse.nomMesure( entity.getNomMesure() );
        mesurePreparationResponse.descriptionMesure( entity.getDescriptionMesure() );

        return mesurePreparationResponse.build();
    }

    @Override
    public void updateEntityFromRequest(MesurePreparationRequest request, MesurePreparation entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomMesure( request.getNomMesure() );
        entity.setDescriptionMesure( request.getDescriptionMesure() );
    }
}
