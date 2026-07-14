package com.ocp.at.controller;

import com.ocp.at.config.SecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @GetMapping
    @Operation(summary = "Obtenir les paramètres système")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(Map.of(
            "maintenanceMode", false,
            "sessionTimeoutMinutes", 60,
            "maxLoginAttempts", maxLoginAttempts,
            "inscriptionOuverte", false,
            "emailNotifications", true,
            "retentionDays", 365
        ));
    }

    @PutMapping
    @Operation(summary = "Mettre à jour les paramètres système")
    @PreAuthorize("hasAuthority('MANAGE_ROLES')")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> settings) {
        // Dans une implémentation réelle, persister en base de données
        return ResponseEntity.ok(settings);
    }
}