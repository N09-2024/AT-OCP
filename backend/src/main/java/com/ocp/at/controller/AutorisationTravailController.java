package com.ocp.at.controller;

import com.ocp.at.dto.request.AutoSaveRequest;
import com.ocp.at.dto.request.RefusRequest;
import com.ocp.at.dto.request.TransferLockRequest;
import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.dto.response.HistoriqueATResponse;
import com.ocp.at.dto.response.VisaResponse;
import com.ocp.at.service.AutorisationTravailService;
import com.ocp.at.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Autorisations de Travail", description = "API du moteur de gestion des Autorisations de Travail (Module 6)")
@SecurityRequirement(name = "bearerAuth")
public class AutorisationTravailController {

    private final AutorisationTravailService atService;
    private final PdfGeneratorService pdfService;

    // --- CRÉATION (CEEP = E sur 8.1, 8.3; CEEE = P sur 8.3) ---

    @PostMapping("/documents/{type}/{id}/creer-at")
    @Operation(summary = "Créer une Autorisation de Travail à partir d'un document (DI, OT, BT)")
    @PreAuthorize("hasAnyAuthority('CREATE_AT')")
    public ResponseEntity<AutorisationTravailResponse> createFromDocument(
            @PathVariable String type,
            @PathVariable String id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atService.createFromDocument(id, type));
    }

    @PostMapping("/documents/{type}/{id}/classifier")
    @Operation(summary = "Étape 0 — Classifier l'intervention en Niveau 1 ou Niveau 2 (HCEP)")
    @PreAuthorize("hasAuthority('CLASSIFY_INTERVENTION')")
    public ResponseEntity<AutorisationTravailResponse> classifierIntervention(
            @PathVariable String type,
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "2") Integer niveau) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atService.classifierIntervention(id, type, niveau));
    }

    @PostMapping("/autorisations-travail")
    @Operation(summary = "Créer une Autorisation de Travail sans document source obligatoire")
    @PreAuthorize("hasAnyAuthority('CREATE_AT')")
    public ResponseEntity<AutorisationTravailResponse> createDirect() {
        return ResponseEntity.status(HttpStatus.CREATED).body(atService.createDirect());
    }

    // --- CONSULTATION (tous les rôles avec READ_AT) ---

    @GetMapping("/autorisations-travail")
    @Operation(summary = "Lister toutes les Autorisations de Travail")
    @PreAuthorize("hasAuthority('READ_AT')")
    public ResponseEntity<Page<AutorisationTravailResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(atService.findAll(pageable));
    }

    @GetMapping("/autorisations-travail/{id}")
    @Operation(summary = "Consulter une Autorisation de Travail par ID")
    @PreAuthorize("hasAuthority('READ_AT')")
    public ResponseEntity<AutorisationTravailResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(atService.findById(id));
    }

    // --- ÉDITION & VERROUS (CEEP = E sur 8.3; CEEE = P sur 8.3) ---

    @PutMapping("/autorisations-travail/{id}/autosave")
    @Operation(summary = "Sauvegarder automatiquement le brouillon d'une AT")
    @PreAuthorize("hasAuthority('EDIT_AT')")
    public ResponseEntity<AutorisationTravailResponse> autoSave(
            @PathVariable String id,
            @Valid @RequestBody AutoSaveRequest request) {
        return ResponseEntity.ok(atService.autoSave(id, request));
    }

    @PutMapping("/autorisations-travail/{id}/prendre-verrou")
    @Operation(summary = "Prendre le verrou pour édition exclusive")
    @PreAuthorize("hasAuthority('EDIT_AT')")
    public ResponseEntity<Void> prendreVerrou(@PathVariable String id) {
        atService.prendreVerrou(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/autorisations-travail/{id}/liberer-verrou")
    @Operation(summary = "Libérer le verrou")
    @PreAuthorize("hasAuthority('EDIT_AT')")
    public ResponseEntity<Void> libererVerrou(@PathVariable String id) {
        atService.libererVerrou(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/autorisations-travail/{id}/transferer-verrou")
    @Operation(summary = "Transférer le verrou à un autre utilisateur du même rôle")
    @PreAuthorize("hasAuthority('TRANSFER_AT')")
    public ResponseEntity<Void> transfererVerrou(
            @PathVariable String id,
            @Valid @RequestBody TransferLockRequest request) {
        atService.transfererVerrou(id, request);
        return ResponseEntity.ok().build();
    }

    // --- WORKFLOW ---
    // Soumission: CEEP (E sur 8.3)
    @PostMapping({"/autorisations-travail/{id}/submit", "/at/{id}/submit"})
    @Operation(summary = "Soumettre l'AT pour validation")
    @PreAuthorize("hasAuthority('SUBMIT_AT')")
    public ResponseEntity<AutorisationTravailResponse> soumettre(@PathVariable String id) {
        return ResponseEntity.ok(atService.soumettreAT(id));
    }

    // Validation: HCEE (G sur 8.3)
    @PostMapping({"/autorisations-travail/{id}/validate", "/at/{id}/validate"})
    @Operation(summary = "Valider l'Autorisation de Travail")
    @PreAuthorize("hasAuthority('VALIDATE_AT')")
    public ResponseEntity<AutorisationTravailResponse> valider(@PathVariable String id) {
        return ResponseEntity.ok(atService.validerAT(id));
    }

    // Rejet: HCEE (G sur 8.3)
    @PostMapping({"/autorisations-travail/{id}/reject", "/at/{id}/reject"})
    @Operation(summary = "Refuser l'Autorisation de Travail avec motif")
    @PreAuthorize("hasAuthority('REJECT_AT')")
    public ResponseEntity<AutorisationTravailResponse> refuser(
            @PathVariable String id,
            @Valid @RequestBody RefusRequest request) {
        return ResponseEntity.ok(atService.refuserAT(id, request));
    }

    // Renouvellement: CEEP (E sur 8.4) + HCEE (G sur 8.4) + CEEE (P sur 8.4)
    @PostMapping({"/autorisations-travail/{id}/renew", "/at/{id}/renew"})
    @Operation(summary = "Renouveler une Autorisation de Travail (incrémente la version)")
    @PreAuthorize("hasAuthority('RENEW_AT')")
    public ResponseEntity<AutorisationTravailResponse> renouveler(@PathVariable String id) {
        return ResponseEntity.ok(atService.renouvelerAT(id));
    }

    // Clôture/Réception: CEEP (E sur 8.5 réception) + CEEE (P sur 8.5 réception)
    @PostMapping({"/autorisations-travail/{id}/close", "/at/{id}/close"})
    @Operation(summary = "Réceptionner les travaux et clôturer l'AT")
    @PreAuthorize("hasAuthority('CLOSE_AT')")
    public ResponseEntity<AutorisationTravailResponse> receptionnerTravaux(@PathVariable String id) {
        return ResponseEntity.ok(atService.cloturerAT(id));
    }

    // Démarrage intervention: CEEE (E sur Étape 4)
    @PostMapping({"/autorisations-travail/{id}/demarrer-intervention", "/at/{id}/demarrer-intervention"})
    @Operation(summary = "Étape 4 — Démarrer l'intervention (CEEE Exécutant)")
    @PreAuthorize("hasAuthority('START_INTERVENTION')")
    public ResponseEntity<AutorisationTravailResponse> demarrerIntervention(@PathVariable String id) {
        return ResponseEntity.ok(atService.demarrerIntervention(id));
    }

    // Déclaration fin des travaux: CEEE (E sur 8.5)
    @PostMapping({"/autorisations-travail/{id}/declarer-fin", "/at/{id}/declarer-fin"})
    @Operation(summary = "Étape 6 — Déclarer la fin des travaux (CEEE Exécutant)")
    @PreAuthorize("hasAuthority('DECLARE_FIN_TRAVAUX')")
    public ResponseEntity<AutorisationTravailResponse> declarerFinTravaux(@PathVariable String id) {
        return ResponseEntity.ok(atService.declarerFinTravaux(id));
    }

    // --- HISTORIQUE & VISAS & PDF ---

    @GetMapping("/autorisations-travail/{id}/historique")
    @Operation(summary = "Consulter l'historique complet d'une AT")
    @PreAuthorize("hasAuthority('READ_AT')")
    public ResponseEntity<List<HistoriqueATResponse>> getHistorique(@PathVariable String id) {
        return ResponseEntity.ok(atService.getHistorique(id));
    }

    @GetMapping("/autorisations-travail/{id}/visas")
    @Operation(summary = "Consulter les visas d'une AT")
    @PreAuthorize("hasAuthority('READ_AT')")
    public ResponseEntity<List<VisaResponse>> getVisas(@PathVariable String id) {
        return ResponseEntity.ok(atService.getVisas(id));
    }

    @GetMapping("/autorisations-travail/{id}/export-pdf")
    @Operation(summary = "Exporter l'AT au format PDF (uniquement si Validée ou Clôturée)")
    @PreAuthorize("hasAuthority('EXPORT_PDF')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String id) {
        AutorisationTravailResponse at = atService.findById(id);
        if (!"VALIDEE".equals(at.getStatut().name()) && !"CLOTUREE".equals(at.getStatut().name())) {
            throw new com.ocp.at.exception.BusinessException("L'export PDF n'est disponible que pour les AT Validées ou Clôturées.");
        }
        
        // Fetch full entity for the PDF generator
        // In a real scenario we could fetch it via repository directly or have the service handle the check
        com.ocp.at.entity.AutorisationTravail entity = new com.ocp.at.entity.AutorisationTravail();
        entity.setId(at.getId());
        entity.setNumero(at.getNumero());
        entity.setStatut(at.getStatut());
        entity.setVersion(at.getVersion());
        entity.setObjet(at.getObjet());
        
        byte[] pdfBytes = pdfService.generateATPdf(entity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", at.getNumero() + "_v" + at.getVersion() + ".pdf");
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
