package com.ocp.at.controller;

import com.ocp.at.dto.request.AnalyseRisqueRequest;
import com.ocp.at.dto.response.AnalyseRisqueResponse;
import com.ocp.at.service.AnalyseRisqueService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses-risques")
@RequiredArgsConstructor
@Tag(name = "Analyses des Risques", description = "API de gestion des analyses des risques (Module 5)")
@SecurityRequirement(name = "bearerAuth")
public class AnalyseRisqueController {

    private final AnalyseRisqueService service;

    @GetMapping
    @Operation(summary = "Lister toutes les analyses des risques")
    public ResponseEntity<Page<AnalyseRisqueResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une analyse des risques par son ID")
    public ResponseEntity<AnalyseRisqueResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/visite/{visiteId}")
    @Operation(summary = "Obtenir l'analyse des risques d'une visite préalable")
    public ResponseEntity<AnalyseRisqueResponse> findByVisite(@PathVariable String visiteId) {
        return ResponseEntity.ok(service.findByVisitePrealableId(visiteId));
    }

    @PostMapping
    @Operation(summary = "Créer une analyse des risques (visite doit être finalisée)")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<AnalyseRisqueResponse> create(@Valid @RequestBody AnalyseRisqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une analyse des risques")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<AnalyseRisqueResponse> update(
            @PathVariable String id,
            @Valid @RequestBody AnalyseRisqueRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une analyse des risques")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
