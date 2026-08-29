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
public class VerificationQrResponse {
    private Boolean valide;
    private String numeroAT;
    private String numeroArchive;
    private String statut;
    private LocalDateTime dateArchivage;
    private String archiveParNomComplet;
    private String hashSHA256;
    private String installation;
    private String zone;
    private String message;
}
