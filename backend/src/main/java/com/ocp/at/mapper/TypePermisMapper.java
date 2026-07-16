package com.ocp.at.mapper;

import com.ocp.at.dto.request.TypePermisRequest;
import com.ocp.at.dto.response.TypePermisResponse;
import com.ocp.at.entity.TypePermis;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TypePermisMapper {
    TypePermis toEntity(TypePermisRequest request);
    TypePermisResponse toResponse(TypePermis entity);
    void updateEntityFromRequest(TypePermisRequest request, @MappingTarget TypePermis entity);
}
