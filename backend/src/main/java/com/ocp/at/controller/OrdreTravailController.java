package com.ocp.at.controller;

import com.ocp.at.dto.request.OrdreTravailRequest;
import com.ocp.at.dto.response.OrdreTravailResponse;
import com.ocp.at.service.OrdreTravailService;
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
@RequestMapping("/api/ordres-travail")
@RequiredArgsConstructor
@Tag(name = "Ordres de Travail", description = "API de gestion des OT")
@SecurityRequirement(name = "bearerAuth")
public class OrdreTravailController {

    private final OrdreTravailService service;

    @GetMapping
    @Operation(summary = "Lister tous les OT")
    public ResponseEntity<Page<OrdreTravailResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un OT par son ID")
    public ResponseEntity<OrdreTravailResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel OT")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<OrdreTravailResponse> create(
            @Valid @RequestBody OrdreTravailRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, null));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un OT")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<OrdreTravailResponse> update(
            @PathVariable String id,
            @Valid @RequestBody OrdreTravailRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Annuler un OT")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
