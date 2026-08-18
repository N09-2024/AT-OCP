package com.ocp.at.service.impl;

import com.ocp.at.dto.response.NotificationResponse;
import com.ocp.at.entity.Notification;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.NotificationMapper;
import com.ocp.at.repository.NotificationRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.security.RoleUtils;
import com.ocp.at.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationMapper mapper;

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

        notificationRepository.save(notification);
        log.info("Notification créée pour {} (ID: {}) : {}", utilisateur.getNom(), utilisateur.getId(), titre);
    }

    @Override
    @Transactional
    public void sendNotificationToRole(String roleName, String titre, String message, String type, String lien) {
        if (roleName == null || roleName.isBlank()) return;

        List<Utilisateur> allUsers = utilisateurRepository.findAll();
        List<Utilisateur> users = allUsers.stream()
                .filter(u -> u != null && !u.isCompteVerrouille() && u.isActif() && RoleUtils.userHasRolePattern(u, roleName))
                .collect(Collectors.toList());

        for (Utilisateur u : users) {
            createNotification(u, titre, message, type, lien);
        }
        log.info("sendNotificationToRole '{}': {} destinataire(s)", roleName, users.size());
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
        notificationRepository.save(notif);
    }

    @Override
    @Transactional
    public void markAllAsRead(String utilisateurId) {
        if (utilisateurId == null || utilisateurId.isBlank()) return;
        int updated = notificationRepository.markAllReadByUtilisateurId(utilisateurId);
        log.info("markAllAsRead: {} notification(s) marquée(s) comme lues pour userId={}", updated, utilisateurId);
    }

    @Override
    public long countUnread(String utilisateurId) {
        if (utilisateurId == null || utilisateurId.isBlank()) return 0;
        return notificationRepository.countByUtilisateurIdAndLuFalse(utilisateurId);
    }
}
