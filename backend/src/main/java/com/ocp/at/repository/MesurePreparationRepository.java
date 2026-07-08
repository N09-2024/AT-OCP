package com.ocp.at.repository;

import com.ocp.at.entity.MesurePreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MesurePreparationRepository extends JpaRepository<MesurePreparation, String>, JpaSpecificationExecutor<MesurePreparation> {
}

