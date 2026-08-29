package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionReconductionRequest {

    @NotNull(message = "La décision (approuvé ou refusé) est obligatoire")
    private Boolean approuve;

    /** Motif de refus (obligatoire si approuve = false) */
    private String motifRefus;

    private String commentaire;
}
