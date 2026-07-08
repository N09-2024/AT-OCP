package com.ocp.at.controller;

import com.ocp.at.dto.request.InstallationRequest;
import com.ocp.at.dto.response.InstallationResponse;
import com.ocp.at.service.InstallationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/installations")
@RequiredArgsConstructor
@Tag(name = "Installation", description = "API de gestion des Installation")
public class InstallationController {

    private final InstallationService service;

    @GetMapping
    @Operation(summary = "Lister tous les Installation")
    public List<InstallationResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) Installation par ID")
    public InstallationResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<InstallationResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Créer un(e) Installation")
    public InstallationResponse create(@Valid @RequestBody InstallationRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Modifier un(e) Installation")
    public InstallationResponse update(@PathVariable String id, @Valid @RequestBody InstallationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Supprimer un(e) Installation")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

