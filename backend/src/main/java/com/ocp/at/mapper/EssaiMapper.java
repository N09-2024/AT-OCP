package com.ocp.at.mapper;

import com.ocp.at.dto.request.EssaiRequest;
import com.ocp.at.dto.response.EssaiResponse;
import com.ocp.at.entity.Essai;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EssaiMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receptionTravaux", ignore = true)
    Essai toEntity(EssaiRequest request);

    EssaiResponse toResponse(Essai essai);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receptionTravaux", ignore = true)
    void updateFromRequest(EssaiRequest request, @MappingTarget Essai essai);
}
