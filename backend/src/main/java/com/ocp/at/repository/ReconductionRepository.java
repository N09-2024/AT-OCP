package com.ocp.at.repository;

import com.ocp.at.entity.Reconduction;
import com.ocp.at.entity.enums.StatutReconduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReconductionRepository extends JpaRepository<Reconduction, String> {

    List<Reconduction> findByAutorisationTravailIdOrderByDateDemandeDesc(String autorisationTravailId);

    List<Reconduction> findByStatutOrderByDateDemandeDesc(StatutReconduction statut);

    boolean existsByAutorisationTravailIdAndStatut(String autorisationTravailId, StatutReconduction statut);

    long countByAutorisationTravailIdAndStatut(String autorisationTravailId, StatutReconduction statut);

    long countByAutorisationTravailId(String autorisationTravailId);

    @Query("SELECT r FROM Reconduction r WHERE r.autorisationTravail.id = :atId AND r.statut = 'REQUESTED'")
    Optional<Reconduction> findPendingByAutorisationTravailId(@Param("atId") String atId);
}
