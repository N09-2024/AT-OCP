package com.ocp.at.dto;

import com.ocp.at.entity.enums.TypeDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentOrigineDTO {
    private String id;
    private TypeDocument typeDocument;
    private String objet;
    private LocalDateTime dateCreation;
}
