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
    @Operation(summary = "Créer une visite préalable liée à un document (DI, OT ou BT)",
               description = "§8.2 Standard S-HSE-SEC-31 : le CEEP exécute (E) la visite chantier.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VisitePrealableResponse> create(@Valid @RequestBody VisitePrealableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une visite préalable (impossible si finalisée)",
               description = "§8.2 CEEP (E) et CEEE (P) peuvent modifier la visite. HCEE/HMEP valident via /finaliser.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VisitePrealableResponse> update(
            @PathVariable String id,
            @Valid @RequestBody VisitePrealableRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une visite préalable (impossible si une analyse est liée)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/finaliser")
    @Operation(summary = "Finaliser une visite (GPS + commentaire + photo requis)",
               description = "§8.2 Garants : HCEE (G côté E) et HMEP (G côté P) valident la visite.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VisitePrealableResponse> finaliser(@PathVariable String id) {
        return ResponseEntity.ok(service.finaliser(id));
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter une photo à la visite (upload multipart)",
               description = "§8.2 CEEP et CEEE peuvent ajouter des photos durant la visite.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotoResponse> addPhoto(
            @PathVariable String id,
            @Parameter(description = "Fichier image") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Légende optionnelle") @RequestParam(value = "legende", required = false) String legende) {
        if (file == null || file.isEmpty()) {
            throw new com.ocp.at.exception.BusinessException("Le fichier photo est obligatoire");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addPhoto(id, file, legende));
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @Operation(summary = "Supprimer une photo de la visite (impossible si finalisée)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable String id,
            @PathVariable String photoId) {
        service.deletePhoto(id, photoId);
        return ResponseEntity.noContent().build();
    }
}
