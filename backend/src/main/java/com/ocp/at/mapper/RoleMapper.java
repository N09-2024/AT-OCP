package com.ocp.at.mapper;

import com.ocp.at.dto.request.RoleRequest;
import com.ocp.at.dto.response.RoleResponse;
import com.ocp.at.entity.Role;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role role);
}
