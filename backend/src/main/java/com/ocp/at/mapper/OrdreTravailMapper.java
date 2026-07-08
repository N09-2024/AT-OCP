package com.ocp.at.mapper;

import com.ocp.at.dto.request.OrdreTravailRequest;
import com.ocp.at.dto.response.OrdreTravailResponse;
import com.ocp.at.entity.OrdreTravail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrdreTravailMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "demandeur", ignore = true)
    @Mapping(target = "installation", ignore = true)
    @Mapping(target = "visitePrealable", ignore = true)
    OrdreTravail toEntity(OrdreTravailRequest request);

    @Mapping(source = "demandeur.id", target = "demandeurId")
    @Mapping(target = "demandeurNomComplet", expression = "java(entity.getDemandeur() != null ? entity.getDemandeur().getNom() + \" \" + entity.getDemandeur().getPrenom() : null)")
    @Mapping(source = "installation.id", target = "installationId")
    @Mapping(source = "installation.nomInstallation", target = "installationNom")
    @Mapping(source = "visitePrealable.id", target = "visitePrealableId")
    @Mapping(source = "visitePrealable.effectuee", target = "visiteEffectuee", defaultValue = "false")
    @Mapping(target = "atCreable", ignore = true) // Calculé dynamiquement dans le service
    OrdreTravailResponse toResponse(OrdreTravail entity);
}
