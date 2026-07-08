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
public class NotificationDTO {
    private String id;
    private String titre;
    private String message;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;
    private boolean lu;
}
