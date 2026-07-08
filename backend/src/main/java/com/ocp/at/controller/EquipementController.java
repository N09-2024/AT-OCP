package com.ocp.at.controller;

import com.ocp.at.dto.request.EquipementRequest;
import com.ocp.at.dto.response.EquipementResponse;
import com.ocp.at.service.EquipementService;
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
@RequestMapping("/api/equipements")
@RequiredArgsConstructor
@Tag(name = "Equipement", description = "API de gestion des Equipement")
public class EquipementController {

    private final EquipementService service;

    @GetMapping
    @Operation(summary = "Lister tous les Equipement")
    public List<EquipementResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) Equipement par ID")
    public EquipementResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<EquipementResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Créer un(e) Equipement")
    public EquipementResponse create(@Valid @RequestBody EquipementRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Modifier un(e) Equipement")
    public EquipementResponse update(@PathVariable String id, @Valid @RequestBody EquipementRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Supprimer un(e) Equipement")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

