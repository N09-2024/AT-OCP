package com.ocp.at.mapper;

import com.ocp.at.dto.request.EntrepriseExterneRequest;
import com.ocp.at.dto.response.EntrepriseExterneResponse;
import com.ocp.at.entity.EntrepriseExterne;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EntrepriseExterneMapper {
    EntrepriseExterne toEntity(EntrepriseExterneRequest request);
    EntrepriseExterneResponse toResponse(EntrepriseExterne entity);
    void updateEntityFromRequest(EntrepriseExterneRequest request, @MappingTarget EntrepriseExterne entity);
}

