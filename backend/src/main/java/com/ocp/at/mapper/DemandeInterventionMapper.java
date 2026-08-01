package com.ocp.at.mapper;

import com.ocp.at.dto.request.DemandeInterventionRequest;
import com.ocp.at.dto.response.DemandeInterventionResponse;
import com.ocp.at.entity.DemandeIntervention;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DemandeInterventionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "dateDemande", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "demandeur", ignore = true)
    @Mapping(target = "installation", ignore = true)
    @Mapping(target = "equipement", ignore = true)
    @Mapping(target = "visitePrealable", ignore = true)
    DemandeIntervention toEntity(DemandeInterventionRequest request);

    @Mapping(source = "demandeur.id", target = "demandeurId")
    @Mapping(target = "demandeurNomComplet", expression = "java(entity.getDemandeur() != null ? entity.getDemandeur().getNom() + \" \" + entity.getDemandeur().getPrenom() : null)")
    @Mapping(source = "installation.id", target = "installationId")
    @Mapping(source = "installation.nomInstallation", target = "installationNom")
    @Mapping(source = "equipement.id", target = "equipementId")
    @Mapping(source = "equipement.nomEquipement", target = "equipementNom")
    @Mapping(source = "visitePrealable.id", target = "visitePrealableId")
    @Mapping(source = "visitePrealable.effectuee", target = "visiteEffectuee", defaultValue = "false")
    @Mapping(target = "atCreable", ignore = true) // Calculé dynamiquement dans le service
    DemandeInterventionResponse toResponse(DemandeIntervention entity);

    // Note: DemandeIntervention has no collection fields, mapping should work
}
