package com.ocp.at.ai;

import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Bridge CrewAI multi-agents (Agent Risques → Agent HSE → Agent AT) via microservice Python FastAPI.
 */
@Component("crewAIProvider")
@Slf4j
public class CrewAIProvider implements IAProvider {

    @Value("${ocp.ai.fastapi-url:http://localhost:8000}")
    private String fastapiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AnalyseIA analyserPermis(FichierJoint fichier, Permis permis) {
        throw new UnsupportedOperationException("CrewAI : analyse de fichier joint non supportée.");
    }

    @Override
    public AnalyzeAtResponse analyzeAt(AnalyzeAtRequest request) {
        if (fastapiUrl == null || fastapiUrl.isBlank()) {
            throw new IllegalStateException("CrewAI : ocp.ai.fastapi-url non configuré.");
        }
        return restTemplate.postForObject(
                fastapiUrl + "/api/ai/analyze-at",
                request,
                AnalyzeAtResponse.class);
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        if (fastapiUrl == null || fastapiUrl.isBlank()) {
            throw new IllegalStateException("CrewAI : ocp.ai.fastapi-url non configuré.");
        }
        return restTemplate.postForObject(
                fastapiUrl + "/api/ai/chat",
                request,
                ChatResponse.class);
    }

    @Override
    public AnalyseInterventionIAResponse analyserIntervention(String description) {
        if (fastapiUrl == null || fastapiUrl.isBlank()) {
            throw new IllegalStateException("CrewAI : définir ocp.ai.fastapi-url");
        }
        return restTemplate.postForObject(
                fastapiUrl + "/crew/analyse-intervention",
                java.util.Map.of("description", description != null ? description : ""),
                AnalyseInterventionIAResponse.class);
    }

    @Override
    public AnalyseInterventionIAResponse controlerDossier(
            String description, boolean visiteFaite, int nbRisques, int nbMesures,
            int nbEpis, int nbPermis, boolean sectionFRenseignee) {
        if (fastapiUrl == null || fastapiUrl.isBlank()) {
            throw new IllegalStateException("CrewAI : définir ocp.ai.fastapi-url");
        }
        return restTemplate.postForObject(
                fastapiUrl + "/crew/controler-dossier",
                java.util.Map.of(
                        "description", description != null ? description : "",
                        "visiteFaite", visiteFaite,
                        "nbRisques", nbRisques,
                        "nbMesures", nbMesures,
                        "nbEpis", nbEpis,
                        "nbPermis", nbPermis,
                        "sectionFRenseignee", sectionFRenseignee),
                AnalyseInterventionIAResponse.class);
    }

    @Override
    public String getProviderName() {
        return "CREW_AI";
    }
}
