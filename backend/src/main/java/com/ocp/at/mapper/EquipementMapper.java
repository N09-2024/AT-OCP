package com.ocp.at.mapper;

import com.ocp.at.dto.request.EquipementRequest;
import com.ocp.at.dto.response.EquipementResponse;
import com.ocp.at.entity.Equipement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EquipementMapper {
    @Mapping(target = "installation", expression = "java(request.getInstallationId() != null && !request.getInstallationId().isEmpty() ? com.ocp.at.entity.Installation.builder().id(request.getInstallationId()).build() : null)")
    Equipement toEntity(EquipementRequest request);
    
    EquipementResponse toResponse(Equipement entity);
    
    @Mapping(target = "installation", expression = "java(request.getInstallationId() != null && !request.getInstallationId().isEmpty() ? com.ocp.at.entity.Installation.builder().id(request.getInstallationId()).build() : null)")
    void updateEntityFromRequest(EquipementRequest request, @MappingTarget Equipement entity);
}

