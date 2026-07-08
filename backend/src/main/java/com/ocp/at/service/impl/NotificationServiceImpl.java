package com.ocp.at.service.impl;

import com.ocp.at.dto.response.NotificationResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        log.info("Notification créée pour {} : {}", utilisateur.getNom(), titre);
    }

    @Override
    @Transactional
    public void sendNotificationToRole(String roleName, String titre, String message, String type, String lien) {
        // Find users having a role whose name contains roleName. 
        // This is a simplified version. Ideally we should have a query in UtilisateurRepository.
        List<Utilisateur> users = utilisateurRepository.findAll().stream()
                .filter(u -> u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getNom().contains(roleName)))
                .toList();

        for (Utilisateur u : users) {
            createNotification(u, titre, message, type, lien);
        }
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(String utilisateurId, Pageable pageable) {
        List<Notification> notifs = notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId);
        // Simple manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), notifs.size());
        List<NotificationResponse> content = notifs.subList(start, end).stream()
                .map(mapper::toResponse)
                .toList();
        
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
}
