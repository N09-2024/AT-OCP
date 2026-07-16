package com.ocp.at.repository;

import com.ocp.at.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByNom(String nom);
    boolean existsByNom(String nom);

    @Query("select distinct r from Role r left join fetch r.permissions")
    Page<Role> findAllWithPermissions(Pageable pageable);

    @Query("select distinct r from Role r left join fetch r.permissions where lower(r.nom) like lower(concat('%', ?1, '%'))")
    Page<Role> findByNomContainingIgnoreCaseWithPermissions(String nom, Pageable pageable);
}