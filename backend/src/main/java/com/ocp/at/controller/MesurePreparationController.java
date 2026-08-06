package com.ocp.at.controller;

import com.ocp.at.dto.request.MesurePreparationRequest;
import com.ocp.at.dto.response.MesurePreparationResponse;
import com.ocp.at.service.MesurePreparationService;
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
@RequestMapping("/api/mesures-preparation")
@RequiredArgsConstructor
@Tag(name = "MesurePreparation", description = "API de gestion des MesurePreparation")
public class MesurePreparationController {

    private final MesurePreparationService service;

    @GetMapping
    @Operation(summary = "Lister tous les MesurePreparation")
    public List<MesurePreparationResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) MesurePreparation par ID")
    public MesurePreparationResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<MesurePreparationResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Créer un(e) MesurePreparation")
    public MesurePreparationResponse create(@Valid @RequestBody MesurePreparationRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Modifier un(e) MesurePreparation")
    public MesurePreparationResponse update(@PathVariable String id, @Valid @RequestBody MesurePreparationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Supprimer un(e) MesurePreparation")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

