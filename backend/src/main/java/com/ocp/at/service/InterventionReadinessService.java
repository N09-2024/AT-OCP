package com.ocp.at.service;

import com.ocp.at.dto.response.ReadinessCheckResponse;

public interface InterventionReadinessService {

    /**
     * Effectue l'ensemble des 13 contrôles déterministes avant d'autoriser le démarrage de l'intervention.
     *
     * @param atId ID de l'autorisation de travail
     * @return ReadinessCheckResponse détaillant chaque point de contrôle
     */
    ReadinessCheckResponse checkInterventionReadiness(String atId);
}
