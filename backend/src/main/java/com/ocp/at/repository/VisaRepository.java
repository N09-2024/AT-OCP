package com.ocp.at.repository;

import com.ocp.at.entity.Visa;
import com.ocp.at.entity.enums.StatutVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisaRepository extends JpaRepository<Visa, String> {

    List<Visa> findByAutorisationTravailId(String atId);

    boolean existsByAutorisationTravailIdAndStatut(String atId, StatutVisa statut);
}
