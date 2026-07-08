package com.ocp.at.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
