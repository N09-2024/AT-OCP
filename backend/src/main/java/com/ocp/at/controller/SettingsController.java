package com.ocp.at.controller;

import com.ocp.at.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Paramètres", description = "Configuration générale du système")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "Obtenir les paramètres système")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    @Operation(summary = "Mettre à jour les paramètres système")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> settings) {
        return ResponseEntity.ok(settingsService.updateSettings(settings));
    }
}