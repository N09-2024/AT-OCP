package com.ocp.at.service.impl;

import com.ocp.at.dto.request.PhotoReceptionRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PhotoReceptionMapper;
import com.ocp.at.mapper.ReceptionTravauxMapper;
import com.ocp.at.repository.*;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.AuditService;
import com.ocp.at.service.HashService;
import com.ocp.at.service.NotificationService;
import com.ocp.at.service.ReceptionTravauxService;
import com.ocp.at.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final StorageService storageService;
    private final HashService hashService;

    private final ReceptionTravauxMapper receptionMapper;
    private final PhotoReceptionMapper photoMapper;

    // =================================================================
    // CREATE
    // =================================================================

    @Override
    @Transactional
    public ReceptionTravauxResponse create(ReceptionTravauxRequest request) {
        // 1. Vérifier que l'AT existe et est dans un état prêt pour la réception
        AutorisationTravail at = atRepository.findById(request.getAutorisationTravailId())
                .orElseThrow(() -> new ResourceNotFoundException("AutorisationTravail non trouvée avec l'ID : " + request.getAutorisationTravailId()));

        StatutAT st = at.getStatut();
        StatutAT stW = at.getStatutWorkflow();
        boolean isEligible = st == StatutAT.VALIDEE || st == StatutAT.AT_VALIDEE
                || st == StatutAT.FIN_TRAVAUX_DECLAREE || st == StatutAT.DECLAREE_TERMINEE
                || st == StatutAT.INTERVENTION_EN_COURS || st == StatutAT.TRAVAUX_RECEPTIONES || st == StatutAT.CLOTUREE
                || stW == StatutAT.FIN_TRAVAUX_DECLAREE || stW == StatutAT.DECLAREE_TERMINEE
                || stW == StatutAT.TRAVAUX_RECEPTIONES || stW == StatutAT.RECEPTIONEES
                || stW == StatutAT.INTERVENTION_EN_COURS || stW == StatutAT.AT_VALIDEE;

        if (!isEligible) {
            throw new BusinessException(
                    "Une réception ne peut être créée que pour une AT en phase d'intervention ou de fin de travaux. Statut actuel : " + at.getStatut()
            );
        }

        // 2. Vérifier que tous les visas requis sont validés s'ils existent
        if (!visaRepository.existsByAutorisationTravailIdAndStatut(at.getId(), com.ocp.at.entity.enums.StatutVisa.VALIDE)) {
            log.warn("Création de réception: aucun visa VALIDE explicite pour l'AT {}", at.getId());
        }

        // 3. Vérifier la conformité des permis s'il en existe
        if (permisRepository.existsByAutorisationTravailId(at.getId())
                && permisRepository.existsByAutorisationTravailIdAndStatutVerification(at.getId(), com.ocp.at.entity.enums.StatutPermis.INVALIDE)) {
            throw new BusinessException("Certains permis attachés à cette AT sont non conformes.");
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
        enregistrerHistoriqueReception(saved, "MODIFICATION", "Réception des travaux mise à jour");
        logAudit("MODIFICATION_RECEPTION", "SUCCES");

        log.info("Réception {} mise à jour par {}", id, getCurrentMatricule());
        return receptionMapper.toResponse(saved);
    }

    // =================================================================
    // GET BY ID / GET BY AT / GET ALL
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

    @Override
    @Transactional
    public ReceptionTravauxResponse validerReceptionCeep(String id, com.ocp.at.dto.request.ValidationReceptionCeepRequest request, org.springframework.web.multipart.MultipartFile signatureFile) {
        ReceptionTravaux reception = getEntityById(id);
        AutorisationTravail at = reception.getAutorisationTravail();

        if (isATCloturee(reception)) {
            throw new BusinessException("Impossible d'évaluer la réception d'une AT déjà clôturée.");
        }

        Utilisateur ceep = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        // 1. Mettre à jour les champs de contrôle sur ReceptionTravaux
        reception.setResultatReception(request.getResultat());
        reception.setReservesDescription(request.getReservesDescription());
        reception.setActionsCorrectives(request.getActionsCorrectives());
        reception.setObservations(request.getObservations());
        reception.setTravauxConformes(request.getTravauxConformes());
        reception.setZoneNettoyee(request.getZoneNettoyee());
        reception.setEquipementRemisEnService(request.getEquipementRemisEnService());
        reception.setConsignationRetiree(request.getConsignationRetiree());
        reception.setEssaisEffectues(request.getEssaisEffectues());
        reception.setResultatEssais(request.getResultatEssais());
        reception.setDateReception(now);

        // 2. Traiter la signature CEEP via le système Visa existant (aucun champ parallèle)
        String signaturePath = null;
        if (signatureFile != null && !signatureFile.isEmpty()) {
            try {
                signaturePath = storageService.saveSignatureBytes(signatureFile.getBytes(), "visa-reception-ceep-" + at.getId() + ".png");
            } catch (Exception e) {
                log.error("Erreur lors de la sauvegarde de la signature CEEP", e);
                throw new BusinessException("Impossible d'enregistrer la signature du CEEP : " + e.getMessage());
            }
        }

        // Créer ou mettre à jour le Visa CEEP de réception
        Visa visaCeep = visaRepository.findByAutorisationTravailId(at.getId()).stream()
                .filter(v -> "RECEPTION_CEEP".equalsIgnoreCase(v.getTypeVisa()) || (v.getCommentaire() != null && v.getCommentaire().contains("RECEPTION_CEEP")))
                .findFirst()
                .orElse(null);

        if (visaCeep == null) {
            visaCeep = Visa.builder()
                    .autorisationTravail(at)
                    .utilisateur(ceep)
                    .typeVisa("RECEPTION_CEEP")
                    .ordre(100)
                    .build();
        }

        visaCeep.setStatut(StatutVisa.VALIDE);
        visaCeep.setDateVisa(now);
        visaCeep.setDateSignature(now);
        visaCeep.setCommentaire("Visa Réception Conjointe CEEP : " + request.getResultat());
        if (signaturePath != null && signatureFile != null) {
            try {
                visaCeep.setSignaturePath(signaturePath);
                visaCeep.setSignatureHash(hashService.calculateSHA256(signatureFile.getBytes()));
            } catch (Exception ignored) {}
        }
        visaRepository.save(visaCeep);

        // 3. Statut de validation conjointe
        if (request.getResultat() == com.ocp.at.entity.enums.ResultatReception.CONFORME
                || request.getResultat() == com.ocp.at.entity.enums.ResultatReception.CONFORME_AVEC_RESERVES) {
            reception.setReceptionConjointeValidee(true);
            reception.setValidee(true);
            at.setStatutWorkflow(StatutAT.TRAVAUX_RECEPTIONES);
            atRepository.save(at);

            enregistrerHistoriqueAT(reception, TypeActionAT.RECEPTION_CONJOINTE, at.getStatut(), StatutAT.TRAVAUX_RECEPTIONES,
                    "Réception conjointe validée par le CEEP (" + request.getResultat() + ")");
            enregistrerHistoriqueReception(reception, "RECEPTION_VALIDEE", "Réception conjointe validée par le CEEP (" + request.getResultat() + ")");

            notifierParticipants(at, "Réception conjointe validée",
                    "La réception conjointe pour l'AT " + at.getNumero() + " a été validée par le CEEP (" + request.getResultat() + ").", "SUCCESS");
        } else {
            // NON CONFORME : l'AT reste en FIN_TRAVAUX_DECLAREE ou nécessite des reprises
            reception.setReceptionConjointeValidee(false);
            reception.setValidee(false);

            enregistrerHistoriqueAT(reception, TypeActionAT.RECEPTION_CONJOINTE, at.getStatut(), at.getStatut(),
                    "Réception conjointe refusée (NON_CONFORME) par le CEEP. Réserves : " + request.getReservesDescription());
            enregistrerHistoriqueReception(reception, "RECEPTION_REFUSEE", "Réception non conforme. Réserves : " + request.getReservesDescription());

            notifierParticipants(at, "Réception non conforme",
                    "La réception conjointe pour l'AT " + at.getNumero() + " a été déclarée NON CONFORME par le CEEP.", "WARNING");
        }

        ReceptionTravaux saved = receptionRepository.save(reception);
        logAudit("VALIDATION_RECEPTION_CEEP", "SUCCES");
        log.info("Réception {} évaluée par CEEP {} avec résultat {}", id, ceep.getEmail(), request.getResultat());

        return receptionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public com.ocp.at.dto.response.ClosureReadinessResponse verifierCloture(String atId) {
        AutorisationTravail at = atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("AT non trouvée avec l'ID : " + atId));

        List<String> blockingReasons = new ArrayList<>();
        ReceptionTravaux reception = receptionRepository.findByAutorisationTravailId(at.getId()).orElse(null);

        boolean hasVisaCeee = reception != null && (reception.getSignatureResponsable() != null || reception.getSignatureDate() != null);
        boolean hasVisaCeep = visaRepository.findByAutorisationTravailId(at.getId()).stream()
                .anyMatch(v -> "RECEPTION_CEEP".equalsIgnoreCase(v.getTypeVisa()) && v.getStatut() == StatutVisa.VALIDE);

        // Déclaration de fin : satisfaite si la date est renseignée, le statut workflow adéquat,
        // ou si la réception est déjà signée par le CEEE (flux simplifié sans déclaration explicite)
        boolean hasDeclarationFin = at.getDateFinReelle() != null
                || at.getStatutWorkflow() == StatutAT.FIN_TRAVAUX_DECLAREE
                || at.getStatutWorkflow() == StatutAT.TRAVAUX_RECEPTIONES
                || hasVisaCeee;

        boolean hasReception = reception != null;
        boolean isConforme = reception != null && (Boolean.TRUE.equals(reception.getTravauxConformes())
                || reception.getResultatReception() == com.ocp.at.entity.enums.ResultatReception.CONFORME
                || reception.getResultatReception() == com.ocp.at.entity.enums.ResultatReception.CONFORME_AVEC_RESERVES);

        if (!hasDeclarationFin) {
            blockingReasons.add("La fin des travaux n'a pas encore été déclarée par le CEEE.");
        }
        if (!hasReception) {
            blockingReasons.add("Aucune réception des travaux n'a été créée pour cette AT.");
        } else {
            if (!isConforme) {
                blockingReasons.add("Les travaux ne sont pas déclarés conformes lors de la réception.");
            }
            if (!Boolean.TRUE.equals(reception.getZoneNettoyee())) {
                blockingReasons.add("La zone de travail n'est pas déclarée nettoyée.");
            }
            if (!Boolean.TRUE.equals(reception.getConsignationRetiree())) {
                blockingReasons.add("La consignation n'a pas été retirée.");
            }
            if (!Boolean.TRUE.equals(reception.getEquipementRemisEnService())) {
                blockingReasons.add("L'équipement n'a pas été remis en service / protections non rétablies.");
            }
            if (!hasVisaCeee) {
                blockingReasons.add("Le visa/signature de réception du CEEE est manquant.");
            }
            // Le visa CEEP conjoint est recommandé mais non bloquant si le CEEE a déjà signé
            // et que les travaux sont conformes (flux simplifié S-HSE-SEC-31 §7.3.4)
        }

        return com.ocp.at.dto.response.ClosureReadinessResponse.builder()
                .canClose(blockingReasons.isEmpty())
                .blockingReasons(blockingReasons)
                .atNumero(at.getNumero())
                .hasDeclarationFin(hasDeclarationFin)
                .hasReception(hasReception)
                .isReceptionConforme(isConforme)
                .hasVisaCeee(hasVisaCeee)
                .hasVisaCeep(hasVisaCeep)
                .build();
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

        // Vérification préalable déterministe
        com.ocp.at.dto.response.ClosureReadinessResponse readiness = verifierCloture(at.getId());
        if (!Boolean.TRUE.equals(readiness.getCanClose())) {
            throw new BusinessException("Clôture impossible : " + String.join(", ", readiness.getBlockingReasons()));
        }

        // Clôture de l'AT et synchronisation du statut standard S-HSE-SEC-31
        StatutAT statutAvant = at.getStatut();
        at.setStatut(StatutAT.CLOTUREE);
        at.setStatutWorkflow(StatutAT.TRAVAUX_RECEPTIONES);
        atRepository.save(at);

        // Historique
        enregistrerHistoriqueReception(reception, "CLOTURE", "AT clôturée suite à réception validée");
        enregistrerHistoriqueAT(reception, TypeActionAT.CLOTURE, statutAvant, StatutAT.CLOTUREE,
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
                .collect(Collectors.toList());
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
                .flatMap(utilisateurRepository::findByEmail)
                .map(Utilisateur::getMatricule)
                .orElse("SYSTEM");
    }

    private Utilisateur getCurrentUser() {
        return SecurityUtils.getCurrentUtilisateurId()
                .flatMap(utilisateurRepository::findByEmail)
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
        // Notification aux rôles standard OCP concernés par la réception
        // CEEP: E sur réception (8.5), CEEE: P sur réception (8.5)
        // HCEE: G sur réception (8.5), HCEP: G sur archivage (8.6)
        notificationService.sendNotificationToRoleForAt("CEEP", at, titre, message, type, "/at/" + at.getId());
        notificationService.sendNotificationToRoleForAt("CEEE", at, titre, message, type, "/at/" + at.getId());
        notificationService.sendNotificationToRoleForAt("HCEE", at, titre, message, type, "/at/" + at.getId());
        // RESPONSABLE_ENTREPRISE reste inchangé (hors logique P/E, sous-traitant externe)
        notificationService.sendNotificationToRole("RESPONSABLE_ENTREPRISE", titre, message, type, "/at/" + at.getId());
    }

    private void logAudit(String action, String resultat) {
        Utilisateur user = getCurrentUser();
        auditService.logAction(action, resultat, user, "N/A", "System");
    }
}
