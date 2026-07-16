package com.ocp.at.mapper;

import com.ocp.at.dto.request.InstallationRequest;
import com.ocp.at.dto.response.InstallationResponse;
import com.ocp.at.entity.Installation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InstallationMapper {
    @Mapping(target = "service", expression = "java(request.getServiceId() != null && !request.getServiceId().isEmpty() ? com.ocp.at.entity.Service.builder().id(request.getServiceId()).build() : null)")
    Installation toEntity(InstallationRequest request);
    
    InstallationResponse toResponse(Installation entity);
    
    @Mapping(target = "service", expression = "java(request.getServiceId() != null && !request.getServiceId().isEmpty() ? com.ocp.at.entity.Service.builder().id(request.getServiceId()).build() : null)")
    void updateEntityFromRequest(InstallationRequest request, @MappingTarget Installation entity);
}

