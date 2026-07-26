package com.ocp.at.service.impl;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Visa;
import com.ocp.at.entity.WorkflowAT;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.repository.VisaRepository;
import com.ocp.at.repository.WorkflowATRepository;
import com.ocp.at.service.WorkflowATService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowATServiceImpl implements WorkflowATService {

    private final WorkflowATRepository workflowRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AutorisationTravailRepository atRepository;
    private final VisaRepository visaRepository;

    @Override
    @Transactional(readOnly = true)
    public WorkflowAT verifierTransition(StatutAT etatDepart, TypeActionAT action) {
        log.debug("Vérification de la transition: {} -> {}", etatDepart, action);
        return workflowRepository.findActiveTransition(etatDepart, action)
                .orElseThrow(() -> new BusinessException(
                        "Transition de workflow non autorisée : de " + etatDepart + " via l'action " + action));
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierRole(WorkflowAT workflow) {
        if (workflow.getRoleAutorise() != null && !workflow.getRoleAutorise().isEmpty()) {
            String userId = com.ocp.at.security.SecurityUtils.getCurrentUtilisateurId()
                    .orElseThrow(() -> new BusinessException("Non authentifié"));
            com.ocp.at.entity.Utilisateur currentUser = utilisateurRepository.findByEmail(userId)
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
        log.info("Calcul de l'état suivant pour AT {} depuis l'état {}", atId, etatActuel);
        
        // Récupérer l'AT avec ses visas
        AutorisationTravail at = atRepository.findById(atId)
                .orElseThrow(() -> new BusinessException("AT non trouvée: " + atId));
        
        // Vérifier si tous les visas requis sont validés
        List<Visa> visas = visaRepository.findByAutorisationTravailId(atId);
        
        // Compter les visas requis vs validés
        long visasRequis = visas.stream()
                .filter(v -> v.getOrdre() != null)
                .count();
        long visasValides = visas.stream()
                .filter(v -> "VALIDE".equals(v.getStatut()))
                .count();
        
        log.debug("AT {}: {} visas requis, {} validés", atId, visasRequis, visasValides);
        
        // Trouver la transition automatique basée sur l'état actuel
        List<WorkflowAT> transitionsPossibles = workflowRepository.findByEtatDepartAndActifTrue(etatActuel);
        
        for (WorkflowAT transition : transitionsPossibles) {
            // Si c'est une transition automatique sans condition de visa
            if (transition.getValidationObligatoire() == null || !transition.getValidationObligatoire()) {
                log.info("Transition automatique trouvée: {} -> {}", etatActuel, transition.getEtatArrivee());
                return transition.getEtatArrivee();
            }
            
            // Si tous les visas sont validés, passer à l'état suivant
            if (visasRequis > 0 && visasValides >= visasRequis) {
                log.info("Tous les visas validés. Transition: {} -> {}", etatActuel, transition.getEtatArrivee());
                return transition.getEtatArrivee();
            }
        }
        
        // Aucune transition possible, garder l'état actuel
        log.warn("Aucune transition disponible pour AT {} depuis l'état {}", atId, etatActuel);
        return etatActuel;
    }
}
