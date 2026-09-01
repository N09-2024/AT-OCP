package com.ocp.at.mapper;

import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.ReceptionTravaux;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PhotoReceptionMapper.class})
public interface ReceptionTravauxMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autorisationTravail", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "historiques", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dateReception", ignore = true)
    @Mapping(target = "dateDebutTravauxReelle", ignore = true)
    @Mapping(target = "dateFinTravauxReelle", ignore = true)
    @Mapping(target = "signaturePath", ignore = true)
    @Mapping(target = "signatureDate", ignore = true)
    @Mapping(target = "signatureBy", ignore = true)
    @Mapping(target = "signatureResponsable", ignore = true)
    @Mapping(target = "dateSignature", ignore = true)
    @Mapping(target = "validee", ignore = true)
    ReceptionTravaux toEntity(ReceptionTravauxRequest request);

    @Mapping(target = "autorisationTravailId", source = "autorisationTravail.id")
    @Mapping(target = "autorisationTravailNumero", source = "autorisationTravail.numero")
    @Mapping(target = "responsableId", source = "responsable.id")
    @Mapping(target = "responsableMatricule", source = "responsable.matricule")
    @Mapping(target = "responsableNom", expression = "java(reception.getResponsable() != null ? reception.getResponsable().getNom() + ' ' + reception.getResponsable().getPrenom() : null)")
    @Mapping(target = "photos", source = "photos")
    @Mapping(target = "atCloturee", source = "autorisationTravail.statut", qualifiedByName = "isCloturee")
    ReceptionTravauxResponse toResponse(ReceptionTravaux reception);

    @Named("isCloturee")
    default Boolean isCloturee(com.ocp.at.entity.enums.StatutAT statut) {
        return statut == com.ocp.at.entity.enums.StatutAT.CLOTUREE;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autorisationTravail", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "historiques", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dateReception", ignore = true)
    @Mapping(target = "dateDebutTravauxReelle", ignore = true)
    @Mapping(target = "dateFinTravauxReelle", ignore = true)
    @Mapping(target = "signaturePath", ignore = true)
    @Mapping(target = "signatureDate", ignore = true)
    @Mapping(target = "signatureBy", ignore = true)
    @Mapping(target = "signatureResponsable", ignore = true)
    @Mapping(target = "dateSignature", ignore = true)
    @Mapping(target = "validee", ignore = true)
    void updateFromRequest(ReceptionTravauxRequest request, @MappingTarget ReceptionTravaux reception);
}
