package com.ocp.at.repository;

import com.ocp.at.entity.ArchiveAT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchiveRepository extends JpaRepository<ArchiveAT, String> {

    Optional<ArchiveAT> findByNumeroArchive(String numeroArchive);

    Optional<ArchiveAT> findByAutorisationTravailId(String autorisationTravailId);

    boolean existsByAutorisationTravailId(String autorisationTravailId);

    boolean existsByHashSHA256(String hashSHA256);

    Optional<ArchiveAT> findTopByAutorisationTravailIdOrderByVersionDesc(String autorisationTravailId);

    Optional<ArchiveAT> findTopByAutorisationTravailNumeroOrderByVersionDesc(String numero);

    @Query("SELECT a FROM ArchiveAT a WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "a.numeroArchive LIKE %:keyword% OR " +
            "a.autorisationTravail.numero LIKE %:keyword% OR " +
            "a.archivePar.matricule LIKE %:keyword%)")
    Page<ArchiveAT> search(@Param("keyword") String keyword, Pageable pageable);

    Page<ArchiveAT> findAllByDeletedFalse(Pageable pageable);
}
