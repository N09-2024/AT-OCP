package com.ocp.at.controller;

import com.ocp.at.entity.AuditLog;
import com.ocp.at.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Consultation des logs d'audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Lister les logs d'audit (paginé)")
    @PreAuthorize("hasAuthority('VIEW_AUDIT') or hasAuthority('ADMIN') or hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<Page<AuditLogResponse>> listerTous(
            @PageableDefault(size = 50) @SortDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAllWithUser(pageable);
        Page<AuditLogResponse> responsePage = logs.map(this::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    private AuditLogResponse toResponse(AuditLog entity) {
        AuditLogResponse.UtilisateurSummary userSummary = null;
        if (entity.getUtilisateur() != null) {
            userSummary = AuditLogResponse.UtilisateurSummary.builder()
                    .id(entity.getUtilisateur().getId())
                    .nom(entity.getUtilisateur().getNom())
                    .prenom(entity.getUtilisateur().getPrenom())
                    .email(entity.getUtilisateur().getEmail())
                    .matricule(entity.getUtilisateur().getMatricule())
                    .build();
        }
        return AuditLogResponse.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .action(entity.getAction())
                .resultat(entity.getResultat() != null ? entity.getResultat().name() : "INFO")
                .adresseIP(entity.getAdresseIP())
                .navigateur(entity.getNavigateur())
                .systemeExploitation(entity.getSystemeExploitation())
                .utilisateur(userSummary)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditLogResponse {
        private String id;
        private LocalDateTime date;
        private String action;
        private String resultat;
        private String adresseIP;
        private String navigateur;
        private String systemeExploitation;
        private UtilisateurSummary utilisateur;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class UtilisateurSummary {
            private String id;
            private String nom;
            private String prenom;
            private String email;
            private String matricule;
        }
    }
}