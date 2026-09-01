package com.ocp.at.service;

import com.ocp.at.dto.response.NotificationResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void createNotification(Utilisateur utilisateur, String titre, String message, String type, String lien);

    void sendNotificationToRole(String role, String titre, String message, String type, String lien);

    /**
     * Notifie uniquement les utilisateurs portant le rôle donné ET rattachés à
     * l'AT ciblée, selon la logique P/E du standard S-HSE-SEC-31 :
     * <ul>
     *   <li>rôles côté Propriétaire (CEEP, HCEP, HMEP) → zone propriétaire de l'AT ;</li>
     *   <li>rôles côté Exécutant (CEEE, HCEE, HMEE) → zone exécutante de l'AT ;</li>
     *   <li>CEEP/CEEE désignés de l'AT inclus même si leur service est sur une autre zone ;</li>
     *   <li>rôles hors P/E (ADMIN, RESPONSABLE_EXTERIEUR...) ou AT sans zone → diffusion
     *       globale par rôle (comportement antérieur conservé).</li>
     * </ul>
     * Un HCEP d'une AT ne reçoit donc jamais les notifications destinées au HCEE
     * de cette même AT (ni celles d'une autre AT).
     */
    void sendNotificationToRoleForAt(String role, AutorisationTravail at,
                                     String titre, String message, String type, String lien);

    Page<NotificationResponse> getUserNotifications(String utilisateurId, Pageable pageable);

    void markAsRead(String notificationId);

    void markAllAsRead(String utilisateurId);

    long countUnread(String utilisateurId);
}
