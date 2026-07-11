package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveResponse {

    private String id;
    private String numeroAT;
    private LocalDateTime dateArchivage;
    private String archiveParId;
    private String archiveParMatricule;
    private String archiveParNom;
    private Integer version;
    private String hashDocument;
    private String cheminPdf;
    private Long taillePdf;
    private String checksum;
    private LocalDateTime createdAt;
    private String autorisationTravailId;
    private String downloadUrl;
}
