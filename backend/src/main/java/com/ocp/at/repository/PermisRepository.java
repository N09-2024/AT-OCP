package com.ocp.at.repository;

import com.ocp.at.entity.Permis;
import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.entity.enums.TypePermis;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermisRepository extends JpaRepository<Permis, String> {

    List<Permis> findByAutorisationTravailId(String atId);

    List<Permis> findByStatutVerification(StatutPermis statut);

    boolean existsByNumero(String numero);

    List<Permis> findByType(TypePermis type);

    boolean existsByAutorisationTravailId(String atId);

    // ============================================
    // OPTIMISATIONS N+1 - EntityGraph
    // ============================================

    /**
     * Récupère les permis avec l'analyse IA (évite N+1)
     */
    @EntityGraph(attributePaths = {"analyseIA"})
    @Query("SELECT p FROM Permis p WHERE p.autorisationTravail.id = :atId")
    List<Permis> findByAutorisationTravailIdWithAnalyse(@Param("atId") String atId);

    /**
     * Récupère un permis avec son analyse IA
     */
    @EntityGraph(attributePaths = {"analyseIA", "fichierJoint"})
    @Query("SELECT p FROM Permis p WHERE p.id = :id")
    Optional<Permis> findByIdWithAnalyse(@Param("id") String id);

    // ============================================
    // REQUÊTES FRÉQUENTES OPTIMISÉES
    // ============================================

    /**
     * Permis expirant avant une date (pour alertes)
     */
    @Query("SELECT p FROM Permis p WHERE p.dateExpiration <= :date AND p.dateExpiration IS NOT NULL")
    List<Permis> findExpiringBefore(@Param("date") LocalDate date);

    /**
     * Permis expirés et non conformes
     */
    @Query("SELECT p FROM Permis p WHERE p.dateExpiration < :date OR p.statutVerification IN ('EXPIRE', 'INVALIDE')")
    List<Permis> findExpiredOrInvalid(@Param("date") LocalDate date);

    /**
     * Vérifier la conformité de tous les permis d'une AT
     */
    @Query("SELECT CASE WHEN COUNT(p) = 0 THEN true ELSE false END " +
           "FROM Permis p WHERE p.autorisationTravail.id = :atId " +
           "AND p.statutVerification <> 'CONFORME'")
    boolean areAllPermisConformes(@Param("atId") String atId);

    /**
     * Compter les permis par statut pour une AT
     */
    @Query("SELECT p.statutVerification, COUNT(p) FROM Permis p " +
           "WHERE p.autorisationTravail.id = :atId GROUP BY p.statutVerification")
    List<Object[]> countByStatutForAt(@Param("atId") String atId);

    long countByStatutVerification(StatutPermis statutVerification);
}

