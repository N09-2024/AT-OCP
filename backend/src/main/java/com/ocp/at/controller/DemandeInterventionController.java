package com.ocp.at.controller;

import com.ocp.at.dto.request.DemandeInterventionRequest;
import com.ocp.at.dto.response.DemandeInterventionResponse;
import com.ocp.at.service.DemandeInterventionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demandes-intervention")
@RequiredArgsConstructor
@Tag(name = "Demandes d'Intervention", description = "API de gestion des DI")
@SecurityRequirement(name = "bearerAuth")
public class DemandeInterventionController {

    private final DemandeInterventionService service;

    @GetMapping
    @Operation(summary = "Lister toutes les DI")
    public ResponseEntity<Page<DemandeInterventionResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une DI par son ID")
    public ResponseEntity<DemandeInterventionResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle DI")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<DemandeInterventionResponse> create(
            @Valid @RequestBody DemandeInterventionRequest request,
            Authentication authentication) {
        // En pratique on récupère l'ID depuis le principal
        String demandeurId = authentication.getName(); // Assumant que getName() retourne l'ID ou le matricule, on adaptera
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, null)); // null temporaire
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une DI")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<DemandeInterventionResponse> update(
            @PathVariable String id,
            @Valid @RequestBody DemandeInterventionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler une DI")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
