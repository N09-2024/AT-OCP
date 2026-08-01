package com.ocp.at.service;

import com.ocp.at.dto.response.AnalyseInterventionIAResponse;

public interface AssistanceIAService {

    AnalyseInterventionIAResponse analyserIntervention(String description);

    AnalyseInterventionIAResponse controlerDossier(
            String description,
            Boolean visiteFaite,
            Integer nbRisques,
            Integer nbMesures,
            Integer nbEpis,
            Integer nbPermis,
            Boolean sectionFRenseignee);
}
