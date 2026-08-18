package com.ocp.at.dto.request;

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
public class AnalyzeAtRequest {

    private String atId;
    private String description;
    private String typeIntervention;
    private String niveau;
    private String installation;
    private String equipement;

    @Builder.Default
    private List<String> risques = new ArrayList<>();

    @Builder.Default
    private List<String> mesures = new ArrayList<>();

    @Builder.Default
    private List<String> epi = new ArrayList<>();

    @Builder.Default
    private List<String> moyensAcces = new ArrayList<>();

    private Boolean visiteFaite;
    private Integer nbRisques;
    private Integer nbMesures;
    private Integer nbEpis;
    private Integer nbPermis;
    private Boolean sectionFRenseignee;
}
