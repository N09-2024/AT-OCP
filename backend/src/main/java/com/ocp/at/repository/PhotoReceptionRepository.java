package com.ocp.at.repository;

import com.ocp.at.entity.PhotoReception;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoReceptionRepository extends JpaRepository<PhotoReception, String> {

    List<PhotoReception> findByReceptionTravauxIdOrderByOrdreAsc(String receptionTravauxId);

    Optional<PhotoReception> findByIdAndReceptionTravauxId(String id, String receptionTravauxId);

    void deleteByReceptionTravauxId(String receptionTravauxId);
}
