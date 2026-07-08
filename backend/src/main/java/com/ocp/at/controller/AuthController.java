package com.ocp.at.controller;

import com.ocp.at.dto.request.LoginRequest;
import com.ocp.at.dto.request.TokenRefreshRequest;
import com.ocp.at.dto.response.JwtResponse;
import com.ocp.at.dto.response.TokenRefreshResponse;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "API d'authentification et gestion des sessions JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie l'utilisateur et retourne un JWT + Refresh Token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connexion réussie"),
        @ApiResponse(responseCode = "401", description = "Identifiants invalides ou compte verrouillé"),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides")
    })
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Renouveler le token", description = "Génère un nouveau JWT à partir d'un Refresh Token valide")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token renouvelé"),
        @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expiré")
    })
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Révoque le Refresh Token et invalide la session")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Déconnexion réussie"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<Map<String, String>> logout(@RequestBody TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    @GetMapping("/me")
    @Operation(summary = "Utilisateur connecté", description = "Retourne les informations de l'utilisateur actuellement authentifié")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Informations récupérées"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<UtilisateurResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }
}
