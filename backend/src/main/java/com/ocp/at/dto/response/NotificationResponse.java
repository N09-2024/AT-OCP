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
public class NotificationResponse {

    private String id;
    private String titre;
    private String message;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;
    private boolean lu;
    private String type;
    private String lien;
    private String utilisateurId;
}
