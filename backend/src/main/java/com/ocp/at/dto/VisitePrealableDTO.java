package com.ocp.at.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitePrealableDTO {
    private String id;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private Double latitude;
    private Double longitude;
    private String commentaire;
    private boolean effectuee;
    private List<PhotoDTO> photos;
}
