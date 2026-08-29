package com.ocp.at.controller;

import com.ocp.at.dto.request.DecisionReconductionRequest;
import com.ocp.at.dto.request.DemandeReconductionRequest;
import com.ocp.at.dto.response.ReconductionResponse;
import com.ocp.at.service.ReconductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reconductions", description = "Gestion du workflow de reconduction des AT (CEEE -> HMEP Responsable OCP)")
public class ReconductionController {

    private final ReconductionService reconductionService;

    @PostMapping("/autorisations-travail/{atId}/reconductions")
    @Operation(summary = "Étape 5b - Formuler une demande de reconduction (CEEE)")
    @PreAuthorize("hasAuthority('REQUEST_EXTENSION') or hasAuthority('RENEW_AT') or hasRole('CEEE') or hasRole('CE') or hasRole('ADMIN')")
    public ResponseEntity<ReconductionResponse> demanderReconduction(
            @PathVariable String atId,
            @Valid @RequestBody DemandeReconductionRequest request) {
        return ResponseEntity.ok(reconductionService.demanderReconduction(atId, request));
    }

    @GetMapping("/autorisations-travail/{atId}/reconductions")
    @Operation(summary = "Consulter l'historique des reconductions d'une AT")
    @PreAuthorize("hasAuthority('READ_AT') or isAuthenticated()")
    public ResponseEntity<List<ReconductionResponse>> getReconductionsByAtId(@PathVariable String atId) {
        return ResponseEntity.ok(reconductionService.getReconductionsByAtId(atId));
    }

    @GetMapping("/reconductions/pending")
    @Operation(summary = "Consulter les demandes de reconduction en attente de décision (HMEP / Responsable OCP)")
    @PreAuthorize("hasAuthority('APPROVE_EXTENSION') or hasRole('HMEP') or hasRole('HM') or hasRole('ADMIN')")
    public ResponseEntity<List<ReconductionResponse>> getPendingReconductions() {
        return ResponseEntity.ok(reconductionService.getPendingReconductions());
    }

    @GetMapping("/reconductions/{id}")
    @Operation(summary = "Consulter le détail d'une demande de reconduction")
    @PreAuthorize("hasAuthority('READ_AT') or isAuthenticated()")
    public ResponseEntity<ReconductionResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(reconductionService.getById(id));
    }

    @PostMapping("/reconductions/{id}/decider")
    @Operation(summary = "Étape 5b - Approuver ou refuser une reconduction (HMEP / Responsable OCP)")
    @PreAuthorize("hasAuthority('APPROVE_EXTENSION') or hasRole('HMEP') or hasRole('HM') or hasRole('ADMIN')")
    public ResponseEntity<ReconductionResponse> deciderReconduction(
            @PathVariable String id,
            @Valid @RequestBody DecisionReconductionRequest request) {
        return ResponseEntity.ok(reconductionService.deciderReconduction(id, request));
    }
}
