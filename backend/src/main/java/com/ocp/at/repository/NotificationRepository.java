package com.ocp.at.repository;

import com.ocp.at.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(String utilisateurId);
}
