package com.ocp.at.mapper;

import com.ocp.at.dto.request.PermissionRequest;
import com.ocp.at.dto.response.PermissionResponse;
import com.ocp.at.entity.Permission;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    @Mapping(target = "id", ignore = true)
    Permission toEntity(PermissionRequest request);

    PermissionResponse toResponse(Permission permission);
}
