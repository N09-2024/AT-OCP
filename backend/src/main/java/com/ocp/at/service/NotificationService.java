package com.ocp.at.service;

import com.ocp.at.dto.response.NotificationResponse;
import com.ocp.at.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void createNotification(Utilisateur utilisateur, String titre, String message, String type, String lien);

    void sendNotificationToRole(String role, String titre, String message, String type, String lien);

    Page<NotificationResponse> getUserNotifications(String utilisateurId, Pageable pageable);

    void markAsRead(String notificationId);

    void markAllAsRead(String utilisateurId);

    long countUnread(String utilisateurId);
}
