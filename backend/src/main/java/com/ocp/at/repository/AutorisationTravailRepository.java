package com.ocp.at.repository;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.enums.StatutAT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutorisationTravailRepository extends JpaRepository<AutorisationTravail, String>, JpaSpecificationExecutor<AutorisationTravail> {

    @Query(value = "SELECT nextval('seq_at_' || cast(extract(year from current_date) as text))", nativeQuery = true)
    Long getNextSequence();

    boolean existsByDemandeInterventionId(String diId);

    boolean existsByOrdreTravailId(String otId);

    boolean existsByBonTravailId(String btId);

    // ============================================
    // OPTIMISATIONS N+1 - EntityGraph
    // ============================================

    /**
     * Récupère une AT avec tous ses visas (évite N+1 sur les visas)
     */
    @EntityGraph(attributePaths = {"visas", "visas.utilisateur"})
    @Query("SELECT at FROM AutorisationTravail at WHERE at.id = :id")
    Optional<AutorisationTravail> findByIdWithVisas(@Param("id") String id);

    /**
     * Récupère une AT avec ses permis (évite N+1 sur les permis)
     */
    @EntityGraph(attributePaths = {"permis"})
    @Query("SELECT at FROM AutorisationTravail at WHERE at.id = :id")
    Optional<AutorisationTravail> findByIdWithPermis(@Param("id") String id);

    /**
     * Récupère une AT avec son historique (évite N+1 sur l'historique)
     */
    @EntityGraph(attributePaths = {"historiques"})
    @Query("SELECT at FROM AutorisationTravail at WHERE at.id = :id")
    Optional<AutorisationTravail> findByIdWithHistoriques(@Param("id") String id);

    /**
     * Récupère une AT complète avec toutes les relations
     */
    @EntityGraph(attributePaths = {
            "visas", "visas.utilisateur",
            "permis", 
            "historiques",
            "proprietaireBrouillon", "proprietaireBrouillon.roles",
            "demandeIntervention",
            "ordreTravail",
            "bonTravail"
    })
    @Query("SELECT at FROM AutorisationTravail at WHERE at.id = :id")
    Optional<AutorisationTravail> findByIdWithAllRelations(@Param("id") String id);

    // ============================================
    // REQUÊTES FRÉQUENTES OPTIMISÉES
    // ============================================

    /**
     * Recherche par numéro (utilise l'index sur numero)
     */
    Optional<AutorisationTravail> findByNumero(String numero);

    /**
     * Liste les AT par statut (utilise l'index sur statut)
     */
    Page<AutorisationTravail> findByStatut(StatutAT statut, Pageable pageable);

    /**
     * Liste les AT par propriétaire (utilise l'index sur proprietaire_brouillon_id)
     */
    Page<AutorisationTravail> findByProprietaireBrouillonId(String proprietaireId, Pageable pageable);

    /**
     * AT expirées (date_fin < aujourd'hui et statut != CLOTUREE)
     */
    @Query("SELECT at FROM AutorisationTravail at WHERE at.dateFin < :date AND at.statut NOT IN ('CLOTUREE', 'ARCHIVEE')")
    Page<AutorisationTravail> findExpiredAt(@Param("date") LocalDate date, Pageable pageable);

    /**
     * AT en cours de validité
     */
    @Query("SELECT at FROM AutorisationTravail at WHERE at.dateDebut <= :date AND at.dateFin >= :date AND at.statut = 'VALIDEE'")
    Page<AutorisationTravail> findActiveAt(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Recherche textuelle optimisée (full-text si disponible, sinon LIKE)
     */
    @Query("SELECT at FROM AutorisationTravail at WHERE " +
           "LOWER(at.objet) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(at.numero) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<AutorisationTravail> searchByQuery(@Param("query") String query, Pageable pageable);

    /**
     * Statistiques par statut (pour dashboard)
     */
    @Query("SELECT at.statut, COUNT(at) FROM AutorisationTravail at GROUP BY at.statut")
    List<Object[]> countByStatutGrouped();
}
