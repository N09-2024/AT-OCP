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
public class PhotoReceptionResponse {

    private String id;
    private String nom;
    private String path;
    private Long taille;
    private String mimeType;
    private Integer ordre;
    private String legende;
    private LocalDateTime createdAt;
    private String downloadUrl;
}
