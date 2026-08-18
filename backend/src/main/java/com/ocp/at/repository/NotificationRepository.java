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

    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(String utilisateurId);

    long countByUtilisateurIdAndLuFalse(String utilisateurId);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true, n.dateLecture = CURRENT_TIMESTAMP " +
           "WHERE n.utilisateur.id = :utilisateurId AND n.lu = false")
    int markAllReadByUtilisateurId(@Param("utilisateurId") String utilisateurId);
}
