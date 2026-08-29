package com.ocp.at.mapper;

import com.ocp.at.dto.response.ReconductionResponse;
import com.ocp.at.entity.Reconduction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReconductionMapper {

    @Mapping(target = "autorisationTravailId", source = "autorisationTravail.id")
    @Mapping(target = "autorisationTravailNumero", source = "autorisationTravail.numero")
    @Mapping(target = "demandeurId", source = "demandeur.id")
    @Mapping(target = "demandeurNomComplet", expression = "java(reconduction.getDemandeur() != null ? (reconduction.getDemandeur().getPrenom() + \" \" + reconduction.getDemandeur().getNom()).trim() : null)")
    @Mapping(target = "demandeurRole", expression = "java(reconduction.getDemandeur() != null && !reconduction.getDemandeur().getRoles().isEmpty() ? reconduction.getDemandeur().getRoles().iterator().next().getNom() : null)")
    @Mapping(target = "decisionParId", source = "decisionPar.id")
    @Mapping(target = "decisionParNomComplet", expression = "java(reconduction.getDecisionPar() != null ? (reconduction.getDecisionPar().getPrenom() + \" \" + reconduction.getDecisionPar().getNom()).trim() : null)")
    ReconductionResponse toResponse(Reconduction reconduction);
}
