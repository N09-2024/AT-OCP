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
    @Query("SELECT DISTINCT u FROM Utilisateur u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
    Optional<Utilisateur> findByEmail(@Param("email") String email);
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

    /** Chefs d'équipe (CE/CEEP/CEEE) rattachés à un service - pour affichage CEEE dans le formulaire AT. */
    @Query("SELECT DISTINCT u FROM Utilisateur u JOIN u.roles r JOIN u.service s " +
           "WHERE s.id = :serviceId AND UPPER(r.nom) IN ('CE','CEEP','CEEE') " +
           "AND (u.compteVerrouille = false OR u.compteVerrouille IS NULL)")
    List<Utilisateur> findChefsEquipeByServiceId(@Param("serviceId") String serviceId);

    /** Chefs d'équipe dont le service est sur une zone donnée (zone exécutante de l'AT). */
    @Query("SELECT DISTINCT u FROM Utilisateur u JOIN u.roles r JOIN u.service s JOIN s.zone z " +
           "WHERE z.id = :zoneId AND UPPER(r.nom) IN ('CE','CEEP','CEEE') " +
           "AND (u.compteVerrouille = false OR u.compteVerrouille IS NULL)")
    List<Utilisateur> findChefsEquipeByZoneId(@Param("zoneId") String zoneId);

    /**
     * Trouve tous les utilisateurs actifs ayant un rôle dont le nom contient le fragment donné.
     * Utilisé par NotificationService.sendNotificationToRole() pour éviter un findAll() en mémoire.
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u JOIN u.roles r " +
           "WHERE UPPER(r.nom) LIKE UPPER(CONCAT('%', :roleFragment, '%')) " +
           "AND (u.compteVerrouille = false OR u.compteVerrouille IS NULL) " +
           "AND u.actif = true")
    List<Utilisateur> findActiveByRoleFragment(@Param("roleFragment") String roleFragment);

    /**
     * Utilisateurs actifs portant EXACTEMENT l'un des rôles donnés.
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u JOIN u.roles r " +
           "WHERE UPPER(r.nom) IN :roleNames " +
           "AND (u.compteVerrouille = false OR u.compteVerrouille IS NULL) " +
           "AND u.actif = true")
    List<Utilisateur> findActiveByRoleNames(@Param("roleNames") List<String> roleNames);

    /**
     * Utilisateurs actifs portant EXACTEMENT le rôle donné ET rattachés à la zone donnée.
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u JOIN u.roles r JOIN u.service s JOIN s.zone z " +
           "WHERE z.id = :zoneId AND UPPER(r.nom) = UPPER(:roleNom) " +
           "AND (u.compteVerrouille = false OR u.compteVerrouille IS NULL) " +
           "AND u.actif = true")
    List<Utilisateur> findActiveByRoleNomAndZoneId(@Param("roleNom") String roleNom,
                                                   @Param("zoneId") String zoneId);

    /**
     * Utilisateurs actifs portant l'un des rôles donnés ET rattachés à la zone donnée.
     */
    @Query("SELECT DISTINCT u FROM Utilisateur u JOIN u.roles r JOIN u.service s JOIN s.zone z " +
           "WHERE z.id = :zoneId AND UPPER(r.nom) IN :roleNames " +
           "AND (u.compteVerrouille = false OR u.compteVerrouille IS NULL) " +
           "AND u.actif = true")
    List<Utilisateur> findActiveByRoleNamesAndZoneId(@Param("roleNames") List<String> roleNames,
                                                     @Param("zoneId") String zoneId);
}

