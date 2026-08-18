package com.ocp.at.ai;

import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;

/**
 * Couche IA - assistance uniquement (LangChain / CrewAI / Mock).
 * Ne remplace jamais un visa CEEP/CEEE ni une décision du standard S-HSE-SEC-31.
 */
public interface IAProvider {

    AnalyseIA analyserPermis(FichierJoint fichier, Permis permis);

    /**
     * Analyse la description d'intervention → suggestions sections A, B, D, E du F-HSE-SEC-31-04.
     */
    AnalyseInterventionIAResponse analyserIntervention(String description);

    /**
     * Contrôle de complétude avant soumission (équivalent agents CrewAI Contrôleur AT).
     */
    AnalyseInterventionIAResponse controlerDossier(
            String description,
            boolean visiteFaite,
            int nbRisques,
            int nbMesures,
            int nbEpis,
            int nbPermis,
            boolean sectionFRenseignee);

    String getProviderName();
}
