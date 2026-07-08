package com.ocp.at.dto;

import com.ocp.at.entity.enums.StatutVisa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaDTO {
    private String id;
    private LocalDateTime dateVisa;
    private StatutVisa statut;
    private String commentaire;
    private String utilisateurNom;
}
