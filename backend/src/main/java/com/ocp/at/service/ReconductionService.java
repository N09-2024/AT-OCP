package com.ocp.at.service;

import com.ocp.at.dto.request.DecisionReconductionRequest;
import com.ocp.at.dto.request.DemandeReconductionRequest;
import com.ocp.at.dto.response.ReconductionResponse;

import java.util.List;

public interface ReconductionService {

    /**
     * Formuler une demande de reconduction d'AT par le CEEE.
     */
    ReconductionResponse demanderReconduction(String atId, DemandeReconductionRequest request);

    /**
     * Approuver ou refuser une demande de reconduction par le HMEP (Responsable OCP).
     */
    ReconductionResponse deciderReconduction(String reconductionId, DecisionReconductionRequest request);

    /**
     * Consulter l'historique des reconductions pour une AT.
     */
    List<ReconductionResponse> getReconductionsByAtId(String atId);

    /**
     * Consulter toutes les demandes de reconduction en attente d'approbation (boîte HMEP).
     */
    List<ReconductionResponse> getPendingReconductions();

    /**
     * Récupérer une demande de reconduction par son ID.
     */
    ReconductionResponse getById(String id);
}
