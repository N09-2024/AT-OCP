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
    // Zones territoriales P/E (standard OCP S-HSE-SEC-31)
    @Mapping(target = "zoneProprietaireId", expression = "java(entity.getZoneProprietaire() != null ? entity.getZoneProprietaire().getId() : null)")
    @Mapping(target = "zoneProprietaireNom", expression = "java(entity.getZoneProprietaire() != null ? entity.getZoneProprietaire().getNomZone() : null)")
    @Mapping(target = "zoneExecutanteId", expression = "java(entity.getZoneExecutante() != null ? entity.getZoneExecutante().getId() : null)")
    @Mapping(target = "zoneExecutanteNom", expression = "java(entity.getZoneExecutante() != null ? entity.getZoneExecutante().getNomZone() : null)")
    AutorisationTravailResponse toResponse(AutorisationTravail entity);
}
