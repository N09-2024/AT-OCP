package com.ocp.at.service.impl;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Visa;
import com.ocp.at.entity.WorkflowAT;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ForbiddenException;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.repository.VisaRepository;
import com.ocp.at.repository.WorkflowATRepository;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.WorkflowATService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Moteur de workflow conforme au Standard OCP S-HSE-SEC-31 §7 / §8.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowATServiceImpl implements WorkflowATService {

    private final WorkflowATRepository workflowRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AutorisationTravailRepository atRepository;
    private final VisaRepository visaRepository;

    private static final Map<StatutAT, Map<TypeActionAT, StatutAT>> TRANSITIONS = new EnumMap<>(StatutAT.class);

    static {
        // Standard S-HSE-SEC-31 §7 workflow principal
        put(StatutAT.CLASSIFICATION_EFFECTUEE, TypeActionAT.CREATION_DEMANDE, StatutAT.DEMANDE_CREEE);

        put(StatutAT.DEMANDE_CREEE, TypeActionAT.VISITE_CHANTIER, StatutAT.EN_VISITE_REDACTION);
        put(StatutAT.DEMANDE_CREEE, TypeActionAT.SOUMISSION, StatutAT.EN_VISITE_REDACTION);
        put(StatutAT.DEMANDE_CREEE, TypeActionAT.REDACTION_AT, StatutAT.EN_VISITE_REDACTION);
        put(StatutAT.DEMANDE_CREEE, TypeActionAT.VALIDATION, StatutAT.AT_VALIDEE);
        put(StatutAT.DEMANDE_CREEE, TypeActionAT.ANNULATION, StatutAT.ANNULEE);

        put(StatutAT.EN_VISITE_REDACTION, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);
        put(StatutAT.EN_VISITE_REDACTION, TypeActionAT.REDACTION_AT, StatutAT.AT_REDIGEE);
        put(StatutAT.EN_VISITE_REDACTION, TypeActionAT.SOUMISSION, StatutAT.AT_REDIGEE);
        put(StatutAT.EN_VISITE_REDACTION, TypeActionAT.VALIDATION, StatutAT.AT_VALIDEE);
        put(StatutAT.EN_VISITE_REDACTION, TypeActionAT.REFUS, StatutAT.REJETEE);
        put(StatutAT.EN_VISITE_REDACTION, TypeActionAT.ANNULATION, StatutAT.ANNULEE);

        put(StatutAT.VISITE_REALISEE, TypeActionAT.REDACTION_AT, StatutAT.AT_REDIGEE);
        put(StatutAT.VISITE_REALISEE, TypeActionAT.SOUMISSION, StatutAT.AT_REDIGEE);
        put(StatutAT.VISITE_REALISEE, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);
        put(StatutAT.VISITE_REALISEE, TypeActionAT.VALIDATION, StatutAT.AT_VALIDEE);
        put(StatutAT.VISITE_REALISEE, TypeActionAT.REFUS, StatutAT.REJETEE);
        put(StatutAT.VISITE_REALISEE, TypeActionAT.ANNULATION, StatutAT.ANNULEE);

        put(StatutAT.AT_REDIGEE, TypeActionAT.VALIDATION, StatutAT.AT_VALIDEE);
        put(StatutAT.AT_REDIGEE, TypeActionAT.REFUS, StatutAT.REJETEE);
        put(StatutAT.AT_REDIGEE, TypeActionAT.DEBUT_INTERVENTION, StatutAT.EN_COURS);
        put(StatutAT.AT_REDIGEE, TypeActionAT.ANNULATION, StatutAT.ANNULEE);

        put(StatutAT.AT_VALIDEE, TypeActionAT.VALIDATION, StatutAT.AT_VALIDEE);
        put(StatutAT.AT_VALIDEE, TypeActionAT.DEBUT_INTERVENTION, StatutAT.EN_COURS);
        put(StatutAT.AT_VALIDEE, TypeActionAT.RECONDUCTION, StatutAT.EN_RECONDUCTION);
        put(StatutAT.AT_VALIDEE, TypeActionAT.DECLARATION_FIN, StatutAT.DECLAREE_TERMINEE);
        put(StatutAT.AT_VALIDEE, TypeActionAT.REFUS, StatutAT.REJETEE);
        put(StatutAT.AT_VALIDEE, TypeActionAT.ANNULATION, StatutAT.ANNULEE);

        put(StatutAT.EN_COURS, TypeActionAT.RECONDUCTION, StatutAT.EN_RECONDUCTION);
        put(StatutAT.EN_COURS, TypeActionAT.DECLARATION_FIN, StatutAT.DECLAREE_TERMINEE);
        put(StatutAT.EN_COURS, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);

        put(StatutAT.INTERVENTION_EN_COURS, TypeActionAT.RECONDUCTION, StatutAT.AT_RECONDUITE);
        put(StatutAT.INTERVENTION_EN_COURS, TypeActionAT.DECLARATION_FIN, StatutAT.FIN_TRAVAUX_DECLAREE);
        put(StatutAT.INTERVENTION_EN_COURS, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);

        put(StatutAT.EN_RECONDUCTION, TypeActionAT.DEBUT_INTERVENTION, StatutAT.EN_COURS);
        put(StatutAT.EN_RECONDUCTION, TypeActionAT.DECLARATION_FIN, StatutAT.DECLAREE_TERMINEE);
        put(StatutAT.EN_RECONDUCTION, TypeActionAT.RECONDUCTION, StatutAT.EN_RECONDUCTION);

        put(StatutAT.AT_RECONDUITE, TypeActionAT.DEBUT_INTERVENTION, StatutAT.INTERVENTION_EN_COURS);
        put(StatutAT.AT_RECONDUITE, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);
        put(StatutAT.AT_RECONDUITE, TypeActionAT.DECLARATION_FIN, StatutAT.FIN_TRAVAUX_DECLAREE);
        put(StatutAT.AT_RECONDUITE, TypeActionAT.RECONDUCTION, StatutAT.AT_RECONDUITE);

        put(StatutAT.DECLAREE_TERMINEE, TypeActionAT.RECEPTION_CONJOINTE, StatutAT.RECEPTIONEES);
        put(StatutAT.DECLAREE_TERMINEE, TypeActionAT.CLOTURE, StatutAT.RECEPTIONEES);
        put(StatutAT.FIN_TRAVAUX_DECLAREE, TypeActionAT.RECEPTION_CONJOINTE, StatutAT.TRAVAUX_RECEPTIONES);

        put(StatutAT.RECEPTIONEES, TypeActionAT.ARCHIVAGE_OFFICIEL, StatutAT.ARCHIVEE);
        put(StatutAT.TRAVAUX_RECEPTIONES, TypeActionAT.ARCHIVAGE_OFFICIEL, StatutAT.ARCHIVEE);

        // Pont legacy
        put(StatutAT.BROUILLON, TypeActionAT.CREATION_DEMANDE, StatutAT.DEMANDE_CREEE);
        put(StatutAT.BROUILLON, TypeActionAT.SOUMISSION, StatutAT.SOUMISE);
        put(StatutAT.BROUILLON, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);
        put(StatutAT.BROUILLON, TypeActionAT.REDACTION_AT, StatutAT.AT_REDIGEE);
        put(StatutAT.BROUILLON, TypeActionAT.VALIDATION, StatutAT.VALIDEE);

        put(StatutAT.SOUMISE, TypeActionAT.VALIDATION, StatutAT.VALIDEE);
        put(StatutAT.SOUMISE, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);
        put(StatutAT.SOUMISE, TypeActionAT.REDACTION_AT, StatutAT.AT_REDIGEE);
        put(StatutAT.SOUMISE, TypeActionAT.REFUS, StatutAT.REJETEE);

        put(StatutAT.VALIDEE, TypeActionAT.VALIDATION, StatutAT.VALIDEE);
        put(StatutAT.VALIDEE, TypeActionAT.DEBUT_INTERVENTION, StatutAT.INTERVENTION_EN_COURS);
        put(StatutAT.VALIDEE, TypeActionAT.RECONDUCTION, StatutAT.AT_RECONDUITE);
        put(StatutAT.VALIDEE, TypeActionAT.REDACTION_AT, StatutAT.AT_REDIGEE);
        put(StatutAT.VALIDEE, TypeActionAT.CLOTURE, StatutAT.TRAVAUX_RECEPTIONES);

        put(StatutAT.RENOUVELEE, TypeActionAT.DEBUT_INTERVENTION, StatutAT.INTERVENTION_EN_COURS);
        put(StatutAT.RENOUVELEE, TypeActionAT.DECLARATION_FIN, StatutAT.FIN_TRAVAUX_DECLAREE);

        put(StatutAT.CLOTUREE, TypeActionAT.ARCHIVAGE_OFFICIEL, StatutAT.ARCHIVEE);
        put(StatutAT.CLOTUREE, TypeActionAT.RECEPTION_CONJOINTE, StatutAT.TRAVAUX_RECEPTIONES);

        put(StatutAT.REJETEE, TypeActionAT.MODIFICATION, StatutAT.DEMANDE_CREEE);
        put(StatutAT.REJETEE, TypeActionAT.VISITE_CHANTIER, StatutAT.VISITE_REALISEE);
    }

    private static void put(StatutAT from, TypeActionAT action, StatutAT to) {
        TRANSITIONS.computeIfAbsent(from, k -> new EnumMap<>(TypeActionAT.class)).put(action, to);
    }

    public StatutAT statutEffectif(AutorisationTravail at) {
        if (at.getStatutWorkflow() != null) {
            return at.getStatutWorkflow();
        }
        return at.getStatut();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowAT verifierTransition(StatutAT etatDepart, TypeActionAT action) {
        log.debug("Vérification transition: {} via {}", etatDepart, action);

        Map<TypeActionAT, StatutAT> actions = TRANSITIONS.get(etatDepart);
        if (actions != null && actions.containsKey(action)) {
            StatutAT arrivee = actions.get(action);
            Optional<WorkflowAT> db = workflowRepository.findActiveTransition(etatDepart, action);
            if (db.isPresent()) {
                return db.get();
            }
            return WorkflowAT.builder()
                    .etatDepart(etatDepart)
                    .etatArrivee(arrivee)
                    .action(action)
                    .actif(true)
                    .validationObligatoire(false)
                    .build();
        }

        return workflowRepository.findActiveTransition(etatDepart, action)
                .orElseThrow(() -> new BusinessException(
                        "Transition non autorisée (S-HSE-SEC-31) : de " + etatDepart + " via " + action));
    }

    public StatutAT resoudreEtatArrivee(StatutAT etatDepart, TypeActionAT action) {
        Map<TypeActionAT, StatutAT> actions = TRANSITIONS.get(etatDepart);
        if (actions != null && actions.containsKey(action)) {
            return actions.get(action);
        }
        return workflowRepository.findActiveTransition(etatDepart, action)
                .map(WorkflowAT::getEtatArrivee)
                .orElseThrow(() -> new BusinessException(
                        "Transition non autorisée : de " + etatDepart + " via " + action));
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierRole(WorkflowAT workflow) {
        if (workflow.getRoleAutorise() == null || workflow.getRoleAutorise().isEmpty()) {
            return;
        }
        String userId = SecurityUtils.getCurrentUtilisateurId()
                .orElseThrow(() -> new BusinessException("Non authentifié"));
        var currentUser = utilisateurRepository.findByEmail(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        boolean ok = currentUser.getRoles().stream().anyMatch(r -> {
            if (r.getNom().equals(workflow.getRoleAutorise())) {
                return true;
            }
            return r.getPermissions() != null && r.getPermissions().stream()
                    .anyMatch(p -> workflow.getRoleAutorise().equals(p.getNom()));
        });
        if (!ok) {
            throw new ForbiddenException(
                    "Permission/rôle requis pour cette action : " + workflow.getRoleAutorise());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StatutAT obtenirEtatSuivant(String atId, StatutAT etatActuel) {
        log.info("Calcul état suivant AT {} depuis {}", atId, etatActuel);

        List<Visa> visas = visaRepository.findByAutorisationTravailId(atId);
        long visasRequis = visas.stream().filter(v -> v.getOrdre() != null).count();
        long visasValides = visas.stream()
                .filter(v -> v.getStatut() == StatutVisa.VALIDE || v.getStatut() == StatutVisa.VALIDATION)
                .count();

        Map<TypeActionAT, StatutAT> possibles = TRANSITIONS.get(etatActuel);
        if (possibles == null || possibles.isEmpty()) {
            return etatActuel;
        }

        if (visasRequis > 0 && visasValides >= visasRequis) {
            if (possibles.containsKey(TypeActionAT.REDACTION_AT)) {
                return possibles.get(TypeActionAT.REDACTION_AT);
            }
            if (possibles.containsKey(TypeActionAT.DEBUT_INTERVENTION)) {
                return possibles.get(TypeActionAT.DEBUT_INTERVENTION);
            }
        }

        return etatActuel;
    }

    public static final List<StatutAT> ORDRE_STANDARD = List.of(
            StatutAT.CLASSIFICATION_EFFECTUEE,
            StatutAT.DEMANDE_CREEE,
            StatutAT.VISITE_REALISEE,
            StatutAT.AT_REDIGEE,
            StatutAT.INTERVENTION_EN_COURS,
            StatutAT.AT_RECONDUITE,
            StatutAT.FIN_TRAVAUX_DECLAREE,
            StatutAT.TRAVAUX_RECEPTIONES,
            StatutAT.ARCHIVEE
    );

    public static final Set<StatutAT> STATUTS_TERMINAUX = Set.of(
            StatutAT.ARCHIVEE, StatutAT.REJETEE, StatutAT.ANNULEE
    );
}
