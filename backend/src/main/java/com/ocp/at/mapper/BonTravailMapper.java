package com.ocp.at.mapper;

import com.ocp.at.dto.request.BonTravailRequest;
import com.ocp.at.dto.response.BonTravailResponse;
import com.ocp.at.entity.BonTravail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BonTravailMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "dateEmission", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "entrepriseExterne", ignore = true)
    @Mapping(target = "demandeur", ignore = true)
    @Mapping(target = "installation", ignore = true)
    @Mapping(target = "visitePrealable", ignore = true)
    BonTravail toEntity(BonTravailRequest request);

    @Mapping(source = "entrepriseExterne.id", target = "entrepriseExterneId")
    @Mapping(source = "entrepriseExterne.nomEntreprise", target = "entrepriseExterneNom")
    @Mapping(source = "demandeur.id", target = "demandeurId")
    @Mapping(target = "demandeurNomComplet", expression = "java(entity.getDemandeur() != null ? entity.getDemandeur().getNom() + \" \" + entity.getDemandeur().getPrenom() : null)")
    @Mapping(source = "installation.id", target = "installationId")
    @Mapping(source = "installation.nomInstallation", target = "installationNom")
    @Mapping(source = "visitePrealable.id", target = "visitePrealableId")
    @Mapping(source = "visitePrealable.effectuee", target = "visiteEffectuee", defaultValue = "false")
    @Mapping(target = "atCreable", ignore = true) // Calculé dynamiquement dans le service
    BonTravailResponse toResponse(BonTravail entity);

    // Note: BonTravail has no collection fields in the entity, mapping should work
}
