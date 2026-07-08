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
public class PhotoDTO {
    private String id;
    private String nom;
    private String path;
    private String typeMime;
    private Long taille;
    private LocalDateTime dateCreation;
    private Integer ordre;
    private String legende;
}
