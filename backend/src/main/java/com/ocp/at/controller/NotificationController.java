package com.ocp.at.controller;

import com.ocp.at.dto.response.NotificationResponse;
import com.ocp.at.exception.UnauthorizedException;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestion des notifications utilisateurs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lister les notifications de l'utilisateur connecté")
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(Pageable pageable) {
        String userId = SecurityUtils.getCurrentUtilisateurId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié"));
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    @GetMapping("/count-unread")
    @Operation(summary = "Nombre de notifications non lues")
    public ResponseEntity<Map<String, Long>> countUnread() {
        String userId = SecurityUtils.getCurrentUtilisateurId()
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<Void> markAllAsRead() {
        String userId = SecurityUtils.getCurrentUtilisateurId()
                .orElseThrow(() -> new UnauthorizedException("Utilisateur non authentifié"));
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
