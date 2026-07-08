package com.ocp.at.mapper;

import com.ocp.at.dto.response.AnalyseRisqueResponse;
import com.ocp.at.entity.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AnalyseRisqueMapper {

    @Mapping(source = "visitePrealable.id", target = "visitePrealableId")
    @Mapping(source = "analyseur.id", target = "analyseurId")
    @Mapping(target = "analyseurNomComplet", expression = "java(entity.getAnalyseur() != null ? entity.getAnalyseur().getNom() + \" \" + entity.getAnalyseur().getPrenom() : null)")
    @Mapping(target = "risquesIds",      expression = "java(mapRisquesIds(entity.getRisques()))")
    @Mapping(target = "risquesNoms",     expression = "java(mapRisquesNoms(entity.getRisques()))")
    @Mapping(target = "mesuresIds",      expression = "java(mapMesuresIds(entity.getMesures()))")
    @Mapping(target = "mesuresNoms",     expression = "java(mapMesuresNoms(entity.getMesures()))")
    @Mapping(target = "episIds",         expression = "java(mapEpisIds(entity.getEpis()))")
    @Mapping(target = "episNoms",        expression = "java(mapEpisNoms(entity.getEpis()))")
    @Mapping(target = "moyensAccesIds",  expression = "java(mapMoyensIds(entity.getMoyensAcces()))")
    @Mapping(target = "moyensAccesNoms", expression = "java(mapMoyensNoms(entity.getMoyensAcces()))")
    AnalyseRisqueResponse toResponse(AnalyseRisque entity);

    default List<String> mapRisquesIds(Set<Risque> risques) {
        if (risques == null) return List.of();
        return risques.stream().map(Risque::getId).collect(Collectors.toList());
    }

    default List<String> mapRisquesNoms(Set<Risque> risques) {
        if (risques == null) return List.of();
        return risques.stream().map(Risque::getNomRisque).collect(Collectors.toList());
    }

    default List<String> mapMesuresIds(Set<MesurePreparation> mesures) {
        if (mesures == null) return List.of();
        return mesures.stream().map(MesurePreparation::getId).collect(Collectors.toList());
    }

    default List<String> mapMesuresNoms(Set<MesurePreparation> mesures) {
        if (mesures == null) return List.of();
        return mesures.stream().map(MesurePreparation::getNomMesure).collect(Collectors.toList());
    }

    default List<String> mapEpisIds(Set<EPI> epis) {
        if (epis == null) return List.of();
        return epis.stream().map(EPI::getId).collect(Collectors.toList());
    }

    default List<String> mapEpisNoms(Set<EPI> epis) {
        if (epis == null) return List.of();
        return epis.stream().map(EPI::getNomEPI).collect(Collectors.toList());
    }

    default List<String> mapMoyensIds(Set<MoyenAcces> moyens) {
        if (moyens == null) return List.of();
        return moyens.stream().map(MoyenAcces::getId).collect(Collectors.toList());
    }

    default List<String> mapMoyensNoms(Set<MoyenAcces> moyens) {
        if (moyens == null) return List.of();
        return moyens.stream().map(MoyenAcces::getNomMoyen).collect(Collectors.toList());
    }
}
