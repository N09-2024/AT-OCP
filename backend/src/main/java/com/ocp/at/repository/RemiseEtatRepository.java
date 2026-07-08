package com.ocp.at.repository;

import com.ocp.at.entity.RemiseEtat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RemiseEtatRepository extends JpaRepository<RemiseEtat, String> {

    Optional<RemiseEtat> findByReceptionTravauxId(String receptionId);
}
