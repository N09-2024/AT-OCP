package com.ocp.at.repository;

import com.ocp.at.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    Optional<Permission> findByNom(String nom);
    boolean existsByNom(String nom);
    Page<Permission> findByNomContainingIgnoreCase(String nom, Pageable pageable);
    java.util.List<Permission> findByNomIn(java.util.Collection<String> noms);
}
