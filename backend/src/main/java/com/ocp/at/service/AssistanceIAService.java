package com.ocp.at.service;

import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;

/**
 * Service métier d'assistance IA pour le système AT-OCP.
 */
public interface AssistanceIAService {

    /**
     * Analyse complète d'une AT (CrewAI / LangChain avec repli automatique sur Mock).
     */
    AnalyzeAtResponse analyzeAt(AnalyzeAtRequest request);

    /**
     * Chat conversationnel avec l'assistant IA RAG.
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Rétrocompatible : analyse de description.
     */
    AnalyseInterventionIAResponse analyserIntervention(String description);

    /**
     * Rétrocompatible : contrôle de complétude du dossier.
     */
    AnalyseInterventionIAResponse controlerDossier(
            String description,
            Boolean visiteFaite,
            Integer nbRisques,
            Integer nbMesures,
            Integer nbEpis,
            Integer nbPermis,
            Boolean sectionFRenseignee);
}
