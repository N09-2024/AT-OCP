package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadinessCheckResponse {
    private Boolean ready;
    private String autorisationTravailId;
    private String numero;
    private String zone;
    private String equipement;
    private String ceeeNomComplet;
    @Builder.Default
    private List<ReadinessCheckItem> checks = new ArrayList<>();
    private String summary;
    private Integer passedCount;
    private Integer totalCount;
}
