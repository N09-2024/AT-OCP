package com.ocp.at.service.impl;

import com.ocp.at.ai.IAProvider;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.service.AssistanceIAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Assistance IA pour le formulaire F-HSE-SEC-31-04.
 * Utilise le provider @Primary (Mock par défaut).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssistanceIAServiceImpl implements AssistanceIAService {

    private final IAProvider iaProvider;

    @Override
    public AnalyseInterventionIAResponse analyserIntervention(String description) {
        log.info("IA analyserIntervention provider={}", iaProvider.getProviderName());
        return iaProvider.analyserIntervention(description);
    }

    @Override
    public AnalyseInterventionIAResponse controlerDossier(
            String description, Boolean visiteFaite, Integer nbRisques, Integer nbMesures,
            Integer nbEpis, Integer nbPermis, Boolean sectionFRenseignee) {
        log.info("IA controlerDossier provider={}", iaProvider.getProviderName());
        return iaProvider.controlerDossier(
                description,
                Boolean.TRUE.equals(visiteFaite),
                nbRisques != null ? nbRisques : 0,
                nbMesures != null ? nbMesures : 0,
                nbEpis != null ? nbEpis : 0,
                nbPermis != null ? nbPermis : 0,
                Boolean.TRUE.equals(sectionFRenseignee));
    }
}
