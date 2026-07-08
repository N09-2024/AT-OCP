package com.ocp.at.mapper;

import com.ocp.at.dto.response.PhotoResponse;
import com.ocp.at.entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PhotoMapper {

    @Mapping(source = "visitePrealable.id", target = "visitePrealableId")
    PhotoResponse toResponse(Photo photo);
}
