package com.ocp.at.repository;

import com.ocp.at.entity.EntrepriseExterne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EntrepriseExterneRepository extends JpaRepository<EntrepriseExterne, String>, JpaSpecificationExecutor<EntrepriseExterne> {
}

