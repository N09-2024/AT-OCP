package com.ocp.at.repository;

import com.ocp.at.entity.OrdreTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdreTravailRepository extends JpaRepository<OrdreTravail, String>, JpaSpecificationExecutor<OrdreTravail> {

    @Query(value = "SELECT nextval('seq_ot_' || cast(extract(year from current_date) as text))", nativeQuery = true)
    Long getNextSequence();
}
