package com.ocp.at.repository;

import com.ocp.at.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    @Query(value = "SELECT a FROM AuditLog a LEFT JOIN FETCH a.utilisateur",
           countQuery = "SELECT COUNT(a) FROM AuditLog a")
    Page<AuditLog> findAllWithUser(Pageable pageable);
}
