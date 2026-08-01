package com.ocp.at.controller;

import com.ocp.at.dto.request.ArchiveSearchRequest;
import com.ocp.at.dto.response.ArchiveResponse;
import com.ocp.at.dto.response.ArchiveSearchResponse;
import com.ocp.at.dto.response.PdfExportResponse;
import com.ocp.at.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * Contrôleur REST pour la gestion des archives d'Autorisations de Travail.
 * Module 10 : Export PDF, Archivage, Audit Final.
 */
@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Archives", description = "API pour la gestion des archives d'Autorisations de Travail - Module 10")
@SecurityRequirement(name = "Bearer Authentication")
public class ArchiveController {

    private final ArchiveService archiveService;

    // =========================================================================
    // EXPORT PDF (sans archivage)
    // =========================================================================

    @Operation(
            summary = "Exporter une AT clôturée en PDF",
            description = "Génère un PDF complet du dossier AT sans l'archiver officiellement. Nécessite le statut CLOTUREE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès",
                    content = @Content(schema = @Schema(implementation = PdfExportResponse.class))),
            @ApiResponse(responseCode = "400", description = "L'AT n'est pas clôturée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission EXPORT_PDF requise"),
            @ApiResponse(responseCode = "404", description = "AT non trouvée")
    })
    @PostMapping("/export/{atId}")
    @PreAuthorize("hasAuthority('EXPORT_PDF')")
    public ResponseEntity<PdfExportResponse> exportAT(@PathVariable String atId) {
        log.info("POST /api/archives/export/{}", atId);
        PdfExportResponse response = archiveService.exportAT(atId);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // ARCHIVAGE OFFICIEL
    // =========================================================================

    @Operation(
            summary = "Archiver officielle une AT clôturée",
            description = "Génère le PDF complet, calcule le hash SHA-256, génère le QR Code et archive l'AT de façon permanente. Chaque appel crée une nouvelle version."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AT archivée avec succès",
                    content = @Content(schema = @Schema(implementation = ArchiveResponse.class))),
            @ApiResponse(responseCode = "400", description = "L'AT n'est pas clôturée ou document doublon détecté"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission EXPORT_PDF requise"),
            @ApiResponse(responseCode = "404", description = "AT non trouvée")
    })
    @PostMapping("/archive/{atId}")
    @PreAuthorize("hasAuthority('ARCHIVE_AT')")
    public ResponseEntity<ArchiveResponse> archiverAT(@PathVariable String atId) {
        log.info("POST /api/archives/archive/{}", atId);
        ArchiveResponse response = archiveService.archiverAT(atId);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // CONSULTATION
    // =========================================================================

    @Operation(
            summary = "Obtenir toutes les archives (paginé)",
            description = "Retourne la liste paginée de toutes les archives non supprimées."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste retournée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission VIEW_ARCHIVE requise")
    })
    @GetMapping("")
    @PreAuthorize("hasAuthority('VIEW_ARCHIVE')")
    public ResponseEntity<Page<ArchiveResponse>> getAllArchives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArchiveResponse> archives = archiveService.getAll(PageRequest.of(page, size));
        return ResponseEntity.ok(archives);
    }

    @Operation(
            summary = "Obtenir une archive par son ID",
            description = "Retourne les détails complets d'une archive spécifique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archive trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission VIEW_ARCHIVE requise"),
            @ApiResponse(responseCode = "404", description = "Archive non trouvée")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ARCHIVE')")
    public ResponseEntity<ArchiveResponse> getArchiveById(@PathVariable String id) {
        return ResponseEntity.ok(archiveService.getById(id));
    }

    @Operation(
            summary = "Obtenir la dernière archive pour une AT",
            description = "Retourne l'archive la plus récente associée à une Autorisation de Travail."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archive trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission VIEW_ARCHIVE requise"),
            @ApiResponse(responseCode = "404", description = "Aucune archive pour cette AT")
    })
    @GetMapping("/at/{atId}")
    @PreAuthorize("hasAuthority('VIEW_ARCHIVE')")
    public ResponseEntity<ArchiveResponse> getArchiveByAtId(@PathVariable String atId) {
        return ResponseEntity.ok(archiveService.getByAutorisationTravailId(atId));
    }

    @Operation(
            summary = "Obtenir toutes les versions d'une AT",
            description = "Retourne l'historique de toutes les versions archivées d'une AT."
    )
    @ApiResponse(responseCode = "200", description = "Liste des versions retournée")
    @GetMapping("/{atId}/versions")
    @PreAuthorize("hasAuthority('VIEW_ARCHIVE')")
    public ResponseEntity<Iterable<ArchiveResponse>> getAllVersions(@PathVariable String atId) {
        // Délégué à une implémentation future ; retourne la version la plus récente pour l'instant
        try {
            return ResponseEntity.ok(Collections.singletonList(archiveService.getByAutorisationTravailId(atId)));
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    // =========================================================================
    // TÉLÉCHARGEMENT
    // =========================================================================

    @Operation(
            summary = "Télécharger le PDF d'une archive",
            description = "Retourne le fichier PDF officiel de l'archive en tant que téléchargement binaire."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF téléchargé avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission DOWNLOAD_ARCHIVE requise"),
            @ApiResponse(responseCode = "404", description = "Archive non trouvée")
    })
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('VIEW_ARCHIVE')")
    public ResponseEntity<ByteArrayResource> downloadArchive(@PathVariable String id) {
        byte[] pdfBytes = archiveService.downloadArchive(id);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive_" + id + ".pdf\"")
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    // =========================================================================
    // VÉRIFICATION D'INTÉGRITÉ
    // =========================================================================

    @Operation(
            summary = "Vérifier l'intégrité d'une archive",
            description = "Calcule le hash SHA-256 du fichier PDF stocké et le compare avec le hash enregistré en base. Permet de détecter toute altération."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultat de la vérification : true = intègre, false = altéré"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission VERIFY_ARCHIVE requise"),
            @ApiResponse(responseCode = "404", description = "Archive non trouvée")
    })
    @GetMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('VIEW_ARCHIVE')")
    public ResponseEntity<Boolean> verifyArchive(@PathVariable String id) {
        boolean valid = archiveService.verifyArchive(id);
        return ResponseEntity.ok(valid);
    }

    // =========================================================================
    // RECHERCHE
    // =========================================================================

    @Operation(
            summary = "Rechercher des archives",
            description = "Recherche des archives par numéro AT, numéro d'archive, dates ou autres critères."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultats de la recherche retournés"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Permission SEARCH_ARCHIVE requise")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('VIEW_ARCHIVE')")
    public ResponseEntity<Page<ArchiveSearchResponse>> searchArchives(
            @RequestParam(required = false) String numeroAT,
            @RequestParam(required = false) String numeroArchive,
            @RequestParam(required = false) Integer version,
            @RequestParam(required = false) String dateArchivageDebut,
            @RequestParam(required = false) String dateArchivageFin,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String archiveStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ArchiveSearchRequest searchRequest = ArchiveSearchRequest.builder()
                .numeroAT(numeroAT)
                .numeroArchive(numeroArchive)
                .version(version)
                .dateArchivageDebut(dateArchivageDebut != null ?
                        java.time.LocalDateTime.parse(dateArchivageDebut) : null)
                .dateArchivageFin(dateArchivageFin != null ?
                        java.time.LocalDateTime.parse(dateArchivageFin) : null)
                .createdBy(createdBy)
                .archiveStatus(archiveStatus)
                .build();

        Page<ArchiveSearchResponse> results = archiveService.search(searchRequest, PageRequest.of(page, size));
        return ResponseEntity.ok(results);
    }
}
