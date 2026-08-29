package com.ocp.at.controller;

import com.ocp.at.dto.response.VerificationQrResponse;
import com.ocp.at.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
@Tag(name = "Vérification QR", description = "Vérification de l'intégrité et de l'authenticité d'un dossier archivé via son QR Code")
public class VerificationController {

    private final ArchiveService archiveService;

    @GetMapping("/{numero}")
    @Operation(summary = "Vérifier l'authenticité d'un dossier archivé via référence AT ou archive")
    public ResponseEntity<VerificationQrResponse> verifyDossier(@PathVariable String numero) {
        return ResponseEntity.ok(archiveService.verifyQrCode(numero));
    }
}
