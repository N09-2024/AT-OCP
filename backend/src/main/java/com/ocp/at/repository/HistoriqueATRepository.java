package com.ocp.at.repository;

import com.ocp.at.entity.HistoriqueAT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueATRepository extends JpaRepository<HistoriqueAT, String> {

    List<HistoriqueAT> findByAutorisationTravailIdOrderByDateActionDesc(String atId);
}
