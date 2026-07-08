package com.ocp.at.repository;

import com.ocp.at.entity.FichierJoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichierJointRepository extends JpaRepository<FichierJoint, String> {
}

