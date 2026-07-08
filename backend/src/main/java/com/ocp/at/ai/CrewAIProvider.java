package com.ocp.at.ai;

import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import org.springframework.stereotype.Component;

@Component("crewAIProvider")
public class CrewAIProvider implements IAProvider {

    @Override
    public AnalyseIA analyserPermis(FichierJoint fichier, Permis permis) {
        throw new UnsupportedOperationException("L'intégration CrewAI sera développée ultérieurement.");
    }

    @Override
    public String getProviderName() {
        return "CREW_AI";
    }
}
