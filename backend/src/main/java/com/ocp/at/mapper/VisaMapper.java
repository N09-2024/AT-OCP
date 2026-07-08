package com.ocp.at.mapper;

import com.ocp.at.dto.response.VisaResponse;
import com.ocp.at.entity.Visa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VisaMapper {

    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "utilisateurNomComplet", expression = "java(visa.getUtilisateur().getPrenom() + \" \" + visa.getUtilisateur().getNom())")
    @Mapping(target = "autorisationTravailId", source = "autorisationTravail.id")
    @Mapping(target = "signaturePresente", expression = "java(visa.getSignaturePath() != null)")
    VisaResponse toResponse(Visa visa);
}
