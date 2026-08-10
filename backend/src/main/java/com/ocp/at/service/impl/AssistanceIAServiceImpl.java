package com.ocp.at.service.impl;

import com.ocp.at.ai.IAProvider;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.service.AssistanceIAService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Assistance IA pour le formulaire F-HSE-SEC-31-04.
 * Utilise le provider @Primary sélectionné par AIConfig.
 * En cas d'erreur réseau (FastAPI indisponible), bascule automatiquement
 * sur le Mock pour ne jamais bloquer le formulaire.
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
    public AnalyseInterventionIAResponse analyserIntervention(String description) {
        log.info("IA analyserIntervention provider={}", iaProvider.getProviderName());
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
        log.info("IA controlerDossier provider={}", iaProvider.getProviderName());
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
