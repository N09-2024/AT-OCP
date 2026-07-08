package com.ocp.at.mapper;

import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.entity.AutorisationTravail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutorisationTravailMapper {

    @Mapping(source = "proprietaireBrouillon.id", target = "proprietaireBrouillonId")
    @Mapping(target = "proprietaireBrouillonNomComplet", expression = "java(entity.getProprietaireBrouillon() != null ? entity.getProprietaireBrouillon().getNom() + \" \" + entity.getProprietaireBrouillon().getPrenom() : null)")
    // Les champs de documentSource seront populés dans le Service car ils dépendent de DI / OT / BT
    @Mapping(target = "typeDocumentSource", ignore = true)
    @Mapping(target = "documentSourceId", ignore = true)
    @Mapping(target = "documentSourceNumero", ignore = true)
    AutorisationTravailResponse toResponse(AutorisationTravail entity);
}
