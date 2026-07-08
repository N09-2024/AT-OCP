package com.ocp.at.mapper;

import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.ReceptionTravaux;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {EssaiMapper.class, RemiseEtatMapper.class})
public interface ReceptionTravauxMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autorisationTravail", ignore = true)
    @Mapping(target = "essais", ignore = true)
    @Mapping(target = "remiseEtat", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "validee", ignore = true)
    @Mapping(target = "dateValidation", ignore = true)
    @Mapping(target = "essaisConformes", ignore = true)
    ReceptionTravaux toEntity(ReceptionTravauxRequest request);

    @Mapping(target = "autorisationTravailId", source = "autorisationTravail.id")
    @Mapping(target = "autorisationTravailNumero", source = "autorisationTravail.numero")
    ReceptionTravauxResponse toResponse(ReceptionTravaux reception);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autorisationTravail", ignore = true)
    @Mapping(target = "essais", ignore = true)
    @Mapping(target = "remiseEtat", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "validee", ignore = true)
    @Mapping(target = "dateValidation", ignore = true)
    @Mapping(target = "essaisConformes", ignore = true)
    void updateFromRequest(ReceptionTravauxRequest request, @MappingTarget ReceptionTravaux reception);
}
