package com.ocp.at.repository;

import com.ocp.at.entity.Permis;
import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.entity.enums.TypePermis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermisRepository extends JpaRepository<Permis, String> {

    List<Permis> findByAutorisationTravailId(String atId);

    List<Permis> findByStatutVerification(StatutPermis statut);

    boolean existsByNumero(String numero);

    List<Permis> findByType(TypePermis type);

    boolean existsByAutorisationTravailId(String atId);
}

