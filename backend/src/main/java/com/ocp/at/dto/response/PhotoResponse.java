package com.ocp.at.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Informations d'une photo liée à une Visite Préalable")
public class PhotoResponse {

    private String id;

    private String nom;

    private String path;

    private Long taille;

    private String typeMime;

    private Integer ordre;

    private String legende;

    private LocalDateTime dateCreation;

    private String visitePrealableId;
}
