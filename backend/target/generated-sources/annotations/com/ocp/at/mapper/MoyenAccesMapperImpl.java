package com.ocp.at.mapper;

import com.ocp.at.dto.request.MoyenAccesRequest;
import com.ocp.at.dto.response.MoyenAccesResponse;
import com.ocp.at.entity.MoyenAcces;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class MoyenAccesMapperImpl implements MoyenAccesMapper {

    @Override
    public MoyenAcces toEntity(MoyenAccesRequest request) {
        if ( request == null ) {
            return null;
        }

        MoyenAcces.MoyenAccesBuilder moyenAcces = MoyenAcces.builder();

        moyenAcces.nomMoyen( request.getNomMoyen() );
        moyenAcces.descriptionMoyen( request.getDescriptionMoyen() );

        return moyenAcces.build();
    }

    @Override
    public MoyenAccesResponse toResponse(MoyenAcces entity) {
        if ( entity == null ) {
            return null;
        }

        MoyenAccesResponse.MoyenAccesResponseBuilder moyenAccesResponse = MoyenAccesResponse.builder();

        moyenAccesResponse.id( entity.getId() );
        moyenAccesResponse.nomMoyen( entity.getNomMoyen() );
        moyenAccesResponse.descriptionMoyen( entity.getDescriptionMoyen() );

        return moyenAccesResponse.build();
    }

    @Override
    public void updateEntityFromRequest(MoyenAccesRequest request, MoyenAcces entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomMoyen( request.getNomMoyen() );
        entity.setDescriptionMoyen( request.getDescriptionMoyen() );
    }
}
