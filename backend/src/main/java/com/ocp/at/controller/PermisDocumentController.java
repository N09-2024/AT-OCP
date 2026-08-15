package com.ocp.at.controller;

import com.ocp.at.dto.response.PermisDocumentResponse;
import com.ocp.at.service.PermisDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpoints REST pour la gestion des documents de permis (validation IA - section E F-HSE-SEC-31-04).
 */
@RestController
@RequestMapping("/api/permis-documents")
@RequiredArgsConstructor
@Slf4j
public class PermisDocumentController {

    private final PermisDocumentService permisDocumentService;

    /**
     * Initialise les PermisDocument selon les permis cochés en section E.
     * A appeler après chaque modification de la section E.
     */
    @PostMapping("/at/{atId}/initialiser")
    public ResponseEntity<List<PermisDocumentResponse>> initialiser(@PathVariable String atId) {
        log.info("Initialisation permis documents pour AT {}", atId);
        return ResponseEntity.ok(permisDocumentService.initialiserPermisRequis(atId));
    }

    /**
     * Upload d un fichier de permis pour un type donné et déclenche l analyse IA.
     */
    @PostMapping(value = "/at/{atId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PermisDocumentResponse> upload(
            @PathVariable String atId,
            @RequestParam("typePermis") String typePermis,
            @RequestPart("file") MultipartFile file) {
        log.info("Upload permis {} pour AT {}, fichier: {}", typePermis, atId, file.getOriginalFilename());
        return ResponseEntity.ok(permisDocumentService.uploadPermisDocument(atId, typePermis, file));
    }

    /**
     * Liste les documents de permis d une AT avec leurs statuts IA.
     */
    @GetMapping("/at/{atId}")
    public ResponseEntity<List<PermisDocumentResponse>> liste(@PathVariable String atId) {
        return ResponseEntity.ok(permisDocumentService.getPermisDocuments(atId));
    }

    /**
     * Re-déclenche l analyse IA sur un document déjà uploadé (ex: après rejet).
     */
    @PostMapping("/{id}/relancer-analyse")
    public ResponseEntity<PermisDocumentResponse> relancer(@PathVariable String id) {
        log.info("Relance analyse IA pour PermisDocument {}", id);
        return ResponseEntity.ok(permisDocumentService.relancerAnalyse(id));
    }
}
