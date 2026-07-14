package com.ocp.at.repository;

import com.ocp.at.entity.ReceptionTravaux;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceptionTravauxRepository extends JpaRepository<ReceptionTravaux, String> {

    boolean existsByAutorisationTravailId(String autorisationTravailId);

    Optional<ReceptionTravaux> findByAutorisationTravailId(String autorisationTravailId);

    Page<ReceptionTravaux> findAll(Pageable pageable);

    @Query("SELECT COUNT(r) FROM ReceptionTravaux r WHERE r.validee = false OR r.validee IS NULL")
    long countPendingReceptions();
}
