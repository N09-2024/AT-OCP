package com.ocp.at.mapper;

import com.ocp.at.dto.request.MoyenAccesRequest;
import com.ocp.at.dto.response.MoyenAccesResponse;
import com.ocp.at.entity.MoyenAcces;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MoyenAccesMapper {
    MoyenAcces toEntity(MoyenAccesRequest request);
    MoyenAccesResponse toResponse(MoyenAcces entity);
    void updateEntityFromRequest(MoyenAccesRequest request, @MappingTarget MoyenAcces entity);
}

