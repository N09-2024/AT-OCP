package com.ocp.at.controller;

import com.ocp.at.dto.request.RisqueRequest;
import com.ocp.at.dto.response.RisqueResponse;
import com.ocp.at.service.RisqueService;
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
@RequestMapping("/api/risques")
@RequiredArgsConstructor
@Tag(name = "Risque", description = "API de gestion des Risque")
public class RisqueController {

    private final RisqueService service;

    @GetMapping
    @Operation(summary = "Lister tous les Risque")
    public List<RisqueResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) Risque par ID")
    public RisqueResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<RisqueResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Créer un(e) Risque")
    public RisqueResponse create(@Valid @RequestBody RisqueRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Modifier un(e) Risque")
    public RisqueResponse update(@PathVariable String id, @Valid @RequestBody RisqueRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Supprimer un(e) Risque")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

