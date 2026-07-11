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
public class PdfExportResponse {

    private String downloadUrl;
    private String nomFichier;
    private String hash;
    private LocalDateTime dateGeneration;
    private Long taille;
}
