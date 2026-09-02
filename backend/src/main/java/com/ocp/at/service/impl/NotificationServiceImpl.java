package com.ocp.at.service.impl;

import com.ocp.at.dto.response.NotificationResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Notification;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.NotificationMapper;
import com.ocp.at.repository.NotificationRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    private void broadcastNotification(String userId, NotificationResponse response) {
        if (messagingTemplate != null && userId != null) {
            try {
                messagingTemplate.convertAndSend("/topic/notifications/" + userId, response);
                long unread = countUnread(userId);
                messagingTemplate.convertAndSend("/topic/notifications/" + userId + "/count", Map.of("count", unread));
            } catch (Exception e) {
                log.debug("Erreur envoi websocket notification: {}", e.getMessage());
            }
        }
    }

    private void broadcastCount(String userId) {
        if (messagingTemplate != null && userId != null) {
            try {
                long unread = countUnread(userId);
                messagingTemplate.convertAndSend("/topic/notifications/" + userId + "/count", Map.of("count", unread));
            } catch (Exception e) {
                log.debug("Erreur envoi websocket count: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void createNotification(Utilisateur utilisateur, String titre, String message, String type, String lien) {
        if (utilisateur == null) {
            log.warn("createNotification: utilisateur null, notification ignorée (titre={})", titre);
            return;
        }
        Notification notification = Notification.builder()
                .titre(titre)
                .message(message)
                .utilisateur(utilisateur)
                .dateCreation(LocalDateTime.now())
                .lu(false)
                .type(type != null ? type : "INFO")
                .lien(lien)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification créée pour {} (ID: {}) : {}", utilisateur.getNom(), utilisateur.getId(), titre);
        broadcastNotification(utilisateur.getId(), mapper.toResponse(saved));
    }

    private List<String> getMatchingRoleNames(String roleName) {
        if (roleName == null) return List.of();
        String role = roleName.trim().toUpperCase();
        List<String> targetRoles = new java.util.ArrayList<>();
        targetRoles.add(role);

        if ("HCEP".equals(role) || "HCEE".equals(role) || "HC".equals(role)) {
            if (!targetRoles.contains("HCEP")) targetRoles.add("HCEP");
            if (!targetRoles.contains("HCEE")) targetRoles.add("HCEE");
            if (!targetRoles.contains("HC")) targetRoles.add("HC");
        } else if ("HMEP".equals(role) || "HMEE".equals(role) || "HM".equals(role)) {
            if (!targetRoles.contains("HMEP")) targetRoles.add("HMEP");
            if (!targetRoles.contains("HMEE")) targetRoles.add("HMEE");
            if (!targetRoles.contains("HM")) targetRoles.add("HM");
        } else if ("CEEP".equals(role) || "CEEE".equals(role) || "CE".equals(role)) {
            if (!targetRoles.contains("CEEP")) targetRoles.add("CEEP");
            if (!targetRoles.contains("CEEE")) targetRoles.add("CEEE");
            if (!targetRoles.contains("CE")) targetRoles.add("CE");
        }
        return targetRoles;
    }

    @Override
    @Transactional
    public void sendNotificationToRole(String roleName, String titre, String message, String type, String lien) {
        if (roleName == null || roleName.isBlank()) return;
        List<String> targetRoles = getMatchingRoleNames(roleName);

        List<Utilisateur> users = utilisateurRepository.findActiveByRoleNames(targetRoles);
        if (users.isEmpty()) {
            users = utilisateurRepository.findActiveByRoleFragment(roleName);
        }

        for (Utilisateur u : users) {
            createNotification(u, titre, message, type, lien);
        }
        log.info("sendNotificationToRole '{}': {} destinataire(s)", roleName, users.size());
    }

    /** Rôles du côté Propriétaire (P) du standard S-HSE-SEC-31 - zone propriétaire de l'AT. */
    private static final java.util.Set<String> ROLES_COTE_PROPRIETAIRE =
            java.util.Set.of("CEEP", "HCEP", "HMEP");

    /** Rôles du côté Exécutant (E) - zone exécutante de l'AT. */
    private static final java.util.Set<String> ROLES_COTE_EXECUTANT =
            java.util.Set.of("CEEE", "HCEE", "HMEE");

    @Override
    @Transactional
    public void sendNotificationToRoleForAt(String roleName, AutorisationTravail at,
                                            String titre, String message, String type, String lien) {
        if (roleName == null || roleName.isBlank() || at == null) return;
        String role = roleName.trim().toUpperCase();
        List<String> targetRoles = getMatchingRoleNames(role);

        // Résolution de la zone de l'AT pour ce rôle (logique P/E contextuelle).
        String zoneId = null;
        boolean rolePe = true;
        if (ROLES_COTE_PROPRIETAIRE.contains(role)) {
            zoneId = at.getZoneProprietaire() != null ? at.getZoneProprietaire().getId() : null;
        } else if (ROLES_COTE_EXECUTANT.contains(role)) {
            zoneId = at.getZoneExecutante() != null ? at.getZoneExecutante().getId() : null;
        } else {
            rolePe = false; // ADMIN, RESPONSABLE_EXTERIEUR... hors logique P/E
        }

        // Rôles hors P/E, ou AT sans zone : diffusion globale
        if (!rolePe || zoneId == null) {
            sendNotificationToRole(role, titre, message, type, lien);
            return;
        }

        java.util.LinkedHashSet<Utilisateur> destinataires = new java.util.LinkedHashSet<>();

        // 1. Recherche ciblée par zone
        List<Utilisateur> zoneUsers = utilisateurRepository.findActiveByRoleNamesAndZoneId(targetRoles, zoneId);
        destinataires.addAll(zoneUsers);

        // 2. Si aucun utilisateur n'est assigné à cette zone spécifique, fallback sur les utilisateurs portant ce rôle
        if (destinataires.isEmpty()) {
            destinataires.addAll(utilisateurRepository.findActiveByRoleNames(targetRoles));
        }

        // 3. L'acteur désigné de l'AT est toujours inclus s'il porte ce rôle
        if (targetRoles.contains("CEEP") && at.getProprietaireBrouillon() != null && hasAnyRole(at.getProprietaireBrouillon(), targetRoles)) {
            destinataires.add(at.getProprietaireBrouillon());
        }
        if (targetRoles.contains("CEEE") && at.getCeee() != null && hasAnyRole(at.getCeee(), targetRoles)) {
            destinataires.add(at.getCeee());
        }

        if (destinataires.isEmpty()) {
            log.info("sendNotificationToRoleForAt '{}': aucun destinataire trouvé pour l'AT {}", role, at.getNumero());
            return;
        }

        for (Utilisateur u : destinataires) {
            createNotification(u, titre, message, type, lien);
        }
        log.info("sendNotificationToRoleForAt '{}': {} destinataire(s) (AT {})", role,
                destinataires.size(), at.getNumero());
    }

    private boolean hasAnyRole(Utilisateur u, List<String> roleNoms) {
        if (u == null || u.getRoles() == null) return false;
        return u.getRoles().stream().anyMatch(r -> roleNoms.stream().anyMatch(rn -> rn.equalsIgnoreCase(r.getNom())));
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(String utilisateurId, Pageable pageable) {
        if (utilisateurId == null || utilisateurId.isBlank()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<Notification> notifs = notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId);
        if (notifs == null || notifs.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int start = (int) pageable.getOffset();
        if (start >= notifs.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, notifs.size());
        }
        int end = Math.min((start + pageable.getPageSize()), notifs.size());
        List<NotificationResponse> content = notifs.subList(start, end).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, notifs.size());
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec l'ID: " + notificationId));

        notif.setLu(true);
        notif.setDateLecture(LocalDateTime.now());
        Notification saved = notificationRepository.save(notif);
        if (saved.getUtilisateur() != null) {
            broadcastCount(saved.getUtilisateur().getId());
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(String utilisateurId) {
        if (utilisateurId == null || utilisateurId.isBlank()) return;
        int updated = notificationRepository.markAllReadByUtilisateurId(utilisateurId);
        log.info("markAllAsRead: {} notification(s) marquée(s) comme lues pour userId={}", updated, utilisateurId);
        broadcastCount(utilisateurId);
    }

    @Override
    public long countUnread(String utilisateurId) {
        if (utilisateurId == null || utilisateurId.isBlank()) return 0;
        return notificationRepository.countByUtilisateurIdAndLuFalse(utilisateurId);
    }
}

