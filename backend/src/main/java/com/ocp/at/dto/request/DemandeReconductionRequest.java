package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeReconductionRequest {

    @NotNull(message = "La nouvelle date et heure de fin est obligatoire")
    private LocalDateTime nouvelleDateFin;

    @NotBlank(message = "Le motif de la reconduction est obligatoire")
    private String motif;

    private String commentaire;
}
