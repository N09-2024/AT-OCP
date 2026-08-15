package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.StatutPermisDocument;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PermisDocumentResponse {
    private String id;
    private String atId;
    private String typePermisAttendu;
    private String fileOriginalName;
    private String fileContentType;
    private StatutPermisDocument statut;
    private LocalDateTime dateUpload;
    private LocalDateTime dateAnalyse;
    private String typeExtrait;
    private String dateDebutExtrait;
    private String dateFinExtrait;
    private String responsablesExtraits;
    private String motifRejet;
    private Double scoreConfiance;
    private String commentaireIA;
}
