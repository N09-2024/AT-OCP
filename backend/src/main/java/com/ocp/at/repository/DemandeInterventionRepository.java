package com.ocp.at.repository;

import com.ocp.at.entity.DemandeIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandeInterventionRepository extends JpaRepository<DemandeIntervention, String>, JpaSpecificationExecutor<DemandeIntervention> {

    @Query(value = "SELECT nextval('seq_di_' || cast(extract(year from current_date) as text))", nativeQuery = true)
    Long getNextSequence();
}
