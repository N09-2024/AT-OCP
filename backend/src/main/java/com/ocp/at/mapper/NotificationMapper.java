package com.ocp.at.mapper;

import com.ocp.at.dto.response.NotificationResponse;
import com.ocp.at.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "utilisateur.id", target = "utilisateurId")
    NotificationResponse toResponse(Notification entity);
}
