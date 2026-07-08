package com.ocp.at.repository;

import com.ocp.at.entity.MoyenAcces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MoyenAccesRepository extends JpaRepository<MoyenAcces, String>, JpaSpecificationExecutor<MoyenAcces> {
}

