package com.ocp.at.repository;

import com.ocp.at.entity.AutorisationTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutorisationTravailRepository extends JpaRepository<AutorisationTravail, String>, JpaSpecificationExecutor<AutorisationTravail> {

    @Query(value = "SELECT nextval('seq_at_' || cast(extract(year from current_date) as text))", nativeQuery = true)
    Long getNextSequence();

    boolean existsByDemandeInterventionId(String diId);

    boolean existsByOrdreTravailId(String otId);

    boolean existsByBonTravailId(String btId);
}
