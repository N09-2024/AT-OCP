package com.ocp.at.mapper;

import com.ocp.at.dto.request.ZoneRequest;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.entity.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ZoneMapper {
    Zone toEntity(ZoneRequest request);
    ZoneResponse toResponse(Zone entity);
    void updateEntityFromRequest(ZoneRequest request, @MappingTarget Zone entity);
}

