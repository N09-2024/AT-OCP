package com.ocp.at.controller;

import com.ocp.at.dto.response.DashboardDataResponse;
import com.ocp.at.security.UserDetailsImpl;
import com.ocp.at.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "API pour les statistiques du tableau de bord")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques", description = "Retourne toutes les KPI et données pour le tableau de bord")
    public ResponseEntity<DashboardDataResponse> getDashboardStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(dashboardService.getDashboardStats(userDetails.getUsername()));
    }
}
