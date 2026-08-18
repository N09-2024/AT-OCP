package com.ocp.at.controller;

import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;
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
 * Endpoints d'assistance IA (CrewAI / LangChain / RAG).
 * Rôle strictement consultatif : ne délivre aucun visa et ne modifie aucun statut AT.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Assistance IA", description = "Endpoints IA pour l'analyse d'AT et le chat conversationnel RAG")
@SecurityRequirement(name = "bearerAuth")
public class AssistanceIAController {

    private final AssistanceIAService assistanceIAService;

    @PostMapping({"/api/ai/analyze-at", "/api/ia/analyze-at"})
    @Operation(summary = "Analyse complète d'une AT par l'orchestration multi-agents (CrewAI + LangChain + RAG)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnalyzeAtResponse> analyzeAt(@RequestBody AnalyzeAtRequest request) {
        return ResponseEntity.ok(assistanceIAService.analyzeAt(request));
    }

    @PostMapping({"/api/ai/chat", "/api/ia/chat"})
    @Operation(summary = "Assistant conversationnel s'appuyant sur le RAG et les connaissances OCP")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(assistanceIAService.chat(request));
    }

    @PostMapping("/api/ia/analyser-intervention")
    @Operation(summary = "Suggestions risques / EPI / mesures / permis à partir de la description (Rétrocompatible)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnalyseInterventionIAResponse> analyser(@RequestBody AnalyseRequest body) {
        return ResponseEntity.ok(assistanceIAService.analyserIntervention(body.getDescription()));
    }

    @PostMapping("/api/ia/controler-dossier")
    @Operation(summary = "Contrôle de complétude avant soumission CEEP (Rétrocompatible)")
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
