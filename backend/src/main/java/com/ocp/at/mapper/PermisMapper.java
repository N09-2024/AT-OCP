package com.ocp.at.mapper;

import com.ocp.at.dto.request.PermisRequest;
import com.ocp.at.dto.response.PermisResponse;
import com.ocp.at.entity.Permis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermisMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "dateEmission", ignore = true)
    @Mapping(target = "dateExpiration", ignore = true)
    @Mapping(target = "statutVerification", ignore = true)
    @Mapping(target = "analyseIA", ignore = true)
    @Mapping(target = "fichierJoint", ignore = true)
    @Mapping(target = "autorisationTravail", ignore = true)
    Permis toEntity(PermisRequest request);

    @Mapping(target = "fichierJointId", source = "fichierJoint.id")
    @Mapping(target = "fichierJointNom", source = "fichierJoint.nom")
    @Mapping(target = "analyseIAId", source = "analyseIA.id")
    PermisResponse toResponse(Permis permis);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "dateEmission", ignore = true)
    @Mapping(target = "dateExpiration", ignore = true)
    @Mapping(target = "statutVerification", ignore = true)
    @Mapping(target = "analyseIA", ignore = true)
    @Mapping(target = "fichierJoint", ignore = true)
    @Mapping(target = "autorisationTravail", ignore = true)
    void updateEntityFromRequest(PermisRequest request, @MappingTarget Permis permis);
}
