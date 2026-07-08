package com.ocp.at.controller;

import com.ocp.at.dto.request.PermissionRequest;
import com.ocp.at.dto.response.PermissionResponse;
import com.ocp.at.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Gestion des permissions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "Lister les permissions (paginé)")
    public ResponseEntity<Page<PermissionResponse>> listerTous(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(permissionService.listerTous(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trouver une permission")
    public ResponseEntity<PermissionResponse> trouverParId(@PathVariable String id) {
        return ResponseEntity.ok(permissionService.trouverParId(id));
    }

    @PostMapping
    @Operation(summary = "Créer une permission")
    public ResponseEntity<PermissionResponse> creer(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.creer(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une permission")
    public ResponseEntity<PermissionResponse> modifier(@PathVariable String id, @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une permission")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        permissionService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
