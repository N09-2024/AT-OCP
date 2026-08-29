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
public class ArchiveReadinessResponse {
    private Boolean readyForArchive;
    @Builder.Default
    private List<String> blockingReasons = new ArrayList<>();
    @Builder.Default
    private List<ReadinessCheckItem> checklist = new ArrayList<>();
    private String atNumero;
    private String statut;
    private Boolean hasCloture;
    private Boolean hasVisasComplets;
    private Boolean hasReceptionConjointe;
    private Boolean hasPermisConformes;
}
