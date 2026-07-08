package com.ocp.at.repository;

import com.ocp.at.entity.AnalyseIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyseIARepository extends JpaRepository<AnalyseIA, String> {
}

