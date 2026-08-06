package com.ocp.at.controller;

import com.ocp.at.dto.request.MoyenAccesRequest;
import com.ocp.at.dto.response.MoyenAccesResponse;
import com.ocp.at.service.MoyenAccesService;
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
@RequestMapping("/api/moyens-acces")
@RequiredArgsConstructor
@Tag(name = "MoyenAcces", description = "API de gestion des MoyenAcces")
public class MoyenAccesController {

    private final MoyenAccesService service;

    @GetMapping
    @Operation(summary = "Lister tous les MoyenAcces")
    public List<MoyenAccesResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) MoyenAcces par ID")
    public MoyenAccesResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<MoyenAccesResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer un(e) MoyenAcces")
    public MoyenAccesResponse create(@Valid @RequestBody MoyenAccesRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Modifier un(e) MoyenAcces")
    public MoyenAccesResponse update(@PathVariable String id, @Valid @RequestBody MoyenAccesRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer un(e) MoyenAcces")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

