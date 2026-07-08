package com.ocp.at.repository;

import com.ocp.at.entity.AnalyseRisque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyseRisqueRepository extends JpaRepository<AnalyseRisque, String> {

    boolean existsByVisitePrealableId(String visitePrealableId);

    Optional<AnalyseRisque> findByVisitePrealableId(String visitePrealableId);
}
