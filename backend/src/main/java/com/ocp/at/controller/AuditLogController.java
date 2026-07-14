package com.ocp.at.controller;

import com.ocp.at.entity.AuditLog;
import com.ocp.at.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Consultation des logs d'audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Lister les logs d'audit (paginé)")
    @PreAuthorize("hasAuthority('VIEW_AUDIT')")
    public ResponseEntity<Page<AuditLog>> listerTous(
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return ResponseEntity.ok(auditLogRepository.findAll(pageable));
    }
}