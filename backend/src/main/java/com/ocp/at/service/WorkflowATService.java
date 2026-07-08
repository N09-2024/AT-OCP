package com.ocp.at.service;

import com.ocp.at.entity.WorkflowAT;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;

public interface WorkflowATService {

    /**
     * Vérifie si une transition d'état est valide selon les règles de workflow.
     * @param etatDepart le statut actuel de l'AT
     * @param action l'action entreprise (ex: SOUMISSION)
     * @return le WorkflowAT si la transition est valide
     * @throws com.ocp.at.exception.BusinessException si la transition est illégale
     */
    WorkflowAT verifierTransition(StatutAT etatDepart, TypeActionAT action);

    /**
     * Vérifie si l'utilisateur possède le rôle requis par la transition.
     */
    void verifierRole(WorkflowAT workflow);

    /**
     * Récupère l'état suivant basé sur les visas validés.
     */
    StatutAT obtenirEtatSuivant(String atId, StatutAT etatActuel);
}
