package com.ocp.at.mapper;

import com.ocp.at.dto.request.EPIRequest;
import com.ocp.at.dto.response.EPIResponse;
import com.ocp.at.entity.EPI;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class EPIMapperImpl implements EPIMapper {

    @Override
    public EPI toEntity(EPIRequest request) {
        if ( request == null ) {
            return null;
        }

        EPI.EPIBuilder ePI = EPI.builder();

        ePI.nomEPI( request.getNomEPI() );
        ePI.descriptionEPI( request.getDescriptionEPI() );

        return ePI.build();
    }

    @Override
    public EPIResponse toResponse(EPI entity) {
        if ( entity == null ) {
            return null;
        }

        EPIResponse.EPIResponseBuilder ePIResponse = EPIResponse.builder();

        ePIResponse.id( entity.getId() );
        ePIResponse.nomEPI( entity.getNomEPI() );
        ePIResponse.descriptionEPI( entity.getDescriptionEPI() );

        return ePIResponse.build();
    }

    @Override
    public void updateEntityFromRequest(EPIRequest request, EPI entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomEPI( request.getNomEPI() );
        entity.setDescriptionEPI( request.getDescriptionEPI() );
    }
}
