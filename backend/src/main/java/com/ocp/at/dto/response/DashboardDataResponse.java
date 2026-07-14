package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataResponse {
    private KpiStats kpis;
    private List<MonthlyStat> monthlyStats;
    private Map<String, Long> statusDistribution;
    private List<AtSummaryDto> recentAutorisations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiStats {
        private long autorisationsEnCours;
        private long visasEnAttente;
        private long permisActifs;
        private long receptionsEnAttente;
        private long totalArchives;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String mois;
        private long total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtSummaryDto {
        private String id;
        private String titre;
        private String installation;
        private String statut;
        private String echeance;
    }
}
