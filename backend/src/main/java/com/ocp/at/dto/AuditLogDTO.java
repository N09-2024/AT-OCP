package com.ocp.at.dto;

import com.ocp.at.entity.enums.ResultatAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    private String id;
    private LocalDateTime date;
    private String action;
    private ResultatAudit resultat;
    private String adresseIP;
    private String navigateur;
    private String utilisateurEmail;
}
