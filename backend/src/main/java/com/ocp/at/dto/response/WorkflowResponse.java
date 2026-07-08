package com.ocp.at.dto.response;

import com.ocp.at.entity.enums.StatutAT;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkflowResponse {
    private String atId;
    private String atNumero;
    private StatutAT statutActuel;
    private List<TransitionInfo> transitionsDisponibles;

    @Data
    @Builder
    public static class TransitionInfo {
        private String action;
        private String etatArrivee;
        private String roleAutorise;
        private Boolean obligatoire;
    }
}
