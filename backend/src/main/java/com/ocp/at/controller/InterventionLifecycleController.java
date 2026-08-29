package com.ocp.at.controller;

import com.ocp.at.dto.request.EndInterventionRequest;
import com.ocp.at.dto.request.StartInterventionRequest;
import com.ocp.at.dto.response.ArchiveReadinessResponse;
import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.dto.response.ReadinessCheckResponse;
import com.ocp.at.service.ArchiveService;
import com.ocp.at.service.AutorisationTravailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autorisations-travail")
@RequiredArgsConstructor
@Tag(name = "Cycle de Vie AT", description = "Endpoints de gestion du cycle de vie post-validation (Démarrage, Fin, Contrôles de complétude)")
public class InterventionLifecycleController {

    private final AutorisationTravailService atService;
    private final ArchiveService archiveService;

    @GetMapping("/{id}/intervention/readiness")
    @Operation(summary = "Étape 4 - Contrôle déterministe pré-démarrage (Readiness Check 13 points)")
    @PreAuthorize("hasAuthority('READ_AT') or isAuthenticated()")
    public ResponseEntity<ReadinessCheckResponse> getInterventionReadiness(@PathVariable String id) {
        return ResponseEntity.ok(atService.getInterventionReadiness(id));
    }

    @PostMapping("/{id}/intervention/start")
    @Operation(summary = "Étape 4 - Démarrer l'intervention avec préconditions déterministes (CEEE)")
    @PreAuthorize("hasAuthority('START_INTERVENTION') or hasRole('CEEE') or hasRole('CE') or hasRole('ADMIN')")
    public ResponseEntity<AutorisationTravailResponse> startIntervention(
            @PathVariable String id,
            @RequestBody(required = false) StartInterventionRequest request) {
        return ResponseEntity.ok(atService.demarrerIntervention(id));
    }

    @PostMapping("/{id}/intervention/end")
    @Operation(summary = "Étape 6 - Déclarer la fin des travaux avec rapport de fin de chantier (CEEE)")
    @PreAuthorize("hasAuthority('DECLARE_FIN_TRAVAUX') or hasRole('CEEE') or hasRole('CE') or hasRole('ADMIN')")
    public ResponseEntity<AutorisationTravailResponse> endIntervention(
            @PathVariable String id,
            @Valid @RequestBody EndInterventionRequest request) {
        return ResponseEntity.ok(atService.declarerFinTravaux(id, request));
    }

    @GetMapping("/{id}/archive/readiness")
    @Operation(summary = "Étape 8 - Contrôle de complétude du dossier avant archivage officiel (HCEP/HCEE/HMEP)")
    @PreAuthorize("hasAuthority('READ_AT') or isAuthenticated()")
    public ResponseEntity<ArchiveReadinessResponse> getArchiveReadiness(@PathVariable String id) {
        return ResponseEntity.ok(archiveService.getArchiveReadiness(id));
    }
}
