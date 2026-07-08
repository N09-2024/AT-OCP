package com.ocp.at.mapper;

import com.ocp.at.dto.request.ServiceRequest;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.entity.Service;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
    @Mapping(target = "zone.id", source = "zoneId")
    Service toEntity(ServiceRequest request);
    ServiceResponse toResponse(Service entity);
    void updateEntityFromRequest(ServiceRequest request, @MappingTarget Service entity);
}

