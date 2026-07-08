package com.ocp.at.controller;

import com.ocp.at.dto.request.EPIRequest;
import com.ocp.at.dto.response.EPIResponse;
import com.ocp.at.service.EPIService;
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
@RequestMapping("/api/epis")
@RequiredArgsConstructor
@Tag(name = "EPI", description = "API de gestion des EPI")
public class EPIController {

    private final EPIService service;

    @GetMapping
    @Operation(summary = "Lister tous les EPI")
    public List<EPIResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) EPI par ID")
    public EPIResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<EPIResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Créer un(e) EPI")
    public EPIResponse create(@Valid @RequestBody EPIRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Modifier un(e) EPI")
    public EPIResponse update(@PathVariable String id, @Valid @RequestBody EPIRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Supprimer un(e) EPI")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}

