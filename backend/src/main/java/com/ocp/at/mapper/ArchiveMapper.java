package com.ocp.at.mapper;

import com.ocp.at.dto.response.ArchiveResponse;
import com.ocp.at.entity.ArchiveAT;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArchiveMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "numeroAT", source = "autorisationTravail.numero")
    @Mapping(target = "dateArchivage", source = "dateArchivage")
    @Mapping(target = "archiveParId", source = "archivePar.id")
    @Mapping(target = "archiveParMatricule", source = "archivePar.matricule")
    @Mapping(target = "archiveParNom", expression = "java(archive.getArchivePar() != null ? archive.getArchivePar().getNom() + ' ' + archive.getArchivePar().getPrenom() : null)")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "hashDocument", source = "hashSHA256")
    @Mapping(target = "cheminPdf", source = "pathPdf")
    @Mapping(target = "taillePdf", source = "taille")
    @Mapping(target = "checksum", source = "hashSHA256")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "autorisationTravailId", source = "autorisationTravail.id")
    @Mapping(target = "downloadUrl", expression = "java(\"/api/archives/\" + archive.getId() + \"/download\")")
    ArchiveResponse toResponse(ArchiveAT archive);
}
