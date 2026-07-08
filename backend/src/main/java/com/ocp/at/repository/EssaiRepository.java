package com.ocp.at.repository;

import com.ocp.at.entity.Essai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EssaiRepository extends JpaRepository<Essai, String> {

    List<Essai> findByReceptionTravauxId(String receptionId);

    boolean existsByReceptionTravauxIdAndConformeIsFalse(String receptionId);
}
