package com.ocp.at.mapper;

import com.ocp.at.dto.request.ServiceRequest;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.entity.Service;
import com.ocp.at.entity.Zone;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class ServiceMapperImpl implements ServiceMapper {

    @Override
    public Service toEntity(ServiceRequest request) {
        if ( request == null ) {
            return null;
        }

        Service.ServiceBuilder service = Service.builder();

        service.zone( serviceRequestToZone( request ) );
        service.nomService( request.getNomService() );
        service.descriptionService( request.getDescriptionService() );
        service.codeService( request.getCodeService() );

        return service.build();
    }

    @Override
    public ServiceResponse toResponse(Service entity) {
        if ( entity == null ) {
            return null;
        }

        ServiceResponse.ServiceResponseBuilder serviceResponse = ServiceResponse.builder();

        serviceResponse.id( entity.getId() );
        serviceResponse.nomService( entity.getNomService() );
        serviceResponse.descriptionService( entity.getDescriptionService() );
        serviceResponse.codeService( entity.getCodeService() );
        serviceResponse.zone( zoneToZoneResponse( entity.getZone() ) );

        return serviceResponse.build();
    }

    @Override
    public void updateEntityFromRequest(ServiceRequest request, Service entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomService( request.getNomService() );
        entity.setDescriptionService( request.getDescriptionService() );
        entity.setCodeService( request.getCodeService() );
    }

    protected Zone serviceRequestToZone(ServiceRequest serviceRequest) {
        if ( serviceRequest == null ) {
            return null;
        }

        Zone.ZoneBuilder zone = Zone.builder();

        zone.id( serviceRequest.getZoneId() );

        return zone.build();
    }

    protected ZoneResponse zoneToZoneResponse(Zone zone) {
        if ( zone == null ) {
            return null;
        }

        ZoneResponse.ZoneResponseBuilder zoneResponse = ZoneResponse.builder();

        zoneResponse.id( zone.getId() );
        zoneResponse.nomZone( zone.getNomZone() );
        zoneResponse.descriptionZone( zone.getDescriptionZone() );
        zoneResponse.codeZone( zone.getCodeZone() );

        return zoneResponse.build();
    }
}
