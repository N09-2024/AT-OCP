package com.ocp.at.repository;

import com.ocp.at.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String>, JpaSpecificationExecutor<Service> {
    boolean existsByZoneId(String zoneId);
}

