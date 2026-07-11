package com.ocp.at.service.impl;

import com.ocp.at.dto.request.PhotoReceptionRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PhotoReceptionMapper;
import com.ocp.at.mapper.ReceptionTravauxMapper;
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
    private final PhotoReceptionRepository photoRepository;
    private final HistoriqueReceptionRepository historiqueReceptionRepository;
    private final AutorisationTravailRepository atRepository;
    private final HistoriqueATRepository historiqueRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VisaRepository visaRepository;
    private final PermisRepository permisRepository;

    private final NotificationService notificationService;
    private final AuditService auditService;

    private final ReceptionTravauxMapper receptionMapper;
    private final PhotoReceptionMapper photoMapper;

    // =================================================================
    // CREATE
    // =================================================================

    @Override
    @Transactional
    public ReceptionTravauxResponse create(ReceptionTravauxRequest request) {
        // 1. Vérifier que l'AT existe et est VALIDEE
        AutorisationTravail at = atRepository.findById(request.getAutorisationTravailId())
                .orElseThrow(() -> new ResourceNotFoundException("AutorisationTravail non trouvée avec l'ID : " + request.getAutorisationTravailId()));

        if (at.getStatut() != StatutAT.VALIDEE) {
            throw new BusinessException(
                    "Une réception ne peut être créée que pour une AT en statut VALIDÉE. Statut actuel : " + at.getStatut()
            );
        }

        // 2. Vérifier que tous les visas sont validés
        if (!visaRepository.existsByAutorisationTravailIdAndStatut(at.getId(), com.ocp.at.entity.enums.StatutVisa.VALIDE)) {
            throw new BusinessException("Tous les visas doivent être validés avant de créer une réception.");
        }

        // 3. Vérifier que tous les permis sont conformes
        if (!permisRepository.existsByAutorisationTravailId(at.getId())) {
            throw new BusinessException("Au moins un permis est requis pour créer une réception.");
        }

        // 4. Vérifier qu'il n'existe pas déjà une réception pour cette AT
        if (receptionRepository.existsByAutorisationTravailId(at.getId())) {
            throw new BusinessException("Une réception des travaux existe déjà pour l'AT " + at.getNumero());
        }

        // 5. Construire l'entité
        ReceptionTravaux reception = receptionMapper.toEntity(request);
        reception.setAutorisationTravail(at);
        reception.setDateReception(request.getDateReception() != null ? request.getDateReception() : LocalDateTime.now());

        // 6. Responsable
        if (request.getResponsableId() != null) {
            Utilisateur responsable = utilisateurRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + request.getResponsableId()));
            reception.setResponsable(responsable);
        }

        ReceptionTravaux saved = receptionRepository.save(reception);

        // 7. Historique réception
        enregistrerHistoriqueReception(saved, "CREATION", "Réception des travaux créée");

        // 8. Historique AT
        enregistrerHistoriqueAT(saved, TypeActionAT.RECEPTION_TRAVAUX, at.getStatut(), at.getStatut(),
                "Réception des travaux créée");

        // 9. Notifications
        notifierParticipants(at, "Réception des travaux créée",
                "La réception des travaux pour l'AT " + at.getNumero() + " a été créée.", "INFO");

        // 10. Audit
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

        if (isATCloturee(reception)) {
            throw new BusinessException("Impossible de modifier une réception d'une AT clôturée.");
        }

        receptionMapper.updateFromRequest(request, reception);

        // Mise à jour du responsable si fourni
        if (request.getResponsableId() != null) {
            Utilisateur responsable = utilisateurRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + request.getResponsableId()));
            reception.setResponsable(responsable);
        }

        ReceptionTravaux saved = receptionRepository.save(reception);
        enregistrerHistoriqueReception(saved, "MODIFICATION", "Réception modifiée");
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
                .orElseThrow(() -> new ResourceNotFoundException("ReceptionTravaux non trouvée pour l'AT ID : " + atId));
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
        if (isATCloturee(reception)) {
            throw new BusinessException("Impossible de supprimer une réception d'une AT clôturée.");
        }
        receptionRepository.delete(reception);
        logAudit("SUPPRESSION_RECEPTION", "SUCCES");
        log.info("Réception {} supprimée.", id);
    }

    // =================================================================
    // SIGNATURE
    // =================================================================

    @Override
    @Transactional
    public ReceptionTravauxResponse signer(String id, String signaturePath) {
        ReceptionTravaux reception = getEntityById(id);

        if (isATCloturee(reception)) {
            throw new BusinessException("Impossible de signer une réception d'une AT clôturée.");
        }

        reception.setSignatureResponsable(signaturePath);
        reception.setDateSignature(LocalDateTime.now());

        ReceptionTravaux saved = receptionRepository.save(reception);
        enregistrerHistoriqueReception(saved, "SIGNATURE", "Réception signée par le responsable");
        logAudit("SIGNATURE_RECEPTION", "SUCCES");

        log.info("Réception {} signée par {}", id, getCurrentMatricule());
        return receptionMapper.toResponse(saved);
    }

    // =================================================================
    // CLOTURE AT
    // =================================================================

    @Override
    @Transactional
    public ReceptionTravauxResponse cloturerAT(String id) {
        ReceptionTravaux reception = getEntityById(id);
        AutorisationTravail at = reception.getAutorisationTravail();

        if (isATCloturee(reception)) {
            throw new BusinessException("L'AT est déjà clôturée.");
        }

        // Vérifications métier
        if (!Boolean.TRUE.equals(reception.getTravauxConformes())) {
            throw new BusinessException("Clôture impossible : les travaux ne sont pas conformes.");
        }

        if (!Boolean.TRUE.equals(reception.getZoneNettoyee())) {
            throw new BusinessException("Clôture impossible : la zone n'est pas nettoyée.");
        }

        if (!Boolean.TRUE.equals(reception.getConsignationRetiree())) {
            throw new BusinessException("Clôture impossible : la consignation n'est pas retirée.");
        }

        if (!Boolean.TRUE.equals(reception.getEquipementRemisEnService())) {
            throw new BusinessException("Clôture impossible : l'équipement n'est pas remis en service.");
        }

        if (!Boolean.TRUE.equals(reception.getEssaisEffectues())) {
            throw new BusinessException("Clôture impossible : les essais n'ont pas été effectués.");
        }

        if (reception.getSignatureResponsable() == null) {
            throw new BusinessException("Clôture impossible : la signature du responsable est obligatoire.");
        }

        // Clôture de l'AT
        at.setStatut(StatutAT.CLOTUREE);
        atRepository.save(at);

        // Historique
        enregistrerHistoriqueReception(reception, "CLOTURE", "AT clôturée suite à réception validée");
        enregistrerHistoriqueAT(reception, TypeActionAT.CLOTURE, StatutAT.VALIDEE, StatutAT.CLOTUREE,
                "AT clôturée suite à réception des travaux");

        // Notifications
        notifierParticipants(at, "AT Clôturée",
                "L'AT " + at.getNumero() + " a été clôturée suite à la réception des travaux.", "SUCCESS");

        // Audit
        logAudit("CLOTURE_AT", "SUCCES");

        log.info("AT {} clôturée suite à réception {}", at.getNumero(), id);
        return receptionMapper.toResponse(reception);
    }

    // =================================================================
    // PHOTOS
    // =================================================================

    @Override
    @Transactional
    public PhotoReceptionResponse ajouterPhoto(String receptionId, PhotoReceptionRequest request) {
        ReceptionTravaux reception = getEntityById(receptionId);

        if (isATCloturee(reception)) {
            throw new BusinessException("Impossible d'ajouter une photo à une réception d'une AT clôturée.");
        }

        PhotoReception photo = photoMapper.toEntity(request);
        photo.setReceptionTravaux(reception);
        PhotoReception saved = photoRepository.save(photo);

        enregistrerHistoriqueReception(reception, "AJOUT_PHOTO", "Photo ajoutée : " + request.getNom());
        logAudit("AJOUT_PHOTO_RECEPTION", "SUCCES");

        return photoMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void supprimerPhoto(String receptionId, String photoId) {
        ReceptionTravaux reception = getEntityById(receptionId);

        if (isATCloturee(reception)) {
            throw new BusinessException("Impossible de supprimer une photo d'une réception d'une AT clôturée.");
        }

        PhotoReception photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("PhotoReception non trouvée avec l'ID : " + photoId));

        if (!photo.getReceptionTravaux().getId().equals(receptionId)) {
            throw new BusinessException("Cette photo n'appartient pas à la réception spécifiée.");
        }

        photoRepository.delete(photo);
        enregistrerHistoriqueReception(reception, "SUPPRESSION_PHOTO", "Photo supprimée : " + photo.getNom());
        logAudit("SUPPRESSION_PHOTO_RECEPTION", "SUCCES");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoReceptionResponse> getPhotos(String receptionId) {
        getEntityById(receptionId); // Vérifie que la réception existe
        return photoRepository.findByReceptionTravauxIdOrderByOrdreAsc(receptionId)
                .stream()
                .map(photoMapper::toResponse)
                .toList();
    }

    // =================================================================
    // MÉTHODES PRIVÉES
    // =================================================================

    private ReceptionTravaux getEntityById(String id) {
        return receptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReceptionTravaux non trouvée avec l'ID : " + id));
    }

    private boolean isATCloturee(ReceptionTravaux reception) {
        return reception.getAutorisationTravail().getStatut() == StatutAT.CLOTUREE;
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

    private void enregistrerHistoriqueReception(ReceptionTravaux reception, String action, String commentaire) {
        Utilisateur currentUser = getCurrentUser();
        HistoriqueReception h = HistoriqueReception.builder()
                .receptionTravaux(reception)
                .dateAction(LocalDateTime.now())
                .action(action)
                .commentaire(commentaire)
                .utilisateur(currentUser)
                .build();
        historiqueReceptionRepository.save(h);
    }

    private void enregistrerHistoriqueAT(ReceptionTravaux reception, TypeActionAT action,
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
        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(
                    at.getProprietaireBrouillon(), titre, message, type, "/at/" + at.getId());
        }
        notificationService.sendNotificationToRole("RESPONSABLE_OCP", titre, message, type, "/at/" + at.getId());
        notificationService.sendNotificationToRole("RESPONSABLE_ENTREPRISE", titre, message, type, "/at/" + at.getId());
    }

    private void logAudit(String action, String resultat) {
        Utilisateur user = getCurrentUser();
        auditService.logAction(action, resultat, user, "N/A", "System");
    }
}
