package com.ocp.at.controller;

import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.service.AssistanceIAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints d'assistance IA (suggestions + contrôle).
 * Ne délivrent aucun visa et ne changent aucun statut AT.
 */
@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
@Tag(name = "Assistance IA", description = "LangChain/CrewAI/Mock — aide au formulaire F-HSE-SEC-31-04")
@SecurityRequirement(name = "bearerAuth")
public class AssistanceIAController {

    private final AssistanceIAService assistanceIAService;

    @PostMapping("/analyser-intervention")
    @Operation(summary = "Suggestions risques / EPI / mesures / permis à partir de la description")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnalyseInterventionIAResponse> analyser(@RequestBody AnalyseRequest body) {
        return ResponseEntity.ok(assistanceIAService.analyserIntervention(body.getDescription()));
    }

    @PostMapping("/controler-dossier")
    @Operation(summary = "Contrôle de complétude avant soumission CEEP (agents CrewAI)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnalyseInterventionIAResponse> controler(@RequestBody ControleRequest body) {
        return ResponseEntity.ok(assistanceIAService.controlerDossier(
                body.getDescription(),
                body.getVisiteFaite(),
                body.getNbRisques(),
                body.getNbMesures(),
                body.getNbEpis(),
                body.getNbPermis(),
                body.getSectionFRenseignee()));
    }

    @Data
    public static class AnalyseRequest {
        private String description;
    }

    @Data
    public static class ControleRequest {
        private String description;
        private Boolean visiteFaite;
        private Integer nbRisques;
        private Integer nbMesures;
        private Integer nbEpis;
        private Integer nbPermis;
        private Boolean sectionFRenseignee;
    }
}
