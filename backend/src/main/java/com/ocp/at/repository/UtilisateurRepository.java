package com.ocp.at.repository;

import com.ocp.at.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByMatricule(String matricule);
    Boolean existsByEmail(String email);
    Boolean existsByMatricule(String matricule);

    @Query("SELECT DISTINCT u FROM Utilisateur u LEFT JOIN FETCH u.roles WHERE " +
           "(:search IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.matricule) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Utilisateur> findBySearchTerm(@Param("search") String search, Pageable pageable);

    List<Utilisateur> findByEnAttenteValidationTrue();
    Optional<Utilisateur> findByEmailAndIdNot(String email, String id);
    int countByRolesId(String roleId);
}
