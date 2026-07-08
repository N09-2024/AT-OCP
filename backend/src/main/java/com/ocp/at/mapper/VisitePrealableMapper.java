package com.ocp.at.mapper;

import com.ocp.at.dto.response.VisitePrealableResponse;
import com.ocp.at.entity.Risque;
import com.ocp.at.entity.VisitePrealable;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PhotoMapper.class})
public interface VisitePrealableMapper {

    /**
     * Mapping de base de l'entité vers le DTO de réponse.
     * Les champs calculés (analyseCreee, documentSourceId, etc.)
     * sont renseignés manuellement dans le service.
     */
    @Mapping(source = "visiteur.id", target = "visiteurId")
    @Mapping(target = "visiteurNomComplet", expression = "java(entity.getVisiteur() != null ? entity.getVisiteur().getNom() + \" \" + entity.getVisiteur().getPrenom() : null)")
    @Mapping(target = "analyseCreee", expression = "java(entity.getAnalyseRisque() != null)")
    @Mapping(source = "analyseRisque.id", target = "analyseRisqueId")
    @Mapping(source = "photos", target = "photos")
    @Mapping(target = "nombrePhotos", expression = "java(entity.getPhotos() != null ? entity.getPhotos().size() : 0)")
    @Mapping(target = "risquesIdentifiesIds", expression = "java(mapRisquesIds(entity.getRisquesIdentifies()))")
    // Ces champs sont peuplés dans le service selon la source (DI, OT, BT)
    @Mapping(target = "documentSourceId", ignore = true)
    @Mapping(target = "typeDocumentSource", ignore = true)
    @Mapping(target = "documentSourceNumero", ignore = true)
    VisitePrealableResponse toResponse(VisitePrealable entity);

    default List<String> mapRisquesIds(Set<Risque> risques) {
        if (risques == null) return List.of();
        return risques.stream().map(Risque::getId).collect(Collectors.toList());
    }
}
