package com.ocp.at.mapper;

import com.ocp.at.dto.request.PermisRequest;
import com.ocp.at.dto.response.PermisResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class PermisMapperImpl implements PermisMapper {

    @Override
    public Permis toEntity(PermisRequest request) {
        if ( request == null ) {
            return null;
        }

        Permis.PermisBuilder permis = Permis.builder();

        permis.type( request.getType() );
        permis.estObligatoire( request.getEstObligatoire() );
        permis.commentaire( request.getCommentaire() );

        return permis.build();
    }

    @Override
    public PermisResponse toResponse(Permis permis) {
        if ( permis == null ) {
            return null;
        }

        PermisResponse permisResponse = new PermisResponse();

        permisResponse.setFichierJointId( permisFichierJointId( permis ) );
        permisResponse.setFichierJointNom( permisFichierJointNom( permis ) );
        permisResponse.setAnalyseIAId( permisAnalyseIAId( permis ) );
        permisResponse.setId( permis.getId() );
        permisResponse.setNumero( permis.getNumero() );
        permisResponse.setType( permis.getType() );
        permisResponse.setDateEmission( permis.getDateEmission() );
        permisResponse.setDateExpiration( permis.getDateExpiration() );
        permisResponse.setStatutVerification( permis.getStatutVerification() );
        permisResponse.setEstObligatoire( permis.getEstObligatoire() );
        permisResponse.setCommentaire( permis.getCommentaire() );

        return permisResponse;
    }

    @Override
    public void updateEntityFromRequest(PermisRequest request, Permis permis) {
        if ( request == null ) {
            return;
        }

        permis.setType( request.getType() );
        permis.setEstObligatoire( request.getEstObligatoire() );
        permis.setCommentaire( request.getCommentaire() );
    }

    private String permisFichierJointId(Permis permis) {
        if ( permis == null ) {
            return null;
        }
        FichierJoint fichierJoint = permis.getFichierJoint();
        if ( fichierJoint == null ) {
            return null;
        }
        String id = fichierJoint.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String permisFichierJointNom(Permis permis) {
        if ( permis == null ) {
            return null;
        }
        FichierJoint fichierJoint = permis.getFichierJoint();
        if ( fichierJoint == null ) {
            return null;
        }
        String nom = fichierJoint.getNom();
        if ( nom == null ) {
            return null;
        }
        return nom;
    }

    private String permisAnalyseIAId(Permis permis) {
        if ( permis == null ) {
            return null;
        }
        AnalyseIA analyseIA = permis.getAnalyseIA();
        if ( analyseIA == null ) {
            return null;
        }
        String id = analyseIA.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
