package com.ocp.at.mapper;

import com.ocp.at.dto.request.ServiceRequest;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
    @Mapping(target = "zone", expression = "java(request.getZoneId() != null && !request.getZoneId().isEmpty() ? com.ocp.at.entity.Zone.builder().id(request.getZoneId()).build() : null)")
    Service toEntity(ServiceRequest request);
    
    ServiceResponse toResponse(Service entity);
    
    @Mapping(target = "zone", expression = "java(request.getZoneId() != null && !request.getZoneId().isEmpty() ? com.ocp.at.entity.Zone.builder().id(request.getZoneId()).build() : null)")
    void updateEntityFromRequest(ServiceRequest request, @MappingTarget Service entity);
}

