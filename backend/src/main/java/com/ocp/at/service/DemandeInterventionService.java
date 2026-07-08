package com.ocp.at.service;

import com.ocp.at.dto.request.DemandeInterventionRequest;
import com.ocp.at.dto.response.DemandeInterventionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DemandeInterventionService {
    DemandeInterventionResponse create(DemandeInterventionRequest request, String demandeurId);
    DemandeInterventionResponse update(String id, DemandeInterventionRequest request);
    void delete(String id);
    DemandeInterventionResponse findById(String id);
    Page<DemandeInterventionResponse> findAll(Pageable pageable);
    
    // Pourra être implémenté plus tard pour chercher avec des specs
    Page<DemandeInterventionResponse> search(String query, Pageable pageable);
}
