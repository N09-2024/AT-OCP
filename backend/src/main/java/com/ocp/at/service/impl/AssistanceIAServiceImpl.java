package com.ocp.at.service.impl;

import com.ocp.at.ai.IAProvider;
import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;
import com.ocp.at.service.AssistanceIAService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service d'assistance IA avec résilience et bascule automatique (Circuit Breaker pattern).
 * Si le microservice Python (FastAPI / CrewAI / LangChain) est indisponible ou en erreur,
 * bascule de manière transparente sur le MockAIProvider pour garantir la continuité de service.
 */
@Service
@Slf4j
public class AssistanceIAServiceImpl implements AssistanceIAService {

    private final IAProvider iaProvider;
    private final IAProvider mockProvider;

    public AssistanceIAServiceImpl(
            IAProvider iaProvider,
            @Qualifier("mockAIProvider") IAProvider mockProvider) {
        this.iaProvider = iaProvider;
        this.mockProvider = mockProvider;
    }

    @Override
    public AnalyzeAtResponse analyzeAt(AnalyzeAtRequest request) {
        log.info("Appel IA analyzeAt via provider={}", iaProvider.getProviderName());
        try {
            return iaProvider.analyzeAt(request);
        } catch (Exception ex) {
            log.warn("Provider IA {} indisponible ({}), repli sur MockAIProvider.", iaProvider.getProviderName(), ex.getMessage());
            return mockProvider.analyzeAt(request);
        }
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        log.info("Appel IA chat via provider={}", iaProvider.getProviderName());
        try {
            return iaProvider.chat(request);
        } catch (Exception ex) {
            log.warn("Provider IA {} indisponible ({}), repli sur MockAIProvider pour le chat.", iaProvider.getProviderName(), ex.getMessage());
            return mockProvider.chat(request);
        }
    }

    @Override
    public AnalyseInterventionIAResponse analyserIntervention(String description) {
        log.info("Appel IA analyserIntervention via provider={}", iaProvider.getProviderName());
        try {
            return iaProvider.analyserIntervention(description);
        } catch (Exception ex) {
            log.warn("Provider {} indisponible ({}), repli sur Mock.", iaProvider.getProviderName(), ex.getMessage());
            return mockProvider.analyserIntervention(description);
        }
    }

    @Override
    public AnalyseInterventionIAResponse controlerDossier(
            String description, Boolean visiteFaite, Integer nbRisques, Integer nbMesures,
            Integer nbEpis, Integer nbPermis, Boolean sectionFRenseignee) {
        log.info("Appel IA controlerDossier via provider={}", iaProvider.getProviderName());
        try {
            return iaProvider.controlerDossier(
                    description,
                    Boolean.TRUE.equals(visiteFaite),
                    nbRisques != null ? nbRisques : 0,
                    nbMesures != null ? nbMesures : 0,
                    nbEpis != null ? nbEpis : 0,
                    nbPermis != null ? nbPermis : 0,
                    Boolean.TRUE.equals(sectionFRenseignee));
        } catch (Exception ex) {
            log.warn("Provider {} indisponible ({}), repli sur Mock.", iaProvider.getProviderName(), ex.getMessage());
            return mockProvider.controlerDossier(
                    description,
                    Boolean.TRUE.equals(visiteFaite),
                    nbRisques != null ? nbRisques : 0,
                    nbMesures != null ? nbMesures : 0,
                    nbEpis != null ? nbEpis : 0,
                    nbPermis != null ? nbPermis : 0,
                    Boolean.TRUE.equals(sectionFRenseignee));
        }
    }
}
