package com.ocp.at.controller;

import com.ocp.at.dto.request.ZoneRequest;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.service.ServiceService;
import com.ocp.at.service.ZoneService;
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
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Tag(name = "Zone", description = "API de gestion des Zone")
public class ZoneController {

    private final ZoneService service;
    private final ServiceService serviceService;

    @GetMapping
    @Operation(summary = "Lister tous les Zone")
    public List<ZoneResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) Zone par ID")
    public ZoneResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<ZoneResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Créer un(e) Zone")
    public ZoneResponse create(@Valid @RequestBody ZoneRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Modifier un(e) Zone")
    public ZoneResponse update(@PathVariable String id, @Valid @RequestBody ZoneRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Supprimer un(e) Zone")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/{id}/services")
    @Operation(summary = "Lister les services d'une zone")
    public List<ServiceResponse> getServicesByZone(@PathVariable String id) {
        return serviceService.getByZoneId(id);
    }
}

