package com.ocp.at.controller;

import com.ocp.at.dto.request.UtilisateurRequest;
import com.ocp.at.dto.request.UtilisateurUpdateRequest;
import com.ocp.at.dto.response.RoleResponse;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs")
@SecurityRequirement(name = "bearerAuth")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @Operation(summary = "Lister les utilisateurs", description = "Retourne la liste paginée des utilisateurs avec filtres optionnels")
    public ResponseEntity<Page<UtilisateurResponse>> listerTous(
            @Parameter(description = "Terme de recherche (nom, prénom, email, matricule)") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return ResponseEntity.ok(utilisateurService.listerTous(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Trouver un utilisateur", description = "Retourne les détails d'un utilisateur par son ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
        @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<UtilisateurResponse> trouverParId(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.trouverParId(id));
    }

    @PostMapping
    @Operation(summary = "Créer un utilisateur", description = "Crée un nouvel utilisateur avec validation de la politique de mot de passe")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou email/matricule déjà existant")
    })
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur", description = "Met à jour les informations d'un utilisateur (hors mot de passe)")
    public ResponseEntity<UtilisateurResponse> modifier(@PathVariable String id, @Valid @RequestBody UtilisateurUpdateRequest request) {
        return ResponseEntity.ok(utilisateurService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    @ApiResponse(responseCode = "204", description = "Utilisateur supprimé")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activer un compte utilisateur")
    public ResponseEntity<UtilisateurResponse> activer(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.activer(id));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Désactiver un compte utilisateur")
    public ResponseEntity<UtilisateurResponse> desactiver(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.desactiver(id));
    }

    @PatchMapping("/{id}/unlock")
    @Operation(summary = "Déverrouiller un compte", description = "Déverrouille un compte après trop de tentatives de connexion échouées (Admin uniquement)")
    public ResponseEntity<UtilisateurResponse> deverrouiller(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.deverrouiller(id));
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "Consulter les rôles d'un utilisateur")
    public ResponseEntity<Set<RoleResponse>> getRoles(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.getRoles(id));
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Affecter un rôle à un utilisateur")
    public ResponseEntity<UtilisateurResponse> affecterRole(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(utilisateurService.affecterRole(id, body.get("roleId")));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Retirer un rôle d'un utilisateur")
    public ResponseEntity<UtilisateurResponse> retirerRole(@PathVariable String id, @PathVariable String roleId) {
        return ResponseEntity.ok(utilisateurService.retirerRole(id, roleId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Lister les inscriptions en attente", description = "Retourne la liste des utilisateurs en attente de validation par l'admin")
    public ResponseEntity<List<UtilisateurResponse>> listerEnAttente() {
        return ResponseEntity.ok(utilisateurService.listerEnAttente());
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approuver une inscription", description = "Valide l'inscription d'un utilisateur et active son compte")
    public ResponseEntity<UtilisateurResponse> approuverInscription(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.approuverInscription(id));
    }

    @DeleteMapping("/{id}/reject")
    @Operation(summary = "Rejeter une inscription", description = "Rejette et supprime une inscription en attente")
    public ResponseEntity<Void> rejeterInscription(@PathVariable String id) {
        utilisateurService.rejeterInscription(id);
        return ResponseEntity.noContent().build();
    }
}
