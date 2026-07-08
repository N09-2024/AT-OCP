package com.ocp.at.controller;

import com.ocp.at.dto.request.VisitePrealableRequest;
import com.ocp.at.dto.response.PhotoResponse;
import com.ocp.at.dto.response.VisitePrealableResponse;
import com.ocp.at.service.VisitePrealableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/visites-prealables")
@RequiredArgsConstructor
@Tag(name = "Visites Préalables", description = "API de gestion des visites préalables de terrain (Module 4)")
@SecurityRequirement(name = "bearerAuth")
public class VisitePrealableController {

    private final VisitePrealableService service;

    @GetMapping
    @Operation(summary = "Lister toutes les visites préalables")
    public ResponseEntity<Page<VisitePrealableResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une visite préalable par son ID")
    public ResponseEntity<VisitePrealableResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une visite préalable liée à un document (DI, OT ou BT)")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<VisitePrealableResponse> create(@Valid @RequestBody VisitePrealableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une visite préalable (impossible si finalisée)")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<VisitePrealableResponse> update(
            @PathVariable String id,
            @Valid @RequestBody VisitePrealableRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une visite préalable (impossible si une analyse est liée)")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/finaliser")
    @Operation(summary = "Finaliser une visite (GPS + commentaire + photo requis)")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<VisitePrealableResponse> finaliser(@PathVariable String id) {
        return ResponseEntity.ok(service.finaliser(id));
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter une photo à la visite (upload multipart)")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<PhotoResponse> addPhoto(
            @PathVariable String id,
            @Parameter(description = "Fichier image") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Légende optionnelle") @RequestParam(value = "legende", required = false) String legende) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPhoto(id, file, legende));
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @Operation(summary = "Supprimer une photo de la visite (impossible si finalisée)")
    @PreAuthorize("hasAuthority('MANAGE_DOCUMENTS')")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable String id,
            @PathVariable String photoId) {
        service.deletePhoto(id, photoId);
        return ResponseEntity.noContent().build();
    }
}
