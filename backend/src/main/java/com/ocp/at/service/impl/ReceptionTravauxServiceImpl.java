package com.ocp.at.service.impl;

import com.ocp.at.dto.request.EssaiRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.EssaiResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.EssaiMapper;
import com.ocp.at.mapper.ReceptionTravauxMapper;
import com.ocp.at.mapper.RemiseEtatMapper;
import com.ocp.at.repository.*;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.AuditService;
import com.ocp.at.service.NotificationService;
import com.ocp.at.service.ReceptionTravauxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceptionTravauxServiceImpl implements ReceptionTravauxService {

    private final ReceptionTravauxRepository receptionRepository;
    private final EssaiRepository essaiRepository;
    private final RemiseEtatRepository remiseEtatRepository;
    private final AutorisationTravailRepository atRepository;
    private final HistoriqueATRepository historiqueRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final NotificationService notificationService;
    private final AuditService auditService;

    private final ReceptionTravauxMapper receptionMapper;
    private final EssaiMapper essaiMapper;
    private final RemiseEtatMapper remiseEtatMapper;

    // =================================================================
    // CREATE
    // =================================================================

    @Override
    @Transactional
    public ReceptionTravauxResponse create(ReceptionTravauxRequest request) {
        // 1. Vérifier que l'AT existe et est VALIDEE
        AutorisationTravail at = atRepository.findById(request.getAutorisationTravailId())
                .orElseThrow(() -> new ResourceNotFoundException("AutorisationTravail", "id", request.getAutorisationTravailId()));

        if (at.getStatut() != StatutAT.VALIDEE) {
            throw new BusinessException(
                    "Une réception ne peut être créée que pour une AT en statut VALIDÉE. Statut actuel : " + at.getStatut()
            );
        }

        // 2. Vérifier qu'il n'existe pas déjà une réception pour cette AT
        if (receptionRepository.existsByAutorisationTravailId(at.getId())) {
            throw new BusinessException("Une réception des travaux existe déjà pour l'AT " + at.getNumero());
        }

        // 3. Construire l'entité
        ReceptionTravaux reception = receptionMapper.toEntity(request);
        reception.setAutorisationTravail(at);
        reception.setDateReception(request.getDateReception() != null ? request.getDateReception() : LocalDateTime.now());
        reception.setCreatedBy(getCurrentMatricule());

        // 4. Essais initiaux
        if (request.getEssais() != null) {
            List<Essai> essais = request.getEssais().stream().map(req -> {
                Essai e = essaiMapper.toEntity(req);
                e.setReceptionTravaux(reception);
                return e;
            }).toList();
            reception.getEssais().addAll(essais);
        }

        // 5. Remise en état initiale
        if (request.getRemiseEtat() != null) {
            RemiseEtat remise = remiseEtatMapper.toEntity(request.getRemiseEtat());
            remise.setReceptionTravaux(reception);
            reception.setRemiseEtat(remise);
        }

        ReceptionTravaux saved = receptionRepository.save(reception);

        // 6. Historique
        enregistrerHistorique(saved, TypeActionAT.RECEPTION_TRAVAUX, at.getStatut(), at.getStatut(),
                "Réception des travaux créée");

        // 7. Notifications
        notifierParticipants(at, "Réception des travaux créée",
                "La réception des travaux pour l'AT " + at.getNumero() + " a été créée.", "INFO");

        // 8. Audit
        logAudit("CREATION_RECEPTION", "SUCCES");

        log.info("Réception créée pour AT {} par {}", at.getNumero(), getCurrentMatricule());
        return receptionMapper.toResponse(saved);
    }

    // =================================================================
    // UPDATE
    // =================================================================

    @Override
    @Transactional
    public ReceptionTravauxResponse update(String id, ReceptionTravauxRequest request) {
        ReceptionTravaux reception = getEntityById(id);

        if (Boolean.TRUE.equals(reception.getValidee())) {
            throw new BusinessException("Impossible de modifier une réception déjà validée.");
        }

        receptionMapper.updateFromRequest(request, reception);

        // Mise à jour des essais : on remplace si la liste est fournie
        if (request.getEssais() != null) {
            reception.getEssais().clear();
            List<Essai> essais = request.getEssais().stream().map(req -> {
                Essai e = essaiMapper.toEntity(req);
                e.setReceptionTravaux(reception);
                return e;
            }).toList();
            reception.getEssais().addAll(essais);
        }

        // Mise à jour de la remise en état
        if (request.getRemiseEtat() != null) {
            if (reception.getRemiseEtat() != null) {
                remiseEtatMapper.updateFromRequest(request.getRemiseEtat(), reception.getRemiseEtat());
            } else {
                RemiseEtat remise = remiseEtatMapper.toEntity(request.getRemiseEtat());
                remise.setReceptionTravaux(reception);
                reception.setRemiseEtat(remise);
            }
        }

        ReceptionTravaux saved = receptionRepository.save(reception);
        logAudit("MODIFICATION_RECEPTION", "SUCCES");
        return receptionMapper.toResponse(saved);
    }

    // =================================================================
    // READ
    // =================================================================

    @Override
    @Transactional(readOnly = true)
    public ReceptionTravauxResponse getById(String id) {
        return receptionMapper.toResponse(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionTravauxResponse getByAutorisationTravailId(String atId) {
        ReceptionTravaux reception = receptionRepository.findByAutorisationTravailId(atId)
                .orElseThrow(() -> new ResourceNotFoundException("ReceptionTravaux", "autorisationTravailId", atId));
        return receptionMapper.toResponse(reception);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceptionTravauxResponse> getAll(Pageable pageable) {
        return receptionRepository.findAll(pageable).map(receptionMapper::toResponse);
    }

    // =================================================================
    // DELETE
    // =================================================================

    @Override
    @Transactional
    public void delete(String id) {
        ReceptionTravaux reception = getEntityById(id);
        if (Boolean.TRUE.equals(reception.getValidee())) {
            throw new BusinessException("Impossible de supprimer une réception déjà validée.");
        }
        receptionRepository.delete(reception);
        logAudit("SUPPRESSION_RECEPTION", "SUCCES");
        log.info("Réception {} supprimée.", id);
    }

    // =================================================================
    // ESSAIS
    // =================================================================

    @Override
    @Transactional
    public EssaiResponse ajouterEssai(String receptionId, EssaiRequest request) {
        ReceptionTravaux reception = getEntityById(receptionId);

        if (Boolean.TRUE.equals(reception.getValidee())) {
            throw new BusinessException("Impossible d'ajouter un essai à une réception déjà validée.");
        }

        Essai essai = essaiMapper.toEntity(request);
        essai.setReceptionTravaux(reception);
        Essai saved = essaiRepository.save(essai);
        return essaiMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EssaiResponse modifierEssai(String receptionId, String essaiId, EssaiRequest request) {
        ReceptionTravaux reception = getEntityById(receptionId);

        if (Boolean.TRUE.equals(reception.getValidee())) {
            throw new BusinessException("Impossible de modifier un essai d'une réception déjà validée.");
        }

        Essai essai = essaiRepository.findById(essaiId)
                .orElseThrow(() -> new ResourceNotFoundException("Essai", "id", essaiId));

        if (!essai.getReceptionTravaux().getId().equals(receptionId)) {
            throw new BusinessException("Cet essai n'appartient pas à la réception spécifiée.");
        }

        essaiMapper.updateFromRequest(request, essai);
        return essaiMapper.toResponse(essaiRepository.save(essai));
    }

    @Override
    @Transactional
    public void supprimerEssai(String receptionId, String essaiId) {
        ReceptionTravaux reception = getEntityById(receptionId);

        if (Boolean.TRUE.equals(reception.getValidee())) {
            throw new BusinessException("Impossible de supprimer un essai d'une réception déjà validée.");
        }

        Essai essai = essaiRepository.findById(essaiId)
                .orElseThrow(() -> new ResourceNotFoundException("Essai", "id", essaiId));

        if (!essai.getReceptionTravaux().getId().equals(receptionId)) {
            throw new BusinessException("Cet essai n'appartient pas à la réception spécifiée.");
        }

        essaiRepository.delete(essai);
    }

    // =================================================================
    // VALIDATION
    // =================================================================

    @Override
    @Transactional
    public ReceptionTravauxResponse validerReception(String id) {
        ReceptionTravaux reception = getEntityById(id);

        if (Boolean.TRUE.equals(reception.getValidee())) {
            throw new BusinessException("Cette réception est déjà validée.");
        }

        // Règle 1 : travaux conformes
        if (!Boolean.TRUE.equals(reception.getTravauxConformes())) {
            throw new BusinessException("Validation impossible : les travaux ne sont pas déclarés conformes.");
        }

        // Règle 2 : installation remise en état
        if (!Boolean.TRUE.equals(reception.getInstallationRemiseEnEtat())) {
            throw new BusinessException("Validation impossible : l'installation n'est pas remise en état.");
        }

        // Règle 3 : essais effectués
        if (!Boolean.TRUE.equals(reception.getEssaisEffectues())) {
            throw new BusinessException("Validation impossible : les essais n'ont pas été effectués.");
        }

        // Règle 4 : tous les essais doivent être conformes
        if (essaiRepository.existsByReceptionTravauxIdAndConformeIsFalse(id)) {
            throw new BusinessException("Validation impossible : un ou plusieurs essais ne sont pas conformes.");
        }

        // Validation
        reception.setValidee(true);
        reception.setDateValidation(LocalDateTime.now());
        reception.setEssaisConformes(true);

        ReceptionTravaux saved = receptionRepository.save(reception);

        AutorisationTravail at = saved.getAutorisationTravail();

        // Historique
        enregistrerHistorique(saved, TypeActionAT.VALIDATION_RECEPTION, at.getStatut(), at.getStatut(),
                "Réception des travaux validée — AT prête pour clôture");

        // Notifications
        notifierParticipants(at, "Réception validée",
                "La réception des travaux pour l'AT " + at.getNumero() +
                        " est validée. L'AT peut maintenant être clôturée.", "SUCCESS");

        // Audit
        logAudit("VALIDATION_RECEPTION", "SUCCES");

        log.info("Réception {} validée par {}", id, getCurrentMatricule());
        return receptionMapper.toResponse(saved);
    }

    // =================================================================
    // MÉTHODES PRIVÉES
    // =================================================================

    private ReceptionTravaux getEntityById(String id) {
        return receptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceptionTravaux", "id", id));
    }

    private String getCurrentMatricule() {
        return SecurityUtils.getCurrentUtilisateurId()
                .flatMap(utilisateurRepository::findById)
                .map(Utilisateur::getMatricule)
                .orElse("SYSTEM");
    }

    private Utilisateur getCurrentUser() {
        return SecurityUtils.getCurrentUtilisateurId()
                .flatMap(utilisateurRepository::findById)
                .orElse(null);
    }

    private void enregistrerHistorique(ReceptionTravaux reception, TypeActionAT action,
                                       StatutAT ancienStatut, StatutAT nouveauStatut, String commentaire) {
        Utilisateur currentUser = getCurrentUser();
        HistoriqueAT h = HistoriqueAT.builder()
                .autorisationTravail(reception.getAutorisationTravail())
                .dateAction(LocalDateTime.now())
                .action(action)
                .ancienStatut(ancienStatut)
                .nouveauStatut(nouveauStatut)
                .commentaire(commentaire)
                .utilisateur(currentUser)
                .build();
        historiqueRepository.save(h);
    }

    private void notifierParticipants(AutorisationTravail at, String titre, String message, String type) {
        // Notifier le propriétaire (demandeur)
        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(
                    at.getProprietaireBrouillon(), titre, message, type, "/at/" + at.getId());
        }
        // Notifier les responsables OCP et entreprise par rôle
        notificationService.sendNotificationToRole("RESPONSABLE_OCP", titre, message, type, "/at/" + at.getId());
        notificationService.sendNotificationToRole("RESPONSABLE_ENTREPRISE", titre, message, type, "/at/" + at.getId());
    }

    private void logAudit(String action, String resultat) {
        Utilisateur user = getCurrentUser();
        auditService.logAction(action, resultat, user, "N/A", "System");
    }
}
