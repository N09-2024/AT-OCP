package com.ocp.at.controller;

import com.ocp.at.dto.request.EntrepriseExterneRequest;
import com.ocp.at.dto.response.EntrepriseExterneResponse;
import com.ocp.at.service.EntrepriseExterneService;
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
@RequestMapping("/api/entreprises-externes")
@RequiredArgsConstructor
@Tag(name = "EntrepriseExterne", description = "API de gestion des EntrepriseExterne")
public class EntrepriseExterneController {

    private final EntrepriseExterneService service;

    @GetMapping
    @Operation(summary = "Lister tous les EntrepriseExterne")
    public List<EntrepriseExterneResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) EntrepriseExterne par ID")
    public EntrepriseExterneResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<EntrepriseExterneResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Créer un(e) EntrepriseExterne")
    public EntrepriseExterneResponse create(@Valid @RequestBody EntrepriseExterneRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Modifier un(e) EntrepriseExterne")
    public EntrepriseExterneResponse update(@PathVariable String id, @Valid @RequestBody EntrepriseExterneRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Supprimer un(e) EntrepriseExterne")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

