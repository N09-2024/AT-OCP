package com.ocp.at.mapper;

import com.ocp.at.dto.request.InstallationRequest;
import com.ocp.at.dto.response.InstallationResponse;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.entity.Installation;
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
public class InstallationMapperImpl implements InstallationMapper {

    @Override
    public Installation toEntity(InstallationRequest request) {
        if ( request == null ) {
            return null;
        }

        Installation.InstallationBuilder installation = Installation.builder();

        installation.service( installationRequestToService( request ) );
        installation.nomInstallation( request.getNomInstallation() );
        installation.atelier( request.getAtelier() );
        installation.localisation( request.getLocalisation() );
        installation.codeInstallation( request.getCodeInstallation() );

        return installation.build();
    }

    @Override
    public InstallationResponse toResponse(Installation entity) {
        if ( entity == null ) {
            return null;
        }

        InstallationResponse.InstallationResponseBuilder installationResponse = InstallationResponse.builder();

        installationResponse.id( entity.getId() );
        installationResponse.nomInstallation( entity.getNomInstallation() );
        installationResponse.atelier( entity.getAtelier() );
        installationResponse.localisation( entity.getLocalisation() );
        installationResponse.codeInstallation( entity.getCodeInstallation() );
        installationResponse.service( serviceToServiceResponse( entity.getService() ) );

        return installationResponse.build();
    }

    @Override
    public void updateEntityFromRequest(InstallationRequest request, Installation entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomInstallation( request.getNomInstallation() );
        entity.setAtelier( request.getAtelier() );
        entity.setLocalisation( request.getLocalisation() );
        entity.setCodeInstallation( request.getCodeInstallation() );
    }

    protected Service installationRequestToService(InstallationRequest installationRequest) {
        if ( installationRequest == null ) {
            return null;
        }

        Service.ServiceBuilder service = Service.builder();

        service.id( installationRequest.getServiceId() );

        return service.build();
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

    protected ServiceResponse serviceToServiceResponse(Service service) {
        if ( service == null ) {
            return null;
        }

        ServiceResponse.ServiceResponseBuilder serviceResponse = ServiceResponse.builder();

        serviceResponse.id( service.getId() );
        serviceResponse.nomService( service.getNomService() );
        serviceResponse.descriptionService( service.getDescriptionService() );
        serviceResponse.codeService( service.getCodeService() );
        serviceResponse.zone( zoneToZoneResponse( service.getZone() ) );

        return serviceResponse.build();
    }
}
