package com.ocp.at.ai;

import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import org.springframework.stereotype.Component;

@Component("langChainProvider")
public class LangChainProvider implements IAProvider {

    @Override
    public AnalyseIA analyserPermis(FichierJoint fichier, Permis permis) {
        throw new UnsupportedOperationException("L'intégration LangChain sera développée ultérieurement.");
    }

    @Override
    public String getProviderName() {
        return "LANG_CHAIN";
    }
}
