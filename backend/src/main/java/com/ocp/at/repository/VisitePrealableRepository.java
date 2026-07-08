package com.ocp.at.repository;

import com.ocp.at.entity.VisitePrealable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitePrealableRepository extends JpaRepository<VisitePrealable, String> {

    /**
     * Trouve la visite liée à une DI.
     */
    @Query("SELECT di.visitePrealable FROM DemandeIntervention di WHERE di.id = :documentId")
    Optional<VisitePrealable> findByDemandeInterventionId(@Param("documentId") String documentId);

    /**
     * Trouve la visite liée à un OT.
     */
    @Query("SELECT ot.visitePrealable FROM OrdreTravail ot WHERE ot.id = :documentId")
    Optional<VisitePrealable> findByOrdreTravailId(@Param("documentId") String documentId);

    /**
     * Trouve la visite liée à un BT.
     */
    @Query("SELECT bt.visitePrealable FROM BonTravail bt WHERE bt.id = :documentId")
    Optional<VisitePrealable> findByBonTravailId(@Param("documentId") String documentId);

    /**
     * Vérifie si une DI a déjà une visite préalable.
     */
    @Query("SELECT COUNT(di) > 0 FROM DemandeIntervention di WHERE di.id = :documentId AND di.visitePrealable IS NOT NULL")
    boolean existsForDI(@Param("documentId") String documentId);

    /**
     * Vérifie si un OT a déjà une visite préalable.
     */
    @Query("SELECT COUNT(ot) > 0 FROM OrdreTravail ot WHERE ot.id = :documentId AND ot.visitePrealable IS NOT NULL")
    boolean existsForOT(@Param("documentId") String documentId);

    /**
     * Vérifie si un BT a déjà une visite préalable.
     */
    @Query("SELECT COUNT(bt) > 0 FROM BonTravail bt WHERE bt.id = :documentId AND bt.visitePrealable IS NOT NULL")
    boolean existsForBT(@Param("documentId") String documentId);
}
