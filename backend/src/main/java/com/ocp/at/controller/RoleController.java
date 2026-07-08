package com.ocp.at.controller;

import com.ocp.at.dto.request.RoleRequest;
import com.ocp.at.dto.response.PermissionResponse;
import com.ocp.at.dto.response.RoleResponse;
import com.ocp.at.service.RoleService;
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

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Rôles", description = "Gestion des rôles et affectation des permissions")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Lister les rôles (paginé)")
    public ResponseEntity<Page<RoleResponse>> listerTous(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(roleService.listerTous(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trouver un rôle")
    public ResponseEntity<RoleResponse> trouverParId(@PathVariable String id) {
        return ResponseEntity.ok(roleService.trouverParId(id));
    }

    @PostMapping
    @Operation(summary = "Créer un rôle")
    public ResponseEntity<RoleResponse> creer(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.creer(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un rôle")
    public ResponseEntity<RoleResponse> modifier(@PathVariable String id, @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un rôle")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        roleService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "Consulter les permissions d'un rôle")
    public ResponseEntity<Set<PermissionResponse>> getPermissions(@PathVariable String id) {
        return ResponseEntity.ok(roleService.getPermissions(id));
    }

    @PostMapping("/{id}/permissions")
    @Operation(summary = "Affecter une permission à un rôle")
    public ResponseEntity<RoleResponse> affecterPermission(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(roleService.affecterPermission(id, body.get("permissionId")));
    }

    @DeleteMapping("/{id}/permissions/{permId}")
    @Operation(summary = "Retirer une permission d'un rôle")
    public ResponseEntity<RoleResponse> retirerPermission(@PathVariable String id, @PathVariable String permId) {
        return ResponseEntity.ok(roleService.retirerPermission(id, permId));
    }
}
