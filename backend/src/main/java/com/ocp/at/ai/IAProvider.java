package com.ocp.at.ai;

import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;

/**
 * Couche d'abstraction IA pour l'assistance aux Autorisations de Travail (S-HSE-SEC-31).
 * L'IA intervient uniquement en rôle consultatif : elle ne remplace jamais un visa CEEP/CEEE.
 */
public interface IAProvider {

    AnalyseIA analyserPermis(FichierJoint fichier, Permis permis);

    /**
     * Analyse complète d'une AT (CrewAI / LangChain / Mock).
     */
    AnalyzeAtResponse analyzeAt(AnalyzeAtRequest request);

    /**
     * Assistant conversationnel s'appuyant sur le RAG et les référentiels OCP.
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Analyse la description d'intervention → suggestions sections A, B, D, E du F-HSE-SEC-31-04 (rétrocompatible).
     */
    AnalyseInterventionIAResponse analyserIntervention(String description);

    /**
     * Contrôle de complétude avant soumission (rétrocompatible).
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
