package com.ocp.at.controller;

import com.ocp.at.dto.request.ServiceRequest;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.security.ATContextService;
import com.ocp.at.service.ServiceService;
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
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Service", description = "API de gestion des Service")
public class ServiceController {

    private final ServiceService service;
    private final ATContextService atContextService;

    @GetMapping
    @Operation(summary = "Lister tous les Service")
    public List<ServiceResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un(e) Service par ID")
    public ServiceResponse getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher avec pagination")
    public Page<ServiceResponse> search(@RequestParam(required = false) String query, Pageable pageable) {
        return service.search(query, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Créer un(e) Service")
    public ServiceResponse create(@Valid @RequestBody ServiceRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Modifier un(e) Service")
    public ServiceResponse update(@PathVariable String id, @Valid @RequestBody ServiceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_REFERENTIELS')")
    @Operation(summary = "Supprimer un(e) Service")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    /**
     * Chefs d'équipe du service (rôles CEEP/CEEE).
     * Utilisé par le formulaire AT : en choisissant le service intervenant (E),
     * le nom du CEEE apparaît automatiquement.
     * Position P/E réelle = résolue par ATContextService selon la zone de l'AT.
     */
    @GetMapping("/{id}/chefs-equipe")
    @Operation(summary = "Lister les chefs d'équipe rattachés à un service (pour CEEE formulaire)")
    @PreAuthorize("hasAuthority('READ_AT') or hasAuthority('CREATE_AT')")
    public List<Map<String, String>> getChefsEquipe(@PathVariable String id) {
        return atContextService.findChefsEquipeByService(id).stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "nom", u.getNom() != null ? u.getNom() : "",
                        "prenom", u.getPrenom() != null ? u.getPrenom() : "",
                        "email", u.getEmail() != null ? u.getEmail() : "",
                        "displayName", ((u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "")).trim()
                ))
                .collect(Collectors.toList());
    }
}

