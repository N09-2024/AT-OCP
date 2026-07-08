package com.ocp.at.mapper;

import com.ocp.at.dto.request.MesurePreparationRequest;
import com.ocp.at.dto.response.MesurePreparationResponse;
import com.ocp.at.entity.MesurePreparation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MesurePreparationMapper {
    MesurePreparation toEntity(MesurePreparationRequest request);
    MesurePreparationResponse toResponse(MesurePreparation entity);
    void updateEntityFromRequest(MesurePreparationRequest request, @MappingTarget MesurePreparation entity);
}

