package com.ocp.at.mapper;

import com.ocp.at.dto.request.EPIRequest;
import com.ocp.at.dto.response.EPIResponse;
import com.ocp.at.entity.EPI;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EPIMapper {
    EPI toEntity(EPIRequest request);
    EPIResponse toResponse(EPI entity);
    void updateEntityFromRequest(EPIRequest request, @MappingTarget EPI entity);
}

