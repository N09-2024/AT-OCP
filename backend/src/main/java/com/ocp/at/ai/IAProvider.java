package com.ocp.at.ai;

import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;

public interface IAProvider {
    
    /**
     * Analyse un fichier de permis via IA.
     * @param fichier Le fichier joint
     * @param permis Le permis concerné
     * @return AnalyseIA L'analyse résultante
     */
    AnalyseIA analyserPermis(FichierJoint fichier, Permis permis);
    
    /**
     * Retourne le nom du provider.
     */
    String getProviderName();
}
