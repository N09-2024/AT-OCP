package com.ocp.at.mapper;

import com.ocp.at.dto.request.EquipementRequest;
import com.ocp.at.dto.response.EquipementResponse;
import com.ocp.at.dto.response.InstallationResponse;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.entity.Equipement;
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
public class EquipementMapperImpl implements EquipementMapper {

    @Override
    public Equipement toEntity(EquipementRequest request) {
        if ( request == null ) {
            return null;
        }

        Equipement.EquipementBuilder equipement = Equipement.builder();

        equipement.installation( equipementRequestToInstallation( request ) );
        equipement.nomEquipement( request.getNomEquipement() );
        equipement.codeEquipement( request.getCodeEquipement() );
        equipement.descriptionEquipement( request.getDescriptionEquipement() );

        return equipement.build();
    }

    @Override
    public EquipementResponse toResponse(Equipement entity) {
        if ( entity == null ) {
            return null;
        }

        EquipementResponse.EquipementResponseBuilder equipementResponse = EquipementResponse.builder();

        equipementResponse.id( entity.getId() );
        equipementResponse.nomEquipement( entity.getNomEquipement() );
        equipementResponse.codeEquipement( entity.getCodeEquipement() );
        equipementResponse.descriptionEquipement( entity.getDescriptionEquipement() );
        equipementResponse.installation( installationToInstallationResponse( entity.getInstallation() ) );

        return equipementResponse.build();
    }

    @Override
    public void updateEntityFromRequest(EquipementRequest request, Equipement entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomEquipement( request.getNomEquipement() );
        entity.setCodeEquipement( request.getCodeEquipement() );
        entity.setDescriptionEquipement( request.getDescriptionEquipement() );
    }

    protected Installation equipementRequestToInstallation(EquipementRequest equipementRequest) {
        if ( equipementRequest == null ) {
            return null;
        }

        Installation.InstallationBuilder installation = Installation.builder();

        installation.id( equipementRequest.getInstallationId() );

        return installation.build();
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

    protected InstallationResponse installationToInstallationResponse(Installation installation) {
        if ( installation == null ) {
            return null;
        }

        InstallationResponse.InstallationResponseBuilder installationResponse = InstallationResponse.builder();

        installationResponse.id( installation.getId() );
        installationResponse.nomInstallation( installation.getNomInstallation() );
        installationResponse.atelier( installation.getAtelier() );
        installationResponse.localisation( installation.getLocalisation() );
        installationResponse.codeInstallation( installation.getCodeInstallation() );
        installationResponse.service( serviceToServiceResponse( installation.getService() ) );

        return installationResponse.build();
    }
}
