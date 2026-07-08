package com.ocp.at.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyseIADTO {
    private String id;
    private LocalDateTime dateAnalyse;
    private String ocrText;
    private Double tauxConfiance;
    private String resultat;
    private String commentaireIA;
    private Long tempsExecution;
    private String modeleUtilise;
    private String versionModele;
}
