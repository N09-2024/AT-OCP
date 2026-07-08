package com.ocp.at.controller;

import com.ocp.at.dto.request.PhotoReceptionRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.service.ReceptionTravauxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptions")
@RequiredArgsConstructor
@Tag(name = "Module 9 – Réception des Travaux", description = "Gestion de la réception des travaux et clôture des AT")
public class ReceptionTravauxController {

    private final ReceptionTravauxService receptionService;

    // =====================================================================
    // GET ALL
    // =====================================================================

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_RECEPTION')")
    @Operation(summary = "Lister toutes les réceptions", description = "Retourne la liste paginée de toutes les réceptions des travaux")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste retournée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    public ResponseEntity<Page<ReceptionTravauxResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(receptionService.getAll(pageable));
    }

    // =====================================================================
    // GET BY ID
    // =====================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_RECEPTION')")
    @Operation(summary = "Récupérer une réception par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réception trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Réception introuvable")
    })
    public ResponseEntity<ReceptionTravauxResponse> getById(
            @Parameter(description = "Identifiant de la réception", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(receptionService.getById(id));
    }

    // =====================================================================
    // GET BY AT
    // =====================================================================

    @GetMapping("/at/{atId}")
    @PreAuthorize("hasAuthority('VIEW_RECEPTION')")
    @Operation(summary = "Récupérer la réception d'une AT", description = "Retourne la réception associée à l'Autorisation de Travail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réception trouvée"),
            @ApiResponse(responseCode = "404", description = "Aucune réception pour cette AT")
    })
    public ResponseEntity<ReceptionTravauxResponse> getByAt(
            @Parameter(description = "Identifiant de l'AT", required = true)
            @PathVariable String atId) {
        return ResponseEntity.ok(receptionService.getByAutorisationTravailId(atId));
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_RECEPTION')")
    @Operation(
            summary = "Créer une réception des travaux",
            description = "L'AT doit être en statut VALIDÉE, tous les visas validés, tous les permis conformes. Une seule réception par AT est autorisée."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réception créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou règle métier violée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "AT introuvable"),
            @ApiResponse(responseCode = "409", description = "Une réception existe déjà pour cette AT")
    })
    public ResponseEntity<ReceptionTravauxResponse> create(
            @Valid @RequestBody ReceptionTravauxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receptionService.create(request));
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDIT_RECEPTION')")
    @Operation(summary = "Mettre à jour une réception", description = "Impossible de modifier une réception d'une AT clôturée")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réception mise à jour"),
            @ApiResponse(responseCode = "400", description = "AT clôturée ou données invalides"),
            @ApiResponse(responseCode = "404", description = "Réception introuvable")
    })
    public ResponseEntity<ReceptionTravauxResponse> update(
            @PathVariable String id,
            @Valid @RequestBody ReceptionTravauxRequest request) {
        return ResponseEntity.ok(receptionService.update(id, request));
    }

    // =====================================================================
    // DELETE
    // =====================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_RECEPTION')")
    @Operation(summary = "Supprimer une réception", description = "Impossible de supprimer une réception d'une AT clôturée")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Réception supprimée"),
            @ApiResponse(responseCode = "400", description = "AT clôturée"),
            @ApiResponse(responseCode = "404", description = "Réception introuvable")
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        receptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // SIGNATURE
    // =====================================================================

    @PutMapping("/{id}/signer")
    @PreAuthorize("hasAuthority('SIGN_RECEPTION')")
    @Operation(
            summary = "Signer la réception",
            description = "Ajoute la signature manuscrite du responsable. Réutilise le système de signature du Module 8."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réception signée avec succès"),
            @ApiResponse(responseCode = "400", description = "AT clôturée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission SIGN_RECEPTION requise"),
            @ApiResponse(responseCode = "404", description = "Réception introuvable")
    })
    public ResponseEntity<ReceptionTravauxResponse> signer(
            @PathVariable String id,
            @RequestBody String signaturePath) {
        return ResponseEntity.ok(receptionService.signer(id, signaturePath));
    }

    // =====================================================================
    // CLOTURE AT
    // =====================================================================

    @PutMapping("/{id}/cloturer")
    @PreAuthorize("hasAuthority('CLOSE_AT')")
    @Operation(
            summary = "Clôturer l'AT",
            description = """
                    Clôture l'AT associée à la réception. Conditions requises :
                    - travaux conformes
                    - zone nettoyée
                    - consignation retirée
                    - équipement remis en service
                    - essais réalisés
                    - signature présente
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AT clôturée avec succès"),
            @ApiResponse(responseCode = "400", description = "Conditions de clôture non remplies"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission CLOSE_AT requise"),
            @ApiResponse(responseCode = "404", description = "Réception introuvable")
    })
    public ResponseEntity<ReceptionTravauxResponse> cloturer(@PathVariable String id) {
        return ResponseEntity.ok(receptionService.cloturerAT(id));
    }

    // =====================================================================
    // PHOTOS
    // =====================================================================

    @GetMapping("/{id}/photos")
    @PreAuthorize("hasAuthority('VIEW_RECEPTION')")
    @Operation(summary = "Lister les photos d'une réception")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des photos retournée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Réception introuvable")
    })
    public ResponseEntity<List<PhotoReceptionResponse>> getPhotos(@PathVariable String id) {
        return ResponseEntity.ok(receptionService.getPhotos(id));
    }

    @PostMapping("/{id}/photos")
    @PreAuthorize("hasAuthority('EDIT_RECEPTION')")
    @Operation(summary = "Ajouter une photo à une réception")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Photo ajoutée"),
            @ApiResponse(responseCode = "400", description = "AT clôturée ou données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Réception introuvable")
    })
    public ResponseEntity<PhotoReceptionResponse> ajouterPhoto(
            @PathVariable String id,
            @Valid @RequestBody PhotoReceptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receptionService.ajouterPhoto(id, request));
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @PreAuthorize("hasAuthority('EDIT_RECEPTION')")
    @Operation(summary = "Supprimer une photo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Photo supprimée"),
            @ApiResponse(responseCode = "400", description = "AT clôturée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Photo ou réception introuvable")
    })
    public ResponseEntity<Void> supprimerPhoto(
            @PathVariable String id,
            @PathVariable String photoId) {
        receptionService.supprimerPhoto(id, photoId);
        return ResponseEntity.noContent().build();
    }
}
