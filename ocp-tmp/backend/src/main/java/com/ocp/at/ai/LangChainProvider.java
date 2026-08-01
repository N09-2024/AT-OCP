package com.ocp.at.ai;

import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Bridge LangChain — appelle le microservice Python FastAPI si configuré,
 * sinon UnsupportedOperationException (utiliser mockAIProvider en @Primary).
 */
@Component("langChainProvider")
public class LangChainProvider implements IAProvider {

    @Value("${ocp.ai.fastapi-url:}")
    private String fastapiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AnalyseIA analyserPermis(FichierJoint fichier, Permis permis) {
        throw new UnsupportedOperationException("Configurer ocp.ai.fastapi-url pour LangChain.");
    }

    @Override
    public AnalyseInterventionIAResponse analyserIntervention(String description) {
        if (fastapiUrl == null || fastapiUrl.isBlank()) {
            throw new UnsupportedOperationException("LangChain : définir ocp.ai.fastapi-url");
        }
        return restTemplate.postForObject(
                fastapiUrl + "/analyse-intervention",
                java.util.Map.of("description", description != null ? description : ""),
                AnalyseInterventionIAResponse.class);
    }

    @Override
    public AnalyseInterventionIAResponse controlerDossier(
            String description, boolean visiteFaite, int nbRisques, int nbMesures,
            int nbEpis, int nbPermis, boolean sectionFRenseignee) {
        if (fastapiUrl == null || fastapiUrl.isBlank()) {
            throw new UnsupportedOperationException("LangChain : définir ocp.ai.fastapi-url");
        }
        return restTemplate.postForObject(
                fastapiUrl + "/controler-dossier",
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
        return "LANG_CHAIN";
    }
}
