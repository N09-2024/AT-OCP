package com.ocp.at.repository;

import com.ocp.at.entity.BonTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BonTravailRepository extends JpaRepository<BonTravail, String>, JpaSpecificationExecutor<BonTravail> {

    @Query(value = "SELECT nextval('seq_bt_' || cast(extract(year from current_date) as text))", nativeQuery = true)
    Long getNextSequence();
}
