package com.ocp.at.repository;

import com.ocp.at.entity.EPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EPIRepository extends JpaRepository<EPI, String>, JpaSpecificationExecutor<EPI> {
}

