package com.ocp.at.mapper;

import com.ocp.at.dto.request.RisqueRequest;
import com.ocp.at.dto.response.RisqueResponse;
import com.ocp.at.entity.Risque;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RisqueMapper {
    Risque toEntity(RisqueRequest request);
    RisqueResponse toResponse(Risque entity);
    void updateEntityFromRequest(RisqueRequest request, @MappingTarget Risque entity);
}

