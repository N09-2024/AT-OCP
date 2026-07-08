package com.ocp.at.repository;

import com.ocp.at.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByNom(String nom);
    boolean existsByNom(String nom);
    Page<Role> findByNomContainingIgnoreCase(String nom, Pageable pageable);
}
