package com.ocp.at.dto.request;

import lombok.Data;

@Data
public class ClassificationInterventionRequest {
    private String niveau;           // NIVEAU_1 ou NIVEAU_2
    private Boolean estTiers;
    private String natureIntervention;
    private String zoneId;
    private String serviceId;
    private String observations;
}
