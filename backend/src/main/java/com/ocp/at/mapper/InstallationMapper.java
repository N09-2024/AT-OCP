package com.ocp.at.mapper;

import com.ocp.at.dto.request.InstallationRequest;
import com.ocp.at.dto.response.InstallationResponse;
import com.ocp.at.entity.Installation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InstallationMapper {
    @Mapping(target = "service.id", source = "serviceId")
    Installation toEntity(InstallationRequest request);
    InstallationResponse toResponse(Installation entity);
    void updateEntityFromRequest(InstallationRequest request, @MappingTarget Installation entity);
}

