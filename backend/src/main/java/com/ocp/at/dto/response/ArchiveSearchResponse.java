package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse de recherche d'archives.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveSearchResponse {

    private String id;
    private String numeroAT;
    private String numeroArchive;
    private Integer version;
    private LocalDateTime dateArchivage;
    private String archiveParNom;
    private String archiveStatus;
    private String hashDocument;
    private Long taillePdf;
}