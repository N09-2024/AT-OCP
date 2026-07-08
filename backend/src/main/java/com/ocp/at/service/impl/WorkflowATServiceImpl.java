package com.ocp.at.service.impl;

import com.ocp.at.entity.WorkflowAT;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.repository.WorkflowATRepository;
import com.ocp.at.service.WorkflowATService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowATServiceImpl implements WorkflowATService {

    private final WorkflowATRepository workflowRepository;
    private final com.ocp.at.repository.UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public WorkflowAT verifierTransition(StatutAT etatDepart, TypeActionAT action) {
        return workflowRepository.findByEtatDepartAndAction(etatDepart, action)
                .orElseThrow(() -> new BusinessException("Transition de workflow non autorisée : de " + etatDepart + " via l'action " + action));
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierRole(WorkflowAT workflow) {
        if (workflow.getRoleAutorise() != null && !workflow.getRoleAutorise().isEmpty()) {
            String userId = com.ocp.at.security.SecurityUtils.getCurrentUtilisateurId()
                    .orElseThrow(() -> new BusinessException("Non authentifié"));
            com.ocp.at.entity.Utilisateur currentUser = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));
            
            boolean hasRole = currentUser.getRoles().stream()
                    .anyMatch(r -> r.getNom().equals(workflow.getRoleAutorise()));
            if (!hasRole) {
                throw new com.ocp.at.exception.ForbiddenException("Vous n'avez pas le rôle requis pour cette action");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StatutAT obtenirEtatSuivant(String atId, StatutAT etatActuel) {
        // Implementation simplifiée : dans la vraie vie, il faudrait regarder 
        // les WorkflowAT configurés, le nombre de visas attendus, etc.
        // On retourne l'état actuel si ce n'est pas géré ici
        return etatActuel;
    }
}
