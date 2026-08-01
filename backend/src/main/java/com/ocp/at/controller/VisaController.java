package com.ocp.at.controller;

import com.ocp.at.dto.request.VisaRequest;
import com.ocp.at.dto.response.VisaResponse;
import com.ocp.at.service.VisaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/visa")
@RequiredArgsConstructor
@Tag(name = "Visas", description = "Gestion des visas d'Autorisation de Travail")
public class VisaController {

    private final VisaService visaService;

    @PostMapping
    @PreAuthorize("hasAuthority('SIGN_AT') or hasAuthority('VALIDATE_AT')")
    @Operation(summary = "Créer un nouveau visa (sans signature)")
    public ResponseEntity<VisaResponse> createVisa(@Valid @RequestBody VisaRequest request) {
        return new ResponseEntity<>(visaService.createVisa(request), HttpStatus.CREATED);
    }

    @PostMapping(value = "/{id}/sign", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SIGN_AT') or hasAuthority('VALIDATE_AT')")
    @Operation(summary = "Signer un visa existant avec une signature PNG manuscrite")
    public ResponseEntity<VisaResponse> signVisa(
            @PathVariable String id,
            @RequestParam("signature") MultipartFile signature,
            @RequestParam(value = "commentaire", required = false) String commentaire) {
        return ResponseEntity.ok(visaService.signVisa(id, signature, commentaire));
    }

    @GetMapping("/at/{atId}")
    @PreAuthorize("hasAuthority('READ_AT')")
    @Operation(summary = "Obtenir tous les visas d'une Autorisation de Travail")
    public ResponseEntity<List<VisaResponse>> getVisasByAtId(@PathVariable String atId) {
        return ResponseEntity.ok(visaService.getVisasByAtId(atId));
    }

    @GetMapping("/{id}/signature")
    @PreAuthorize("hasAuthority('READ_AT')")
    @Operation(summary = "Télécharger l'image de la signature PNG")
    public ResponseEntity<Resource> downloadSignature(@PathVariable String id) {
        Resource file = visaService.downloadSignature(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(file);
    }
}