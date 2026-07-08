package com.ocp.at.repository;

import com.ocp.at.entity.HistoriqueReception;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueReceptionRepository extends JpaRepository<HistoriqueReception, String> {

    List<HistoriqueReception> findByReceptionTravauxIdOrderByDateActionDesc(String receptionTravauxId);
}
