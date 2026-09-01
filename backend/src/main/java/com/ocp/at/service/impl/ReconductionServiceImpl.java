package com.ocp.at.service.impl;

import com.ocp.at.dto.request.DecisionReconductionRequest;
import com.ocp.at.dto.request.DemandeReconductionRequest;
import com.ocp.at.dto.response.ReconductionResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.HistoriqueAT;
import com.ocp.at.entity.Reconduction;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.StatutReconduction;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.ReconductionMapper;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.HistoriqueATRepository;
import com.ocp.at.repository.ReconductionRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.AuditService;
import com.ocp.at.service.NotificationService;
import com.ocp.at.service.ReconductionService;
import com.ocp.at.service.WorkflowATService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconductionServiceImpl implements ReconductionService {

    private final ReconductionRepository reconductionRepository;
    private final AutorisationTravailRepository atRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueATRepository historiqueRepository;
    private final ReconductionMapper reconductionMapper;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final WorkflowATService workflowService;

    @Override
    @Transactional
    public ReconductionResponse demanderReconduction(String atId, DemandeReconductionRequest request) {
        Utilisateur currentUser = getCurrentUser();
        AutorisationTravail at = atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("Autorisation de travail non trouvée avec l'ID : " + atId));

        // 1. Contrôle de statut : l'AT doit être en cours d'intervention ou déjà reconduite
        StatutAT st = at.getStatutWorkflow() != null ? at.getStatutWorkflow() : at.getStatut();
        if (st != StatutAT.INTERVENTION_EN_COURS && st != StatutAT.EN_COURS && st != StatutAT.AT_RECONDUITE && st != StatutAT.RENOUVELEE) {
            throw new BusinessException("Une demande de reconduction ne peut être formulée que pour une AT en cours d'intervention. Statut actuel : " + st);
        }

        // 2. Contrôle anti-doublon : pas d'autre demande en attente
        if (reconductionRepository.existsByAutorisationTravailIdAndStatut(at.getId(), StatutReconduction.REQUESTED)) {
            throw new BusinessException("Une demande de reconduction est déjà en attente de décision pour cette AT.");
        }

        // 3. Contrôle des dates : la nouvelle date de fin doit être future
        LocalDateTime now = LocalDateTime.now();
        if (request.getNouvelleDateFin().isBefore(now)) {
            throw new BusinessException("La nouvelle date et heure de fin demandée doit être postérieure à la date et heure actuelle.");
        }

        LocalDateTime dateFinActuelle = at.getDateFin() != null
                ? LocalDateTime.of(at.getDateFin(), at.getHeureFin() != null ? at.getHeureFin() : LocalTime.of(18, 0))
                : now;

        if (request.getNouvelleDateFin().isBefore(dateFinActuelle) || request.getNouvelleDateFin().isEqual(dateFinActuelle)) {
            throw new BusinessException("La nouvelle échéance demandée doit être strictement postérieure à l'échéance actuelle (" + dateFinActuelle + ").");
        }

        // 4. Analyse IA consultative préliminaire (aide à la décision HMEP)
        long nbReconductionsPrecedentes = reconductionRepository.countByAutorisationTravailIdAndStatut(at.getId(), StatutReconduction.APPROVED);
        String riskLevel = nbReconductionsPrecedentes >= 2 ? "HIGH" : (nbReconductionsPrecedentes == 1 ? "MEDIUM" : "LOW");
        String analyseJson = String.format(
                "{\"riskLevel\":\"%s\",\"previousExtensions\":%d,\"alerts\":[%s],\"recommendation\":\"%s\"}",
                riskLevel,
                nbReconductionsPrecedentes,
                nbReconductionsPrecedentes > 0 ? "\"Intervention ayant déjà fait l'objet de " + nbReconductionsPrecedentes + " reconduction(s)\"" : "\"Première demande de reconduction\"",
                nbReconductionsPrecedentes >= 2 ? "Réévaluation complète des conditions de sécurité recommandée par le HMEP." : "Conditions standard de reconduction de poste."
        );

        // 5. Créer l'entité Reconduction
        Reconduction reconduction = Reconduction.builder()
                .autorisationTravail(at)
                .demandeur(currentUser)
                .dateDemande(now)
                .dateFinInitiale(dateFinActuelle)
                .nouvelleDateFin(request.getNouvelleDateFin())
                .motif(request.getMotif())
                .commentaire(request.getCommentaire())
                .statut(StatutReconduction.REQUESTED)
                .analyseIaJson(analyseJson)
                .build();

        Reconduction saved = reconductionRepository.save(reconduction);

        // 6. Historique & Notifications
        HistoriqueAT histo = HistoriqueAT.builder()
                .autorisationTravail(at)
                .utilisateur(currentUser)
                .action(TypeActionAT.DEMANDE_RECONDUCTION)
                .ancienStatut(st)
                .nouveauStatut(st)
                .commentaire("Demande de reconduction jusqu'au " + request.getNouvelleDateFin() + " par " + currentUser.getNom() + " (Motif: " + request.getMotif() + ")")
                .dateAction(now)
                .build();
        historiqueRepository.save(histo);

        // Notification envoyée au HMEP (Responsable OCP)
        notificationService.sendNotificationToRoleForAt(
                "HMEP", at,
                "Demande de reconduction AT " + at.getNumero(),
                "Le CEEE " + currentUser.getPrenom() + " " + currentUser.getNom() + " demande une reconduction jusqu'au " + request.getNouvelleDateFin() + " pour l'AT " + at.getNumero() + ".",
                "ACTION",
                "/autorisations/" + at.getId()
        );

        logAudit("DEMANDE_RECONDUCTION", "SUCCES");
        log.info("Demande de reconduction créée pour AT {} par {}", at.getNumero(), currentUser.getEmail());

        return reconductionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReconductionResponse deciderReconduction(String reconductionId, DecisionReconductionRequest request) {
        Utilisateur hmep = getCurrentUser();
        Reconduction reconduction = reconductionRepository.findById(reconductionId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de reconduction non trouvée avec l'ID : " + reconductionId));

        if (reconduction.getStatut() != StatutReconduction.REQUESTED) {
            throw new BusinessException("Cette demande de reconduction a déjà été traitée (statut actuel : " + reconduction.getStatut() + ").");
        }

        AutorisationTravail at = reconduction.getAutorisationTravail();
        LocalDateTime now = LocalDateTime.now();

        if (Boolean.TRUE.equals(request.getApprouve())) {
            // APPROBATION par le HMEP (Responsable OCP)
            reconduction.setStatut(StatutReconduction.APPROVED);
            reconduction.setDecisionPar(hmep);
            reconduction.setDateDecision(now);
            reconduction.setCommentaire(request.getCommentaire());

            // Mise à jour de l'échéance sur l'AT et incrément de version
            LocalDate nouvelleDate = reconduction.getNouvelleDateFin().toLocalDate();
            LocalTime nouvelleHeure = reconduction.getNouvelleDateFin().toLocalTime();
            at.setDateFin(nouvelleDate);
            at.setHeureFin(nouvelleHeure);
            at.setVersion(at.getVersion() == null ? 2 : at.getVersion() + 1);

            StatutAT ancienStatut = at.getStatutWorkflow() != null ? at.getStatutWorkflow() : at.getStatut();
            at.setStatutWorkflow(StatutAT.AT_RECONDUITE);
            at.setStatut(StatutAT.RENOUVELEE);
            atRepository.save(at);

            // Historique
            HistoriqueAT histo = HistoriqueAT.builder()
                    .autorisationTravail(at)
                    .utilisateur(hmep)
                    .action(TypeActionAT.APPROBATION_RECONDUCTION)
                    .ancienStatut(ancienStatut)
                    .nouveauStatut(StatutAT.AT_RECONDUITE)
                    .commentaire("Reconduction approuvée par le HMEP " + hmep.getNom() + " jusqu'au " + reconduction.getNouvelleDateFin() + " (Version " + at.getVersion() + ")")
                    .dateAction(now)
                    .build();
            historiqueRepository.save(histo);

            // Notifier le demandeur CEEE
            if (reconduction.getDemandeur() != null) {
                notificationService.createNotification(
                        reconduction.getDemandeur(),
                        "Reconduction approuvée - AT " + at.getNumero(),
                        "Votre demande de reconduction a été validée par le Responsable OCP (HMEP) jusqu'au " + reconduction.getNouvelleDateFin() + ".",
                        "INFO",
                        "/autorisations/" + at.getId()
                );
            }

            logAudit("APPROBATION_RECONDUCTION", "SUCCES");
            log.info("Reconduction approuvée pour AT {} par HMEP {}", at.getNumero(), hmep.getEmail());

        } else {
            // REFUS par le HMEP (motif obligatoire)
            if (request.getMotifRefus() == null || request.getMotifRefus().isBlank()) {
                throw new BusinessException("Le motif de refus est obligatoire pour rejeter une demande de reconduction.");
            }

            reconduction.setStatut(StatutReconduction.REJECTED);
            reconduction.setDecisionPar(hmep);
            reconduction.setDateDecision(now);
            reconduction.setMotifRefus(request.getMotifRefus());
            reconduction.setCommentaire(request.getCommentaire());

            StatutAT statutActuel = at.getStatutWorkflow() != null ? at.getStatutWorkflow() : at.getStatut();
            HistoriqueAT histo = HistoriqueAT.builder()
                    .autorisationTravail(at)
                    .utilisateur(hmep)
                    .action(TypeActionAT.REFUS_RECONDUCTION)
                    .ancienStatut(statutActuel)
                    .nouveauStatut(statutActuel)
                    .commentaire("Reconduction refusée par le HMEP " + hmep.getNom() + " (Motif: " + request.getMotifRefus() + ")")
                    .dateAction(now)
                    .build();
            historiqueRepository.save(histo);

            // Notifier le demandeur CEEE
            if (reconduction.getDemandeur() != null) {
                notificationService.createNotification(
                        reconduction.getDemandeur(),
                        "Reconduction refusée - AT " + at.getNumero(),
                        "Votre demande de reconduction pour l'AT " + at.getNumero() + " a été refusée. Motif : " + request.getMotifRefus(),
                        "WARNING",
                        "/autorisations/" + at.getId()
                );
            }

            logAudit("REFUS_RECONDUCTION", "SUCCES");
            log.info("Reconduction refusée pour AT {} par HMEP {}", at.getNumero(), hmep.getEmail());
        }

        Reconduction saved = reconductionRepository.save(reconduction);
        return reconductionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconductionResponse> getReconductionsByAtId(String atId) {
        return reconductionRepository.findByAutorisationTravailIdOrderByDateDemandeDesc(atId)
                .stream()
                .map(reconductionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconductionResponse> getPendingReconductions() {
        return reconductionRepository.findByStatutOrderByDateDemandeDesc(StatutReconduction.REQUESTED)
                .stream()
                .map(reconductionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReconductionResponse getById(String id) {
        Reconduction r = reconductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de reconduction non trouvée avec l'ID : " + id));
        return reconductionMapper.toResponse(r);
    }

    private void logAudit(String action, String resultat) {
        Utilisateur user = getCurrentUser();
        auditService.logAction(action, resultat, user, "N/A", "System");
    }

    private Utilisateur getCurrentUser() {
        String currentUserId = SecurityUtils.getCurrentUtilisateurId()
                .orElseThrow(() -> new BusinessException("Utilisateur non authentifié"));
        return utilisateurRepository.findById(currentUserId)
                .orElseGet(() -> utilisateurRepository.findByEmail(currentUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID/email : " + currentUserId)));
    }
}
