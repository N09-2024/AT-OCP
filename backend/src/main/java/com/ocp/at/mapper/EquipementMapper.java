package com.ocp.at.mapper;

import com.ocp.at.dto.request.EquipementRequest;
import com.ocp.at.dto.response.EquipementResponse;
import com.ocp.at.entity.Equipement;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EquipementMapper {
    Equipement toEntity(EquipementRequest request);

    EquipementResponse toResponse(Equipement entity);

    void updateEntityFromRequest(EquipementRequest request, @MappingTarget Equipement entity);
}

