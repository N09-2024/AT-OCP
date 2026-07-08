package com.ocp.at.repository;

import com.ocp.at.entity.WorkflowAT;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowATRepository extends JpaRepository<WorkflowAT, String> {

    Optional<WorkflowAT> findByEtatDepartAndAction(StatutAT etatDepart, TypeActionAT action);
}
