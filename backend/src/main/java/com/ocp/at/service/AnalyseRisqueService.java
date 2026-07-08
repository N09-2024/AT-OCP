package com.ocp.at.service;

import com.ocp.at.dto.request.AnalyseRisqueRequest;
import com.ocp.at.dto.response.AnalyseRisqueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnalyseRisqueService {

    /**
     * Crée une analyse des risques.
     * Règle : la visite préalable doit être finalisée (effectuee = true).
     * Règle : chaque visite ne peut avoir qu'une seule analyse.
     */
    AnalyseRisqueResponse create(AnalyseRisqueRequest request);

    /**
     * Modifie une analyse existante.
     */
    AnalyseRisqueResponse update(String id, AnalyseRisqueRequest request);

    /** Supprime une analyse des risques. */
    void delete(String id);

    /** Consulte une analyse par son ID. */
    AnalyseRisqueResponse findById(String id);

    /** Consulte l'analyse d'une visite donnée. */
    AnalyseRisqueResponse findByVisitePrealableId(String visiteId);

    /** Liste toutes les analyses avec pagination. */
    Page<AnalyseRisqueResponse> findAll(Pageable pageable);
}
