package com.ocp.at.repository;

import com.ocp.at.entity.WorkflowAT;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowATRepository extends JpaRepository<WorkflowAT, String> {

    Optional<WorkflowAT> findByEtatDepartAndAction(StatutAT etatDepart, TypeActionAT action);
    
    List<WorkflowAT> findByEtatDepartAndActifTrue(StatutAT etatDepart);
    
    @Query("SELECT w FROM WorkflowAT w WHERE w.etatDepart = :etatDepart AND w.action = :action AND w.actif = true")
    Optional<WorkflowAT> findActiveTransition(@Param("etatDepart") StatutAT etatDepart, 
                                              @Param("action") TypeActionAT action);
}
