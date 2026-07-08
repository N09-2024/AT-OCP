package com.ocp.at.repository;

import com.ocp.at.entity.Installation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallationRepository extends JpaRepository<Installation, String>, JpaSpecificationExecutor<Installation> {
    boolean existsByServiceId(String serviceId);
}

