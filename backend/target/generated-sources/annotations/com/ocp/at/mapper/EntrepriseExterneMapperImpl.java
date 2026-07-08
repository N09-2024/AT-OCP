package com.ocp.at.mapper;

import com.ocp.at.dto.request.EntrepriseExterneRequest;
import com.ocp.at.dto.response.EntrepriseExterneResponse;
import com.ocp.at.entity.EntrepriseExterne;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class EntrepriseExterneMapperImpl implements EntrepriseExterneMapper {

    @Override
    public EntrepriseExterne toEntity(EntrepriseExterneRequest request) {
        if ( request == null ) {
            return null;
        }

        EntrepriseExterne.EntrepriseExterneBuilder entrepriseExterne = EntrepriseExterne.builder();

        entrepriseExterne.nomEntreprise( request.getNomEntreprise() );
        entrepriseExterne.adresse( request.getAdresse() );
        entrepriseExterne.telephone( request.getTelephone() );
        entrepriseExterne.responsable( request.getResponsable() );

        return entrepriseExterne.build();
    }

    @Override
    public EntrepriseExterneResponse toResponse(EntrepriseExterne entity) {
        if ( entity == null ) {
            return null;
        }

        EntrepriseExterneResponse.EntrepriseExterneResponseBuilder entrepriseExterneResponse = EntrepriseExterneResponse.builder();

        entrepriseExterneResponse.id( entity.getId() );
        entrepriseExterneResponse.nomEntreprise( entity.getNomEntreprise() );
        entrepriseExterneResponse.adresse( entity.getAdresse() );
        entrepriseExterneResponse.telephone( entity.getTelephone() );
        entrepriseExterneResponse.responsable( entity.getResponsable() );

        return entrepriseExterneResponse.build();
    }

    @Override
    public void updateEntityFromRequest(EntrepriseExterneRequest request, EntrepriseExterne entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomEntreprise( request.getNomEntreprise() );
        entity.setAdresse( request.getAdresse() );
        entity.setTelephone( request.getTelephone() );
        entity.setResponsable( request.getResponsable() );
    }
}
