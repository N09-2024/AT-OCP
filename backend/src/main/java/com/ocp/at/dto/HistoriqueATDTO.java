package com.ocp.at.dto;

import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueATDTO {
    private String id;
    private LocalDateTime dateAction;
    private TypeActionAT action;
    private StatutAT ancienStatut;
    private StatutAT nouveauStatut;
    private String commentaire;
}
