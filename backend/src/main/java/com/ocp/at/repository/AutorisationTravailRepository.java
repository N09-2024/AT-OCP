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
import com.ocp.at.entity.enums.TypeDocumentSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutorisationTravailRepository extends JpaRepository<AutorisationTravail, String>, JpaSpecificationExecutor<AutorisationTravail> {
@Query(value = "SELECT nextval('seq_at_' || cast(extract(year from current_date) as text))", nativeQuery = true)
    Long getNextSequence();

    boolean existsByTypeDocumentSourceAndNumeroDocumentSource(
        TypeDocumentSource typeDocumentSource, String numeroDocumentSource);

    boolean existsByDemandeInterventionId(String demandeInterventionId);
    boolean existsByOrdreTravailId(String ordreTravailId);
    boolean existsByBonTravailId(String bonTravailId);

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
     * Récupère une AT avec ses associations to-one principales
     */
    @EntityGraph(attributePaths = {
        "proprietaireBrouillon",
        "proprietaireBrouillon.roles",
        "demandeIntervention",
        "ordreTravail",
        "bonTravail",
        "zoneProprietaire",
        "zoneExecutante"
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

    /**
     * Compte les AT par statut
     */
    long countByStatut(StatutAT statut);

    /**
     * AT récentes de l'utilisateur
     */
    List<AutorisationTravail> findTop5ByProprietaireBrouillonIdOrderByDateCreationDesc(String proprietaireId);

    /**
     * AT récentes globales (pour dashboard admin)
     */
    List<AutorisationTravail> findTop5ByOrderByDateCreationDesc();

    /**
     * Statistiques mensuelles des AT (pour le graphe)
     */
    @Query(value = "SELECT to_char(date_creation, 'Mon') as mois, COUNT(*) as total FROM autorisations_travail GROUP BY to_char(date_creation, 'Mon'), extract(month from date_creation) ORDER BY extract(month from date_creation)", nativeQuery = true)
    List<Object[]> countAtByMonth();

    /**
     * Statistiques mensuelles des AT pour un utilisateur (Demandeur)
     */
    @Query(value = "SELECT to_char(date_creation, 'Mon') as mois, COUNT(*) as total FROM autorisations_travail WHERE proprietaire_brouillon_id = :userId GROUP BY to_char(date_creation, 'Mon'), extract(month from date_creation) ORDER BY extract(month from date_creation)", nativeQuery = true)
    List<Object[]> countAtByMonthForUser(@Param("userId") String userId);

    /**
     * Compte les AT par statut pour un utilisateur
     */
    long countByProprietaireBrouillonIdAndStatut(String userId, StatutAT statut);

    /**
     * Compte les AT par statut pour un utilisateur
     */
    @Query("SELECT at.statut, COUNT(at) FROM AutorisationTravail at WHERE at.proprietaireBrouillon.id = :userId GROUP BY at.statut")
    List<Object[]> countByStatutGroupedForUser(@Param("userId") String userId);

    // ============================================
    // REQUÊTES FILTRÉES PAR RÔLE (role-aware)
    // ============================================

    /**
     * AT visibles par le CEEP : uniquement ses propres brouillons et AT créées.
     * Un CEEP ne voit PAS les AT d'autres CEEP.
     */
    @Query("SELECT at FROM AutorisationTravail at WHERE at.proprietaireBrouillon.id = :userId")
    Page<AutorisationTravail> findByCeep(@Param("userId") String userId, Pageable pageable);

    /**
     * AT visibles par le CEEE : AT dont la zone exécutante correspond à la zone de son service,
     * ou le nom du service correspond à servicesIntervenants, et dont le statut est hors BROUILLON.
     */
    @Query("SELECT at FROM AutorisationTravail at WHERE " +
           "((at.zoneExecutante IS NOT NULL AND at.zoneExecutante.id = :zoneId) " +
           " OR (at.servicesIntervenants IS NOT NULL AND LOWER(at.servicesIntervenants) LIKE LOWER(CONCAT('%', :serviceNom, '%')))) " +
           "AND at.statut NOT IN ('BROUILLON')")
    Page<AutorisationTravail> findByCeeeByZoneAndService(@Param("zoneId") String zoneId, @Param("serviceNom") String serviceNom, Pageable pageable);

    /**
     * AT visibles par HCEP/HCEE : AT liées à leur zone (propriétaire ou exécutante).
     */
    @Query("SELECT DISTINCT at FROM AutorisationTravail at WHERE " +
           "(at.zoneProprietaire IS NOT NULL AND at.zoneProprietaire.id = :zoneId) " +
           "OR (at.zoneExecutante IS NOT NULL AND at.zoneExecutante.id = :zoneId) " +
           "OR (at.servicesIntervenants IS NOT NULL AND LOWER(at.servicesIntervenants) LIKE LOWER(CONCAT('%', :serviceNom, '%')))")
    Page<AutorisationTravail> findByHcByZoneAndService(@Param("zoneId") String zoneId, @Param("serviceNom") String serviceNom, Pageable pageable);

    /**
     * AT visibles par un Chef d'Équipe (CE) :
     * 1. AT qu'il a créées (proprietaireBrouillon = userId) — y compris ses brouillons (CEEP)
     * 2. AT où son service est exécutant et le statut est hors BROUILLON (CEEE)
     */
    @Query("SELECT DISTINCT at FROM AutorisationTravail at WHERE " +
           "(at.proprietaireBrouillon IS NOT NULL AND at.proprietaireBrouillon.id = :userId) " +
           "OR (" +
           "  ((at.zoneExecutante IS NOT NULL AND at.zoneExecutante.id = :zoneId) " +
           "   OR (at.servicesIntervenants IS NOT NULL AND LOWER(at.servicesIntervenants) LIKE LOWER(CONCAT('%', :serviceNom, '%')))) " +
           "  AND at.statut NOT IN ('BROUILLON')" +
           ")")
    Page<AutorisationTravail> findForChefEquipe(
        @Param("userId") String userId,
        @Param("zoneId") String zoneId,
        @Param("serviceNom") String serviceNom,
        Pageable pageable
    );

    /**
     * AT à viser par le CEEE (statut SOUMISE, DEMANDE_CREEE, EN_VISITE_REDACTION, AT_REDIGEE) liées à son service exécutant.
     */
    @Query("SELECT DISTINCT at FROM AutorisationTravail at WHERE " +
           "((at.zoneExecutante IS NOT NULL AND at.zoneExecutante.id = :zoneId) " +
           " OR (at.servicesIntervenants IS NOT NULL AND LOWER(at.servicesIntervenants) LIKE LOWER(CONCAT('%', :serviceNom, '%')))) " +
           "AND at.statut IN ('SOUMISE', 'DEMANDE_CREEE', 'EN_VISITE_REDACTION', 'AT_REDIGEE')")
    List<AutorisationTravail> findATaViserByCeee(@Param("zoneId") String zoneId, @Param("serviceNom") String serviceNom);
}
