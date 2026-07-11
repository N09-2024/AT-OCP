package com.ocp.at.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la recherche d'archives.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveSearchRequest {

    private String numeroAT;
    private String numeroArchive;
    private Integer version;
    private LocalDateTime dateArchivageDebut;
    private LocalDateTime dateArchivageFin;
    private String createdBy;
    private String archiveStatus;
}