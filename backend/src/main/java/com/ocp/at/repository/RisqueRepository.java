package com.ocp.at.repository;

import com.ocp.at.entity.Risque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RisqueRepository extends JpaRepository<Risque, String>, JpaSpecificationExecutor<Risque> {
}

