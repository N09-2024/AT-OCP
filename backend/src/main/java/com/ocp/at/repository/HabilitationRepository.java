package com.ocp.at.repository;

import com.ocp.at.entity.Habilitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabilitationRepository extends JpaRepository<Habilitation, String>, JpaSpecificationExecutor<Habilitation> {

    List<Habilitation> findByActifTrue();

    Optional<Habilitation> findByUtilisateurId(String utilisateurId);

    boolean existsByUtilisateurId(String utilisateurId);
}
