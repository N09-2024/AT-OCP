package com.ocp.at.mapper;

import com.ocp.at.dto.request.RemiseEtatRequest;
import com.ocp.at.dto.response.RemiseEtatResponse;
import com.ocp.at.entity.RemiseEtat;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RemiseEtatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receptionTravaux", ignore = true)
    RemiseEtat toEntity(RemiseEtatRequest request);

    RemiseEtatResponse toResponse(RemiseEtat remiseEtat);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receptionTravaux", ignore = true)
    void updateFromRequest(RemiseEtatRequest request, @MappingTarget RemiseEtat remiseEtat);
}
