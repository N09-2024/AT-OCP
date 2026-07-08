package com.ocp.at.mapper;

import com.ocp.at.dto.request.PhotoReceptionRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.entity.PhotoReception;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PhotoReceptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receptionTravaux", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PhotoReception toEntity(PhotoReceptionRequest request);

    @Mapping(target = "downloadUrl", expression = "java(\"/api/receptions/\" + photo.getReceptionTravaux().getId() + \"/photos/\" + photo.getId() + \"/download\")")
    PhotoReceptionResponse toResponse(PhotoReception photo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "receptionTravaux", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFromRequest(PhotoReceptionRequest request, @MappingTarget PhotoReception photo);
}
