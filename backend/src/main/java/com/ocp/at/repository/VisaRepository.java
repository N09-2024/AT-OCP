package com.ocp.at.repository;

import com.ocp.at.entity.Visa;
import com.ocp.at.entity.enums.StatutVisa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisaRepository extends JpaRepository<Visa, String> {

    List<Visa> findByAutorisationTravailId(String atId);

    boolean existsByAutorisationTravailIdAndStatut(String atId, StatutVisa statut);

    // ============================================
    // OPTIMISATIONS N+1 - EntityGraph
    // ============================================

    /**
     * Récupère les visas avec l'utilisateur signataire (évite N+1)
     */
    @EntityGraph(attributePaths = {"utilisateur"})
    @Query("SELECT v FROM Visa v WHERE v.autorisationTravail.id = :atId")
    List<Visa> findByAutorisationTravailIdWithUtilisateur(@Param("atId") String atId);

    /**
     * Récupère un visa avec son utilisateur
     */
    @EntityGraph(attributePaths = {"utilisateur"})
    @Query("SELECT v FROM Visa v WHERE v.id = :id")
    Optional<Visa> findByIdWithUtilisateur(@Param("id") String id);

    // ============================================
    // REQUÊTES FRÉQUENTES OPTIMISÉES
    // ============================================

    /**
     * Visa par AT et statut
     */
    List<Visa> findByAutorisationTravailIdAndStatut(String atId, StatutVisa statut);

    /**
     * Compter les visas validés pour une AT
     */
    @Query("SELECT COUNT(v) FROM Visa v WHERE v.autorisationTravail.id = :atId AND v.statut = :statut")
    long countByAtIdAndStatut(@Param("atId") String atId, @Param("statut") StatutVisa statut);

    /**
     * Vérifier si tous les visas requis sont validés
     */
    @Query("SELECT CASE WHEN COUNT(v) = 0 THEN true ELSE false END " +
           "FROM Visa v WHERE v.autorisationTravail.id = :atId " +
           "AND (v.statut <> 'VALIDE' OR v.statut IS NULL)")
    boolean areAllVisasValidated(@Param("atId") String atId);

    long countByStatut(StatutVisa statut);
}
