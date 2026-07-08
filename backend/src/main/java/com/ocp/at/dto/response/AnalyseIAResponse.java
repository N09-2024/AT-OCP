package com.ocp.at.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnalyseIAResponse {
    private String id;
    private LocalDateTime dateAnalyse;
    private String ocrText;
    private String jsonExtraction;
    private Double tauxConfiance;
    private String resultat;
    private String commentaireIA;
    private Long tempsExecution;
    private String modeleUtilise;
    private String versionModele;
    private String permisId;
}
