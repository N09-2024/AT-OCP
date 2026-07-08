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
public class FichierJointDTO {
    private String id;
    private String nom;
    private String type;
    private Long taille;
    private LocalDateTime dateImport;
    private String hashSHA256;
}
