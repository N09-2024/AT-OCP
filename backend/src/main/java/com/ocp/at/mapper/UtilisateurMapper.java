package com.ocp.at.mapper;

import com.ocp.at.dto.request.UtilisateurRequest;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.entity.Utilisateur;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, ServiceMapper.class})
public interface UtilisateurMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "derniereConnexion", ignore = true)
    @Mapping(target = "compteurEchecsConnexion", ignore = true)
    @Mapping(target = "compteVerrouille", ignore = true)
    @Mapping(target = "motDePasseExpire", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "service", ignore = true)
    Utilisateur toEntity(UtilisateurRequest request);

    UtilisateurResponse toResponse(Utilisateur utilisateur);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "motDePasse", ignore = true)
    @Mapping(target = "matricule", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "derniereConnexion", ignore = true)
    @Mapping(target = "compteurEchecsConnexion", ignore = true)
    @Mapping(target = "compteVerrouille", ignore = true)
    @Mapping(target = "motDePasseExpire", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "service", ignore = true)
    void updateEntityFromRequest(com.ocp.at.dto.request.UtilisateurUpdateRequest request, @MappingTarget Utilisateur utilisateur);
}
