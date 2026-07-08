package com.ocp.at.mapper;

import com.ocp.at.dto.request.UtilisateurRequest;
import com.ocp.at.dto.request.UtilisateurUpdateRequest;
import com.ocp.at.dto.response.RoleResponse;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-08T11:06:33+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class UtilisateurMapperImpl implements UtilisateurMapper {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Utilisateur toEntity(UtilisateurRequest request) {
        if ( request == null ) {
            return null;
        }

        Utilisateur.UtilisateurBuilder utilisateur = Utilisateur.builder();

        utilisateur.matricule( request.getMatricule() );
        utilisateur.nom( request.getNom() );
        utilisateur.prenom( request.getPrenom() );
        utilisateur.email( request.getEmail() );
        utilisateur.telephone( request.getTelephone() );
        utilisateur.motDePasse( request.getMotDePasse() );
        utilisateur.photo( request.getPhoto() );

        return utilisateur.build();
    }

    @Override
    public UtilisateurResponse toResponse(Utilisateur utilisateur) {
        if ( utilisateur == null ) {
            return null;
        }

        UtilisateurResponse.UtilisateurResponseBuilder utilisateurResponse = UtilisateurResponse.builder();

        utilisateurResponse.id( utilisateur.getId() );
        utilisateurResponse.matricule( utilisateur.getMatricule() );
        utilisateurResponse.nom( utilisateur.getNom() );
        utilisateurResponse.prenom( utilisateur.getPrenom() );
        utilisateurResponse.email( utilisateur.getEmail() );
        utilisateurResponse.telephone( utilisateur.getTelephone() );
        utilisateurResponse.photo( utilisateur.getPhoto() );
        utilisateurResponse.actif( utilisateur.isActif() );
        utilisateurResponse.compteVerrouille( utilisateur.isCompteVerrouille() );
        utilisateurResponse.motDePasseExpire( utilisateur.isMotDePasseExpire() );
        utilisateurResponse.dateCreation( utilisateur.getDateCreation() );
        utilisateurResponse.dateModification( utilisateur.getDateModification() );
        utilisateurResponse.derniereConnexion( utilisateur.getDerniereConnexion() );
        utilisateurResponse.roles( roleSetToRoleResponseSet( utilisateur.getRoles() ) );

        return utilisateurResponse.build();
    }

    @Override
    public void updateEntityFromRequest(UtilisateurUpdateRequest request, Utilisateur utilisateur) {
        if ( request == null ) {
            return;
        }

        if ( request.getNom() != null ) {
            utilisateur.setNom( request.getNom() );
        }
        if ( request.getPrenom() != null ) {
            utilisateur.setPrenom( request.getPrenom() );
        }
        if ( request.getEmail() != null ) {
            utilisateur.setEmail( request.getEmail() );
        }
        if ( request.getTelephone() != null ) {
            utilisateur.setTelephone( request.getTelephone() );
        }
        if ( request.getPhoto() != null ) {
            utilisateur.setPhoto( request.getPhoto() );
        }
    }

    protected Set<RoleResponse> roleSetToRoleResponseSet(Set<Role> set) {
        if ( set == null ) {
            return null;
        }

        Set<RoleResponse> set1 = new LinkedHashSet<RoleResponse>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Role role : set ) {
            set1.add( roleMapper.toResponse( role ) );
        }

        return set1;
    }
}
