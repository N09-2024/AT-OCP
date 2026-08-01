package com.ocp.at.repository;

import com.ocp.at.entity.ClassificationIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassificationInterventionRepository extends JpaRepository<ClassificationIntervention, String>, JpaSpecificationExecutor<ClassificationIntervention> {

    List<ClassificationIntervention> findByNiveauOrderByDateClassificationDesc(String niveau);

    List<ClassificationIntervention> findByClassifieParIdOrderByDateClassificationDesc(String classifieParId);
}
