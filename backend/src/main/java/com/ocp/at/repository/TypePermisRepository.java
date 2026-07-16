package com.ocp.at.repository;

import com.ocp.at.entity.TypePermis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypePermisRepository extends JpaRepository<TypePermis, String> {
    boolean existsByNom(String nom);
    Optional<TypePermis> findByNom(String nom);
}
