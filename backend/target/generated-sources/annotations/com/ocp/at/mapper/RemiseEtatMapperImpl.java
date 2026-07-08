package com.ocp.at.mapper;

import com.ocp.at.dto.request.RemiseEtatRequest;
import com.ocp.at.dto.response.RemiseEtatResponse;
import com.ocp.at.entity.RemiseEtat;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:13:18+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class RemiseEtatMapperImpl implements RemiseEtatMapper {

    @Override
    public RemiseEtat toEntity(RemiseEtatRequest request) {
        if ( request == null ) {
            return null;
        }

        RemiseEtat.RemiseEtatBuilder remiseEtat = RemiseEtat.builder();

        remiseEtat.zoneNettoyee( request.getZoneNettoyee() );
        remiseEtat.materielRetire( request.getMaterielRetire() );
        remiseEtat.protectionsRetirees( request.getProtectionsRetirees() );
        remiseEtat.consignationRetiree( request.getConsignationRetiree() );
        remiseEtat.commentaire( request.getCommentaire() );

        return remiseEtat.build();
    }

    @Override
    public RemiseEtatResponse toResponse(RemiseEtat remiseEtat) {
        if ( remiseEtat == null ) {
            return null;
        }

        RemiseEtatResponse remiseEtatResponse = new RemiseEtatResponse();

        remiseEtatResponse.setId( remiseEtat.getId() );
        remiseEtatResponse.setZoneNettoyee( remiseEtat.getZoneNettoyee() );
        remiseEtatResponse.setMaterielRetire( remiseEtat.getMaterielRetire() );
        remiseEtatResponse.setProtectionsRetirees( remiseEtat.getProtectionsRetirees() );
        remiseEtatResponse.setConsignationRetiree( remiseEtat.getConsignationRetiree() );
        remiseEtatResponse.setCommentaire( remiseEtat.getCommentaire() );

        return remiseEtatResponse;
    }

    @Override
    public void updateFromRequest(RemiseEtatRequest request, RemiseEtat remiseEtat) {
        if ( request == null ) {
            return;
        }

        if ( request.getZoneNettoyee() != null ) {
            remiseEtat.setZoneNettoyee( request.getZoneNettoyee() );
        }
        if ( request.getMaterielRetire() != null ) {
            remiseEtat.setMaterielRetire( request.getMaterielRetire() );
        }
        if ( request.getProtectionsRetirees() != null ) {
            remiseEtat.setProtectionsRetirees( request.getProtectionsRetirees() );
        }
        if ( request.getConsignationRetiree() != null ) {
            remiseEtat.setConsignationRetiree( request.getConsignationRetiree() );
        }
        if ( request.getCommentaire() != null ) {
            remiseEtat.setCommentaire( request.getCommentaire() );
        }
    }
}
