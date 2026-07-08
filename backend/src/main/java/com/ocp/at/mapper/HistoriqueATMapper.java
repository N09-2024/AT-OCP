package com.ocp.at.mapper;

import com.ocp.at.dto.response.HistoriqueATResponse;
import com.ocp.at.entity.HistoriqueAT;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoriqueATMapper {

    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    @Mapping(target = "utilisateurNomComplet",
             expression = "java(h.getUtilisateur() != null ? h.getUtilisateur().getPrenom() + \" \" + h.getUtilisateur().getNom() : null)")
    @Mapping(target = "atId", source = "autorisationTravail.id")
    HistoriqueATResponse toResponse(HistoriqueAT h);
}
