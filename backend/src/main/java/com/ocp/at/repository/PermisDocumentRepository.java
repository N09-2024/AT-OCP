package com.ocp.at.repository;

import com.ocp.at.entity.PermisDocument;
import com.ocp.at.entity.enums.StatutPermisDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermisDocumentRepository extends JpaRepository<PermisDocument, String> {

    List<PermisDocument> findByAutorisationTravailId(String atId);

    Optional<PermisDocument> findByAutorisationTravailIdAndTypePermisAttendu(String atId, String typePermis);

    long countByAutorisationTravailId(String atId);

    long countByAutorisationTravailIdAndStatut(String atId, StatutPermisDocument statut);

    @Modifying
    @Query("DELETE FROM PermisDocument pd WHERE pd.autorisationTravail.id = :atId AND pd.typePermisAttendu NOT IN :types")
    void deleteByAtIdAndTypeNotIn(@Param("atId") String atId, @Param("types") List<String> types);
}
