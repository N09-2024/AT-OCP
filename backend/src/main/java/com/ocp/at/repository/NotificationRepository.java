package com.ocp.at.repository;

import com.ocp.at.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.utilisateur u " +
           "WHERE u.id = :utilisateurId OR u.email = :utilisateurId " +
           "ORDER BY n.dateCreation DESC")
    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(@Param("utilisateurId") String utilisateurId);

    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE (n.utilisateur.id = :utilisateurId OR n.utilisateur.email = :utilisateurId) AND n.lu = false")
    long countByUtilisateurIdAndLuFalse(@Param("utilisateurId") String utilisateurId);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true, n.dateLecture = CURRENT_TIMESTAMP " +
           "WHERE (n.utilisateur.id = :utilisateurId OR n.utilisateur.email = :utilisateurId) AND n.lu = false")
    int markAllReadByUtilisateurId(@Param("utilisateurId") String utilisateurId);
}
