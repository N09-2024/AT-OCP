package com.ocp.at.mapper;

import com.ocp.at.dto.response.NotificationResponse;
import com.ocp.at.entity.Notification;
import com.ocp.at.entity.Utilisateur;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponse toResponse(Notification entity) {
        if ( entity == null ) {
            return null;
        }

        NotificationResponse.NotificationResponseBuilder notificationResponse = NotificationResponse.builder();

        notificationResponse.utilisateurId( entityUtilisateurId( entity ) );
        notificationResponse.id( entity.getId() );
        notificationResponse.titre( entity.getTitre() );
        notificationResponse.message( entity.getMessage() );
        notificationResponse.dateCreation( entity.getDateCreation() );
        notificationResponse.dateLecture( entity.getDateLecture() );
        notificationResponse.lu( entity.isLu() );

        return notificationResponse.build();
    }

    private String entityUtilisateurId(Notification notification) {
        if ( notification == null ) {
            return null;
        }
        Utilisateur utilisateur = notification.getUtilisateur();
        if ( utilisateur == null ) {
            return null;
        }
        String id = utilisateur.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
