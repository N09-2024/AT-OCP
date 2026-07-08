package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.entity.enums.TypePermis;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PermisResponse {
    private String id;
    private String numero;
    private TypePermis type;
    private LocalDate dateEmission;
    private LocalDate dateExpiration;
    private StatutPermis statutVerification;
    private Boolean estObligatoire;
    private String commentaire;
    private String fichierJointId;
    private String fichierJointNom;
    private String analyseIAId;
}
