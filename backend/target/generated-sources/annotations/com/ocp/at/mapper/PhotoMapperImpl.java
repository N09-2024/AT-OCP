package com.ocp.at.mapper;

import com.ocp.at.dto.response.PhotoResponse;
import com.ocp.at.entity.Photo;
import com.ocp.at.entity.VisitePrealable;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:32+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class PhotoMapperImpl implements PhotoMapper {

    @Override
    public PhotoResponse toResponse(Photo photo) {
        if ( photo == null ) {
            return null;
        }

        PhotoResponse.PhotoResponseBuilder photoResponse = PhotoResponse.builder();

        photoResponse.visitePrealableId( photoVisitePrealableId( photo ) );
        photoResponse.id( photo.getId() );
        photoResponse.nom( photo.getNom() );
        photoResponse.path( photo.getPath() );
        photoResponse.taille( photo.getTaille() );
        photoResponse.typeMime( photo.getTypeMime() );
        photoResponse.ordre( photo.getOrdre() );
        photoResponse.legende( photo.getLegende() );
        photoResponse.dateCreation( photo.getDateCreation() );

        return photoResponse.build();
    }

    private String photoVisitePrealableId(Photo photo) {
        if ( photo == null ) {
            return null;
        }
        VisitePrealable visitePrealable = photo.getVisitePrealable();
        if ( visitePrealable == null ) {
            return null;
        }
        String id = visitePrealable.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
