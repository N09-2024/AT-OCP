package com.ocp.at.mapper;

import com.ocp.at.dto.request.ZoneRequest;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.entity.Zone;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class ZoneMapperImpl implements ZoneMapper {

    @Override
    public Zone toEntity(ZoneRequest request) {
        if ( request == null ) {
            return null;
        }

        Zone.ZoneBuilder zone = Zone.builder();

        zone.nomZone( request.getNomZone() );
        zone.descriptionZone( request.getDescriptionZone() );
        zone.codeZone( request.getCodeZone() );

        return zone.build();
    }

    @Override
    public ZoneResponse toResponse(Zone entity) {
        if ( entity == null ) {
            return null;
        }

        ZoneResponse.ZoneResponseBuilder zoneResponse = ZoneResponse.builder();

        zoneResponse.id( entity.getId() );
        zoneResponse.nomZone( entity.getNomZone() );
        zoneResponse.descriptionZone( entity.getDescriptionZone() );
        zoneResponse.codeZone( entity.getCodeZone() );

        return zoneResponse.build();
    }

    @Override
    public void updateEntityFromRequest(ZoneRequest request, Zone entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomZone( request.getNomZone() );
        entity.setDescriptionZone( request.getDescriptionZone() );
        entity.setCodeZone( request.getCodeZone() );
    }
}
