package com.ocp.at.mapper;

import com.ocp.at.dto.request.PermissionRequest;
import com.ocp.at.dto.response.PermissionResponse;
import com.ocp.at.entity.Permission;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public Permission toEntity(PermissionRequest request) {
        if ( request == null ) {
            return null;
        }

        Permission.PermissionBuilder permission = Permission.builder();

        permission.nom( request.getNom() );
        permission.description( request.getDescription() );

        return permission.build();
    }

    @Override
    public PermissionResponse toResponse(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionResponse.PermissionResponseBuilder permissionResponse = PermissionResponse.builder();

        permissionResponse.id( permission.getId() );
        permissionResponse.nom( permission.getNom() );
        permissionResponse.description( permission.getDescription() );

        return permissionResponse.build();
    }
}
