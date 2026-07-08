package com.ocp.at.mapper;

import com.ocp.at.dto.response.AnalyseIAResponse;
import com.ocp.at.entity.AnalyseIA;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalyseIAMapper {

    @Mapping(target = "permisId", source = "permis.id")
    AnalyseIAResponse toResponse(AnalyseIA analyseIA);
}
