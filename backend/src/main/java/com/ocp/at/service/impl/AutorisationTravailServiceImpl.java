package com.ocp.at.service.impl;

import com.ocp.at.dto.request.AutoSaveRequest;
import com.ocp.at.dto.request.RefusRequest;
import com.ocp.at.dto.request.TransferLockRequest;
import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.dto.response.HistoriqueATResponse;
import com.ocp.at.dto.response.VisaResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.*;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.AutorisationTravailMapper;
import com.ocp.at.mapper.HistoriqueATMapper;
import com.ocp.at.mapper.VisaMapper;
import com.ocp.at.repository.*;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.AutorisationTravailService;
import com.ocp.at.service.NotificationService;
import com.ocp.at.security.ATContextService;
import com.ocp.at.service.WorkflowATService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutorisationTravailServiceImpl implements AutorisationTravailService {

    private final AutorisationTravailRepository atRepository;
    private final DemandeInterventionRepository diRepository;
    private final OrdreTravailRepository otRepository;
    private final BonTravailRepository btRepository;
    private final VisitePrealableRepository visiteRepository;
    private final AnalyseRisqueRepository analyseRepository;
    private final HistoriqueATRepository historiqueRepository;
    private final VisaRepository visaRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PermisRepository permisRepository;
    private final ReceptionTravauxRepository receptionRepository;
    private final RisqueRepository risqueRepository;
    private final MesurePreparationRepository mesureRepository;
    private final EPIRepository epiRepository;
    private final MoyenAccesRepository moyenAccesRepository;
    private final TypePermisRepository typePermisRepository;
    private final com.ocp.at.repository.ServiceRepository serviceRepository;
    private final com.ocp.at.repository.ZoneRepository zoneRepository;
    
    private final WorkflowATService workflowService;
    private final NotificationService notificationService;
    private final ATContextService atContextService;
    
    private final AutorisationTravailMapper atMapper;
    private final HistoriqueATMapper historiqueMapper;
    private final VisaMapper visaMapper;

    // --- CRÉATION ---

    @Override
    @Transactional
    public AutorisationTravailResponse createFromDocument(String documentId, String typeDocument) {
        // 1. Vérifier qu'aucune AT n'existe déjà pour ce document
        boolean atExists = false;
        switch (typeDocument.toUpperCase()) {
            case "DI":
                atExists = atRepository.existsByDemandeInterventionId(documentId);
                break;
            case "OT":
                atExists = atRepository.existsByOrdreTravailId(documentId);
                break;
            case "BT":
                atExists = atRepository.existsByBonTravailId(documentId);
                break;
            default:
                throw new BusinessException("Type de document source invalide : " + typeDocument);
        }
        if (atExists) {
            throw new BusinessException("Une Autorisation de Travail existe déjà pour ce document.");
        }

        // 2. Récupérer le document source
        DemandeIntervention di = null;
        OrdreTravail ot = null;
        BonTravail bt = null;
        String objet = null;
        String description = null;
        String documentNumero = null;

        switch (typeDocument.toUpperCase()) {
            case "DI":
                di = diRepository.findById(documentId).orElseThrow(() -> new ResourceNotFoundException("DI non trouvée"));
                objet = di.getObjet();
                description = di.getDescription();
                documentNumero = di.getNumero();
                break;
            case "OT":
                ot = otRepository.findById(documentId).orElseThrow(() -> new ResourceNotFoundException("OT non trouvé"));
                objet = ot.getObjet();
                description = ot.getDescription();
                documentNumero = ot.getNumero();
                break;
            case "BT":
                bt = btRepository.findById(documentId).orElseThrow(() -> new ResourceNotFoundException("BT non trouvé"));
                objet = bt.getObjet();
                description = bt.getDescription();
                documentNumero = bt.getNumero();
                break;
        }

        // 3. Créer l'AT brouillon (les vérifications strictes sont faites à la soumission)
        Utilisateur currentUtilisateur = getCurrentUser();
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        Long seq = atRepository.getNextSequence();
        String numero = String.format("AT-%s-%06d", currentYear, seq);

        AutorisationTravail at = AutorisationTravail.builder()
                .numero(numero)
                .objet(objet)
                .descriptionTravaux(description)
                .statut(StatutAT.BROUILLON)
                .statutWorkflow(StatutAT.DEMANDE_CREEE)
                .version(1)
                .etatVerrou(EtatVerrou.EN_COURS_EDITION)
                .proprietaireBrouillon(currentUtilisateur)
                .datePriseVerrou(LocalDateTime.now())
                .demandeIntervention(di)
                .ordreTravail(ot)
                .bonTravail(bt)
                .build();

        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.CREATION, null, StatutAT.BROUILLON, "Création de l'AT depuis " + documentNumero);
        log.info("AT {} créée par {}", numero, currentUtilisateur.getNom());
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse createDirect() {
        // Création d'une AT brouillon sans document source obligatoire
        Utilisateur currentUtilisateur = getCurrentUser();
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        Long seq = atRepository.getNextSequence();
        String numero = String.format("AT-%s-%06d", currentYear, seq);

        AutorisationTravail at = AutorisationTravail.builder()
                .numero(numero)
                .objet("Nouvelle AT")
                .statut(StatutAT.BROUILLON)
                .statutWorkflow(StatutAT.DEMANDE_CREEE)
                .version(1)
                .etatVerrou(EtatVerrou.EN_COURS_EDITION)
                .proprietaireBrouillon(currentUtilisateur)
                .datePriseVerrou(LocalDateTime.now())
                .build();

        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.CREATION, null, StatutAT.BROUILLON, "Création directe de l'AT");
        log.info("AT {} créée directement par {}", numero, currentUtilisateur.getNom());
        return mapToResponse(savedAt);
    }

    // --- CONSULTATION ---

    @Override
    @Transactional(readOnly = true)
    public Page<AutorisationTravailResponse> findAll(Pageable pageable) {
        return atRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AutorisationTravailResponse findById(String id) {
        AutorisationTravail at = getEntityById(id);
        return mapToResponse(at);
    }

    // --- EDITION / AUTO SAVE ---

    @Override
    @Transactional
    public AutorisationTravailResponse autoSave(String id, AutoSaveRequest request) {
        AutorisationTravail at = getEntityById(id);
        Utilisateur currentUser = getCurrentUser();
        // Verrou souple : prendre automatiquement si libre / même propriétaire
        if (at.getEtatVerrou() == EtatVerrou.LIBRE || at.getProprietaireBrouillon() == null) {
            at.setEtatVerrou(EtatVerrou.EN_COURS_EDITION);
            at.setProprietaireBrouillon(currentUser);
            at.setDatePriseVerrou(LocalDateTime.now());
        } else if (at.getProprietaireBrouillon() != null
                && !at.getProprietaireBrouillon().getId().equals(currentUser.getId())) {
            // Autre éditeur : autoriser quand même la synchro des cases si brouillon
            log.warn("autoSave AT {} par {} alors que verrou tenu par {}",
                    id, currentUser.getEmail(), at.getProprietaireBrouillon().getEmail());
        }
        // Statut : autoriser édition tant que pas VALIDEE/CLOTUREE/ANNULEE
        StatutAT st = at.getStatut();
        if (st == StatutAT.VALIDEE || st == StatutAT.CLOTUREE || st == StatutAT.ANNULEE
                || st == StatutAT.ARCHIVEE) {
            throw new BusinessException("Une AT au statut " + st + " ne peut plus être modifiée.");
        }

        at.setObjet(request.getObjet());
        at.setDescriptionTravaux(request.getDescriptionTravaux());
        at.setDateDebut(request.getDateDebut());
        at.setDateFin(request.getDateFin());
        at.setHeureDebut(request.getHeureDebut());
        at.setHeureFin(request.getHeureFin());
        
        at.setServicesIntervenants(request.getServicesIntervenants());
        at.setEntreprisesIntervenantes(request.getEntreprisesIntervenantes());
        at.setMesuresSecuriteExecutant(request.getMesuresSecuriteExecutant());

        // Lier zone exécutante (E) pour résoudre/notifier les CEEE
        resoudreEtAffecterZones(at, request);

        // Cases formulaire → colonnes JSON (source de vérité)
        persistFormCheckboxes(at, request);

        AutorisationTravail savedAt = atRepository.save(at);

        // Sync Permis
        if (request.getPermisIds() != null) {
            List<com.ocp.at.entity.Permis> existingPermis = permisRepository.findByAutorisationTravailId(savedAt.getId());
            List<String> existingTypeIds = existingPermis.stream().map(p -> p.getTypePermis().getId()).collect(Collectors.toList());

            for (String reqTypeId : request.getPermisIds()) {
                if (!existingTypeIds.contains(reqTypeId)) {
                    com.ocp.at.entity.TypePermis type = typePermisRepository.findById(reqTypeId).orElse(null);
                    if (type != null) {
                        com.ocp.at.entity.Permis newPermis = com.ocp.at.entity.Permis.builder()
                                .typePermis(type)
                                .autorisationTravail(savedAt)
                                .estObligatoire(true)
                                .statutVerification(com.ocp.at.entity.enums.StatutPermis.A_VERIFIER)
                                .build();
                        permisRepository.save(newPermis);
                    }
                }
            }

            for (com.ocp.at.entity.Permis ep : existingPermis) {
                if (!request.getPermisIds().contains(ep.getTypePermis().getId())) {
                    permisRepository.delete(ep);
                }
            }
        }
        
        enregistrerHistorique(savedAt, TypeActionAT.AUTO_SAVE, savedAt.getStatut(), savedAt.getStatut(), "Sauvegarde automatique");

        return mapToResponse(savedAt);
    }

    // --- GESTION DU VERROU ---

    @Override
    @Transactional
    public void prendreVerrou(String id) {
        AutorisationTravail at = getEntityById(id);
        verifierStatutModifiable(at);
        
        Utilisateur currentUser = getCurrentUser();

        if (at.getEtatVerrou() == EtatVerrou.EN_COURS_EDITION && 
            at.getProprietaireBrouillon() != null && 
            !at.getProprietaireBrouillon().getId().equals(currentUser.getId())) {
            throw new BusinessException("L'AT est déjà en cours d'édition par " + at.getProprietaireBrouillon().getNom());
        }

        at.setEtatVerrou(EtatVerrou.EN_COURS_EDITION);
        at.setProprietaireBrouillon(currentUser);
        at.setDatePriseVerrou(LocalDateTime.now());
        at.setDateLiberationVerrou(null);
        atRepository.save(at);
    }

    @Override
    @Transactional
    public void libererVerrou(String id) {
        AutorisationTravail at = getEntityById(id);
        verifierVerrouEtProprietaire(at);

        at.setEtatVerrou(EtatVerrou.LIBRE);
        at.setDateLiberationVerrou(LocalDateTime.now());
        atRepository.save(at);
    }

    @Override
    @Transactional
    public void transfererVerrou(String id, TransferLockRequest request) {
        AutorisationTravail at = getEntityById(id);
        verifierVerrouEtProprietaire(at);

        Utilisateur targetUser = utilisateurRepository.findById(request.getNouvelUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Utilisateur currentUser = getCurrentUser();

        // Vérification d'une intersection de rôles
        boolean hasCommonRole = currentUser.getRoles().stream()
                .anyMatch(r1 -> targetUser.getRoles().stream().anyMatch(r2 -> r1.getId().equals(r2.getId())));
        if (!hasCommonRole) {
            throw new BusinessException("Le transfert de verrou n'est autorisé qu'entre utilisateurs ayant un rôle en commun.");
        }

        at.setProprietaireBrouillon(targetUser);
        at.setEtatVerrou(EtatVerrou.TRANSFERE);
        at.setDatePriseVerrou(LocalDateTime.now());
        atRepository.save(at);

        enregistrerHistorique(at, TypeActionAT.TRANSFERT, at.getStatut(), at.getStatut(), "Verrou transféré à " + targetUser.getNom());
        notificationService.createNotification(targetUser, "Verrou AT transféré", "L'AT " + at.getNumero() + " vous a été transférée.", "INFO", "/at/" + at.getId());
    }

    // --- SOUMISSION ---

    @Override
    @Transactional
    public AutorisationTravailResponse soumettreAT(String id) {
        AutorisationTravail at = getEntityById(id);
        // Verrou : prendre automatiquement si libre, sinon vérifier le propriétaire
        try {
            verifierVerrouEtProprietaire(at);
        } catch (BusinessException ex) {
            Utilisateur currentUser = getCurrentUser();
            if (at.getEtatVerrou() == EtatVerrou.LIBRE || at.getProprietaireBrouillon() == null) {
                at.setEtatVerrou(EtatVerrou.EN_COURS_EDITION);
                at.setProprietaireBrouillon(currentUser);
                at.setDatePriseVerrou(LocalDateTime.now());
            } else if (at.getProprietaireBrouillon() != null
                    && at.getProprietaireBrouillon().getId().equals(currentUser.getId())) {
                at.setEtatVerrou(EtatVerrou.EN_COURS_EDITION);
            } else {
                throw ex;
            }
        }

        StatutAT ancienStatut = statutEffectif(at);

        // Dates : avertissement souple — si absentes, on ne bloque plus systématiquement
        // (le formulaire papier peut être complété terrain §8.3)
        if (at.getDateDebut() == null && at.getDateFin() == null
                && at.getHeureDebut() == null && at.getHeureFin() == null) {
            log.warn("Soumission AT {} sans dates/heures renseignées", id);
        }

        // Permis : ne bloquer QUE s'il existe au moins un permis obligatoire non conforme
        List<com.ocp.at.entity.Permis> permis = permisRepository.findByAutorisationTravailId(id);
        List<com.ocp.at.entity.Permis> obligatoires = permis.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstObligatoire()))
                .collect(Collectors.toList());
        if (!obligatoires.isEmpty()) {
            boolean hasNonConforme = obligatoires.stream()
                    .anyMatch(p -> p.getStatutVerification() != com.ocp.at.entity.enums.StatutPermis.CONFORME);
            if (hasNonConforme) {
                throw new BusinessException("Tous les permis obligatoires doivent être conformes avant la soumission.");
            }
            boolean hasExpire = permis.stream()
                    .anyMatch(p -> p.getStatutVerification() == com.ocp.at.entity.enums.StatutPermis.EXPIRE);
            if (hasExpire) {
                throw new BusinessException("Certains permis sont expirés. Veuillez les mettre à jour avant la soumission.");
            }
            boolean hasInvalide = permis.stream()
                    .anyMatch(p -> p.getStatutVerification() == com.ocp.at.entity.enums.StatutPermis.INVALIDE);
            if (hasInvalide) {
                throw new BusinessException("Certains permis ont une analyse IA invalide. Veuillez relancer l'analyse.");
            }
        }

        // Transition : tenter SOUMISSION, sinon accepter les états brouillon/demande/visite
        StatutAT nouvelEtat = StatutAT.AT_REDIGEE;
        try {
            workflowService.verifierTransition(ancienStatut, TypeActionAT.SOUMISSION);
        } catch (BusinessException be) {
            // États encore soumissibles même si matrice incomplete
            if (ancienStatut != StatutAT.BROUILLON
                    && ancienStatut != StatutAT.DEMANDE_CREEE
                    && ancienStatut != StatutAT.VISITE_REALISEE
                    && ancienStatut != StatutAT.SOUMISE
                    && ancienStatut != StatutAT.AT_REDIGEE) {
                throw be;
            }
            log.warn("Transition SOUMISSION non listée pour {} — forcée vers AT_REDIGEE", ancienStatut);
        }
        // §8.3 — AT_REDIGEE (legacy statut = SOUMISE)
        at.setStatut(StatutAT.SOUMISE);
        at.setStatutWorkflow(nouvelEtat);
        at.setEtatVerrou(EtatVerrou.LIBRE);
        at.setDateLiberationVerrou(LocalDateTime.now());
        
        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.SOUMISSION, ancienStatut, nouvelEtat, "Soumission AT — workflow standard (visite / rédaction)");

        // Notifications best-effort — parcours standard CE → HM → HC
        String lienAt = "/autorisations/" + savedAt.getId();
        String lienViser = "/autorisations/" + savedAt.getId() + "/editer?mode=viser";
        String lienValider = "/visas/validation/" + savedAt.getId();

        // 1) CEEE (service intervenant / zone exécutante)
        try {
            java.util.List<Utilisateur> ceees = new java.util.ArrayList<>();
            if (atContextService != null) {
                ceees.addAll(atContextService.findChefsEquipeExecutants(savedAt.getId()));
            }
            // Fallback : tous CEEP/CEEE du service nommé
            if (ceees.isEmpty() && savedAt.getServicesIntervenants() != null) {
                serviceRepository.findAll().stream()
                        .filter(s -> savedAt.getServicesIntervenants().equalsIgnoreCase(s.getNomService()))
                        .findFirst()
                        .ifPresent(s -> ceees.addAll(utilisateurRepository.findChefsEquipeByServiceId(s.getId())));
            }
            for (Utilisateur ceee : ceees) {
                if (ceee == null) continue;
                if (savedAt.getProprietaireBrouillon() != null
                        && ceee.getId().equals(savedAt.getProprietaireBrouillon().getId())) {
                    continue;
                }
                notificationService.createNotification(
                        ceee,
                        "AT à viser (CEEE)",
                        "L'AT " + savedAt.getNumero() + " est soumise. Signez la case Visa CEEE sur le formulaire F-HSE-SEC-31-04.",
                        "ACTION",
                        lienViser
                );
            }
            log.info("Notifs CEEE: {} destinataire(s) pour AT {}", ceees.size(), savedAt.getNumero());
        } catch (Exception e) {
            log.warn("Notif CEEE: {}", e.getMessage());
        }

        // 2) HM (HMEP / HMEE) — garants terrain
        try {
            notificationService.sendNotificationToRole("HMEP", "AT soumise — garantie HMEP",
                    "L'AT " + savedAt.getNumero() + " nécessite votre garantie (Haute Maîtrise Propriétaire).",
                    "ACTION", lienValider);
            notificationService.sendNotificationToRole("HMEE", "AT soumise — garantie HMEE",
                    "L'AT " + savedAt.getNumero() + " nécessite votre garantie (Haute Maîtrise Exécutante).",
                    "ACTION", lienValider);
        } catch (Exception e) {
            log.warn("Notif HM: {}", e.getMessage());
        }

        // 3) HC (HCEE / HCEP) — validation / pilotage
        try {
            notificationService.sendNotificationToRole("HCEE", "AT soumise — validation HCEE",
                    "L'AT " + savedAt.getNumero() + " est soumise. Garantir / valider le dossier.",
                    "ACTION", lienValider);
            notificationService.sendNotificationToRole("HCEP", "AT soumise (info HCEP)",
                    "L'AT " + savedAt.getNumero() + " a été soumise dans votre périmètre.",
                    "INFO", lienAt);
        } catch (Exception e) {
            log.warn("Notif HC: {}", e.getMessage());
        }

        try {
            return mapToResponse(savedAt);
        } catch (Exception e) {
            log.error("mapToResponse après soumission AT {}", savedAt.getId(), e);
            // Recharger minimal pour éviter 500
            return atMapper.toResponse(atRepository.findById(savedAt.getId()).orElse(savedAt));
        }
    }

    // --- VALIDATION / REFUS ---

    @Override
    @Transactional
    public AutorisationTravailResponse validerAT(String id) {
        AutorisationTravail at = getEntityById(id);
        workflowService.verifierTransition(at.getStatut(), TypeActionAT.VALIDATION);

        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.VALIDATION);
        StatutAT nouvelEtat = StatutAT.AT_REDIGEE;
        at.setStatut(StatutAT.VALIDEE);
        at.setStatutWorkflow(nouvelEtat);
        
        // Création du Visa
        Utilisateur currentUser = getCurrentUser();
        Visa visa = Visa.builder()
                .autorisationTravail(at)
                .utilisateur(currentUser)
                .dateVisa(LocalDateTime.now())
                .statut(StatutVisa.VALIDATION)
                .build();
        visaRepository.save(visa);

        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.VALIDATION, ancienStatut, nouvelEtat, "AT validée — §8.3 AT_REDIGEE");
        notificationService.createNotification(at.getProprietaireBrouillon(), "AT Validée", "Votre AT " + savedAt.getNumero() + " a été validée.", "SUCCESS", "/at/" + savedAt.getId());

        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse refuserAT(String id, RefusRequest request) {
        AutorisationTravail at = getEntityById(id);
        workflowService.verifierTransition(at.getStatut(), TypeActionAT.REFUS);

        StatutAT ancienStatut = at.getStatut();
        at.setStatut(StatutAT.REJETEE);
        
        Utilisateur currentUser = getCurrentUser();
        Visa visa = Visa.builder()
                .autorisationTravail(at)
                .utilisateur(currentUser)
                .dateVisa(LocalDateTime.now())
                .statut(StatutVisa.REFUS)
                .commentaire(request.getCommentaire())
                .build();
        visaRepository.save(visa);

        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.REFUS, ancienStatut, StatutAT.REJETEE, "AT refusée : " + request.getCommentaire());
        notificationService.createNotification(at.getProprietaireBrouillon(), "AT Refusée", "Votre AT " + savedAt.getNumero() + " a été refusée.", "ERROR", "/at/" + savedAt.getId());

        return mapToResponse(savedAt);
    }

    // --- RENOUVELLEMENT / RECEPTION ---

    @Override
    @Transactional
    public AutorisationTravailResponse renouvelerAT(String id) {
        // Délègue à reconduction standard §8.4 (sans dépassement 24h)
        return reconduireAT(id, false);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse annulerAT(String id) {
        AutorisationTravail at = getEntityById(id);
        workflowService.verifierTransition(at.getStatut(), TypeActionAT.ANNULATION);

        StatutAT ancienStatut = at.getStatut();
        at.setStatut(StatutAT.ANNULEE);
        
        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.ANNULATION, ancienStatut, StatutAT.ANNULEE, "Annulation de l'AT");
        
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse cloturerAT(String id) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        // Accepte CLOTURE (legacy) ou RECEPTION_CONJOINTE (standard)
        try {
            workflowService.verifierTransition(ancienStatut, TypeActionAT.RECEPTION_CONJOINTE);
        } catch (Exception e) {
            workflowService.verifierTransition(ancienStatut, TypeActionAT.CLOTURE);
        }

        // === Module 9 : Vérification de la réception des travaux ===
        ReceptionTravaux reception = receptionRepository.findByAutorisationTravailId(id)
                .orElseThrow(() -> new BusinessException(
                        "Clôture impossible : aucune réception des travaux n'a été enregistrée pour l'AT " + at.getNumero()));

        if (!Boolean.TRUE.equals(reception.getValidee())) {
            throw new BusinessException(
                    "Clôture impossible : la réception des travaux doit être validée avant la clôture.");
        }

        if (!Boolean.TRUE.equals(reception.getEssaisConformes())) {
            throw new BusinessException(
                    "Clôture impossible : tous les essais doivent être conformes.");
        }

        if (!Boolean.TRUE.equals(reception.getInstallationRemiseEnEtat())) {
            throw new BusinessException(
                    "Clôture impossible : la remise en état de l'installation n'est pas complète.");
        }
        // === Fin vérification Module 9 ===

        at.setStatut(StatutAT.CLOTUREE);
        at.setStatutWorkflow(StatutAT.TRAVAUX_RECEPTIONES);
        
        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.RECEPTION_CONJOINTE, ancienStatut, StatutAT.TRAVAUX_RECEPTIONES, "§8.5 — Clôture / réception AT");
        notificationService.createNotification(at.getProprietaireBrouillon(), "AT Clôturée", "Votre AT " + savedAt.getNumero() + " a été clôturée.", "INFO", "/at/" + savedAt.getId());
        
        return mapToResponse(savedAt);
    }

    // --- Standard S-HSE-SEC-31 Workflow Methods ---

    @Override
    @Transactional
    public AutorisationTravailResponse classifierIntervention(String documentId, String typeDocument, Integer niveau) {
        log.info("Classification intervention docId={} type={} niveau={}", documentId, typeDocument, niveau);
        if (niveau != null && niveau == 1) {
            throw new BusinessException("L'intervention de Niveau 1 ne nécessite pas d'Autorisation de Travail (couverture par ADRPT + Plan de Prévention uniquement).");
        }

        // Niveau 2 → AT obligatoire : création demande (§8.1) après classification (§6)
        AutorisationTravailResponse response = createFromDocument(documentId, typeDocument);
        AutorisationTravail at = getEntityById(response.getId());
        at.setStatutWorkflow(StatutAT.DEMANDE_CREEE);
        AutorisationTravail savedAt = atRepository.save(at);

        enregistrerHistorique(savedAt, TypeActionAT.CLASSIFICATION, StatutAT.CLASSIFICATION_EFFECTUEE, StatutAT.DEMANDE_CREEE,
                "Classification Niveau 2 (HCEP) → Demande d'intervention créée (CEEP)");
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse demarrerIntervention(String id) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.DEBUT_INTERVENTION);
        StatutAT nouvelEtat = StatutAT.INTERVENTION_EN_COURS;

        at.setStatutWorkflow(nouvelEtat);
        at.setStatut(StatutAT.VALIDEE);
        AutorisationTravail savedAt = atRepository.save(at);

        enregistrerHistorique(savedAt, TypeActionAT.DEBUT_INTERVENTION, ancienStatut, nouvelEtat, "§8 — Démarrage travaux (CEEE E, HCEE/HMEP G)");
        notificationService.createNotification(at.getProprietaireBrouillon(), "Intervention Démarrée", "L'intervention sur l'AT " + savedAt.getNumero() + " a démarré.", "INFO", "/at/" + savedAt.getId());

        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse declarerFinTravaux(String id) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.DECLARATION_FIN);
        StatutAT nouvelEtat = StatutAT.FIN_TRAVAUX_DECLAREE;

        at.setStatutWorkflow(nouvelEtat);
        AutorisationTravail savedAt = atRepository.save(at);

        enregistrerHistorique(savedAt, TypeActionAT.DECLARATION_FIN, ancienStatut, nouvelEtat, "§8.5 — Fin des travaux déclarée (CEEE E, CEEP I)");
        notificationService.createNotification(at.getProprietaireBrouillon(), "Fin des Travaux Déclarée", "Le CEEE a déclaré la fin des travaux sur l'AT " + savedAt.getNumero() + ".", "ACTION", "/at/" + savedAt.getId());

        return mapToResponse(savedAt);
    }

    // --- HISTORIQUE ET VISAS ---

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueATResponse> getHistorique(String id) {
        return historiqueRepository.findByAutorisationTravailIdOrderByDateActionDesc(id)
                .stream().map(historiqueMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisaResponse> getVisas(String id) {
        return visaRepository.findByAutorisationTravailId(id)
                .stream().map(visaMapper::toResponse).collect(Collectors.toList());
    }


    // --- STANDARD S-HSE-SEC-31 : Visite / Rédaction / Reconduction / Incident / Réception ---

    @Override
    @Transactional
    public AutorisationTravailResponse marquerVisiteRealisee(String id) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.VISITE_CHANTIER);
        StatutAT nouvelEtat = StatutAT.VISITE_REALISEE;
        at.setStatutWorkflow(nouvelEtat);
        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.VISITE_CHANTIER, ancienStatut, nouvelEtat, "§8.2 — Visite préalable chantier réalisée");
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse redigerAT(String id) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.REDACTION_AT);
        StatutAT nouvelEtat = StatutAT.AT_REDIGEE;
        at.setStatutWorkflow(nouvelEtat);
        at.setStatut(StatutAT.VALIDEE);
        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.REDACTION_AT, ancienStatut, nouvelEtat, "§8.3 — AT et permis rédigés/signés sur le terrain");
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse reconduireAT(String id, boolean depasse24h) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        if (depasse24h) {
            // §8.4 — > 24h : nouvelle visite obligatoire
            workflowService.verifierTransition(StatutAT.AT_RECONDUITE, TypeActionAT.VISITE_CHANTIER);
            at.setStatutWorkflow(StatutAT.VISITE_REALISEE);
            at.setVersion(at.getVersion() == null ? 2 : at.getVersion() + 1);
            AutorisationTravail savedAt = atRepository.save(at);
            enregistrerHistorique(savedAt, TypeActionAT.VISITE_CHANTIER, ancienStatut, StatutAT.VISITE_REALISEE,
                    "§8.4 — Dépassement 24h : nouvelle visite chantier obligatoire");
            return mapToResponse(savedAt);
        }
        workflowService.verifierTransition(ancienStatut, TypeActionAT.RECONDUCTION);
        at.setStatutWorkflow(StatutAT.AT_RECONDUITE);
        at.setStatut(StatutAT.RENOUVELEE);
        at.setVersion(at.getVersion() == null ? 2 : at.getVersion() + 1);
        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.RECONDUCTION, ancienStatut, StatutAT.AT_RECONDUITE,
                "§8.4 — Reconduction AT (début de poste)");
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse signalerIncident(String id, String motif) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        // Retour à VISITE_REALISEE (§8.4)
        at.setStatutWorkflow(StatutAT.VISITE_REALISEE);
        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.VISITE_CHANTIER, ancienStatut, StatutAT.VISITE_REALISEE,
                "§8.4 — Incident/changement condition : " + (motif != null ? motif : "retour visite obligatoire"));
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse receptionnerTravauxStandard(String id) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.RECEPTION_CONJOINTE);
        StatutAT nouvelEtat = StatutAT.TRAVAUX_RECEPTIONES;
        at.setStatutWorkflow(nouvelEtat);
        at.setStatut(StatutAT.CLOTUREE);
        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.RECEPTION_CONJOINTE, ancienStatut, nouvelEtat,
                "§8.5 — Réception conjointe CEEP+CEEE, clôture AT et permis");
        return mapToResponse(savedAt);
    }

    private StatutAT statutEffectif(AutorisationTravail at) {
        if (at.getStatutWorkflow() != null) {
            return at.getStatutWorkflow();
        }
        return at.getStatut();
    }

    // --- METHODES PRIVEES ---


    /**
     * Affecte zoneProprietaire (service du CEEP courant) et zoneExecutante (service intervenant).
     */
    private void resoudreEtAffecterZones(AutorisationTravail at, AutoSaveRequest request) {
        try {
            Utilisateur current = getCurrentUser();
            if (at.getZoneProprietaire() == null && current.getService() != null
                    && current.getService().getZone() != null) {
                at.setZoneProprietaire(current.getService().getZone());
            }
            if (request.getZoneProprietaireId() != null && !request.getZoneProprietaireId().isBlank()) {
                zoneRepository.findById(request.getZoneProprietaireId()).ifPresent(at::setZoneProprietaire);
            }
            com.ocp.at.entity.Service svc = null;
            if (request.getServiceIntervenantId() != null && !request.getServiceIntervenantId().isBlank()) {
                svc = serviceRepository.findById(request.getServiceIntervenantId()).orElse(null);
            }
            if (svc == null && request.getServicesIntervenants() != null
                    && !request.getServicesIntervenants().isBlank()) {
                String nom = request.getServicesIntervenants().trim();
                svc = serviceRepository.findAll().stream()
                        .filter(s -> nom.equalsIgnoreCase(s.getNomService())
                                || nom.equalsIgnoreCase(s.getCodeService()))
                        .findFirst()
                        .orElse(null);
            }
            if (svc != null) {
                if (svc.getZone() != null) {
                    at.setZoneExecutante(svc.getZone());
                }
                if (at.getServicesIntervenants() == null || at.getServicesIntervenants().isBlank()) {
                    at.setServicesIntervenants(svc.getNomService());
                }
            }
        } catch (Exception e) {
            log.warn("resoudreEtAffecterZones: {}", e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public void verifierDroitExportPdf(String id) {
        AutorisationTravail at = getEntityById(id);
        List<String> motifs = calculerMotifsRefusExportPdf(at);
        if (!motifs.isEmpty()) {
            throw new BusinessException("Export PDF refusé : " + String.join(" | ", motifs));
        }
    }

    public List<String> calculerMotifsRefusExportPdf(AutorisationTravail at) {
        List<String> motifs = new java.util.ArrayList<>();

        StatutAT st = at.getStatut();
        if (st == StatutAT.BROUILLON || st == StatutAT.REJETEE || st == StatutAT.ANNULEE) {
            motifs.add("L'AT doit être validée avant l'export PDF (statut actuel : " + (st != null ? st.name() : "N/A") + ").");
        }

        List<Visa> visas = visaRepository.findByAutorisationTravailId(at.getId());

        boolean hmOk = visas.stream().anyMatch(v -> 
            isVisaPositif(v) && userHasRolePattern(v.getUtilisateur(), "HM")
        );
        if (!hmOk) {
            motifs.add("Validation Haute Maîtrise (HM) manquante.");
        }

        boolean hcOk = visas.stream().anyMatch(v -> 
            isVisaPositif(v) && userHasRolePattern(v.getUtilisateur(), "HC")
        );
        if (!hcOk) {
            motifs.add("Validation Hors Cadre (HC) manquante.");
        }

        List<com.ocp.at.entity.Permis> permisList = permisRepository.findByAutorisationTravailId(at.getId());
        for (com.ocp.at.entity.Permis p : permisList) {
            if (Boolean.TRUE.equals(p.getEstObligatoire())) {
                if (p.getStatutVerification() != com.ocp.at.entity.enums.StatutPermis.CONFORME) {
                    String nomPermis = p.getTypePermis() != null ? p.getTypePermis().getNom() : "Permis";
                    motifs.add("Permis obligatoire non conforme : " + nomPermis + " (" + p.getStatutVerification() + ")");
                }
            }
        }

        return motifs;
    }

    private boolean isVisaPositif(Visa v) {
        if (v == null || v.getStatut() == null) return false;
        StatutVisa s = v.getStatut();
        return s == StatutVisa.VALIDE || s == StatutVisa.VALIDATION || s == StatutVisa.SIGNATURE;
    }

    private boolean userHasRolePattern(Utilisateur user, String pattern) {
        if (user == null || user.getRoles() == null) return false;
        return user.getRoles().stream().anyMatch(r -> {
            if (r.getNom() == null) return false;
            String nom = r.getNom().toUpperCase();
            return nom.contains(pattern.toUpperCase());
        });
    }

    private String toJsonIds(java.util.List<String> ids) {
        if (ids == null) {
            return "[]";
        }
        java.util.List<String> distinct = ids.stream()
                .filter(x -> x != null && !x.isBlank())
                .distinct()
                .collect(Collectors.toList());
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(distinct);
        } catch (Exception e) {
            return "[]";
        }
    }

    private java.util.List<String> fromJsonIds(String json) {
        if (json == null || json.isBlank()) {
            return java.util.Collections.emptyList();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    json, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    private void persistFormCheckboxes(AutorisationTravail at, AutoSaveRequest request) {
        if (request.getRisquesIds() != null) {
            at.setFormRisquesIds(toJsonIds(request.getRisquesIds()));
        }
        if (request.getMesuresIds() != null) {
            at.setFormMesuresIds(toJsonIds(request.getMesuresIds()));
        }
        if (request.getEpisIds() != null) {
            at.setFormEpisIds(toJsonIds(request.getEpisIds()));
        }
        if (request.getMoyensAccesIds() != null) {
            at.setFormMoyensIds(toJsonIds(request.getMoyensAccesIds()));
        }
        if (request.getPermisIds() != null) {
            at.setFormPermisIds(toJsonIds(request.getPermisIds()));
        }
        log.info("Form checkboxes AT {} r={} m={} e={} mo={}",
                at.getId(), at.getFormRisquesIds(), at.getFormMesuresIds(),
                at.getFormEpisIds(), at.getFormMoyensIds());
    }

    private AutorisationTravail getEntityById(String id) {
        AutorisationTravail at = atRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AutorisationTravail non trouvée"));
        // Charger les bags un par un (évite MultipleBagFetchException)
        forceLoadCollections(at);
        return at;
    }

    private void forceLoadCollections(AutorisationTravail at) {
        try {
            if (at.getRisques() != null) {
                at.getRisques().size();
            }
            if (at.getMesures() != null) {
                at.getMesures().size();
            }
            if (at.getEpis() != null) {
                at.getEpis().size();
            }
            if (at.getMoyensAcces() != null) {
                at.getMoyensAcces().size();
            }
            if (at.getZoneProprietaire() != null) {
                at.getZoneProprietaire().getId();
            }
            if (at.getZoneExecutante() != null) {
                at.getZoneExecutante().getId();
            }
        } catch (Exception e) {
            log.warn("forceLoadCollections AT {}: {}", at.getId(), e.getMessage());
        }
    }

    private Utilisateur getCurrentUser() {
        return SecurityUtils.getCurrentUtilisateurId()
                .flatMap(utilisateurRepository::findByEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur non authentifié"));
    }

    private void verifierVerrouEtProprietaire(AutorisationTravail at) {
        Utilisateur currentUser = getCurrentUser();
        if (at.getEtatVerrou() != EtatVerrou.EN_COURS_EDITION) {
            throw new BusinessException("Vous devez prendre le verrou pour modifier cette AT.");
        }
        if (at.getProprietaireBrouillon() == null || !at.getProprietaireBrouillon().getId().equals(currentUser.getId())) {
            throw new BusinessException("Vous ne possédez pas le verrou sur cette AT.");
        }
    }

    private void verifierStatutModifiable(AutorisationTravail at) {
        if (at.getStatut() != StatutAT.BROUILLON) {
            throw new BusinessException("Une AT au statut " + at.getStatut() + " ne peut pas être modifiée.");
        }
    }

    private void enregistrerHistorique(AutorisationTravail at, TypeActionAT action, StatutAT ancien, StatutAT nouveau, String com) {
        Utilisateur currentUser = null;
        try {
            currentUser = getCurrentUser();
        } catch (Exception e) {
            log.warn("Utilisateur non authentifié pour l'historique (système potentiel)");
        }

        // Mappe vers des statuts legacy si la colonne DB a encore un CHECK V1 restrictif
        StatutAT ancienSafe = toLegacyStatut(ancien);
        StatutAT nouveauSafe = toLegacyStatut(nouveau);
        // Actions hors CHECK V1 → MODIFICATION (évite violation contrainte action)
        TypeActionAT actionSafe = toLegacyAction(action);

        try {
            HistoriqueAT h = HistoriqueAT.builder()
                    .autorisationTravail(at)
                    .dateAction(LocalDateTime.now())
                    .action(actionSafe)
                    .ancienStatut(ancienSafe)
                    .nouveauStatut(nouveauSafe)
                    .commentaire((com != null ? com : "") + (action != actionSafe ? " [" + action + "]" : ""))
                    .utilisateur(currentUser)
                    .build();
            historiqueRepository.save(h);
        } catch (Exception e) {
            // Ne jamais faire échouer le métier pour un historique
            log.error("Échec enregistrement historique AT {} : {}", at.getId(), e.getMessage());
        }
    }

    /** Statuts acceptés par CHECK V1 historiques_at / autorisations_travail.statut */
    private StatutAT toLegacyStatut(StatutAT s) {
        if (s == null) {
            return null;
        }
        switch (s) {
            case BROUILLON:
            case SOUMISE:
            case VALIDEE:
            case REJETEE:
            case RENOUVELEE:
            case CLOTUREE:
            case ARCHIVEE:
            case ANNULEE:
                return s;
            case CLASSIFICATION_EFFECTUEE:
            case DEMANDE_CREEE:
                return StatutAT.BROUILLON;
            case VISITE_REALISEE:
            case AT_REDIGEE:
                return StatutAT.SOUMISE;
            case INTERVENTION_EN_COURS:
            case AT_RECONDUITE:
                return StatutAT.VALIDEE;
            case FIN_TRAVAUX_DECLAREE:
            case TRAVAUX_RECEPTIONES:
                return StatutAT.CLOTUREE;
            default:
                return StatutAT.BROUILLON;
        }
    }

    private TypeActionAT toLegacyAction(TypeActionAT a) {
        if (a == null) {
            return TypeActionAT.MODIFICATION;
        }
        switch (a) {
            case CREATION:
            case MODIFICATION:
            case AUTO_SAVE:
            case TRANSFERT:
            case SOUMISSION:
            case VALIDATION:
            case REFUS:
            case RENOUVELLEMENT:
            case CLOTURE:
            case EXPORT_PDF:
            case ANNULATION:
            case RECEPTION_TRAVAUX:
            case VALIDATION_RECEPTION:
                return a;
            case CLASSIFICATION:
            case CREATION_DEMANDE:
                return TypeActionAT.CREATION;
            case VISITE_CHANTIER:
            case REDACTION_AT:
                return TypeActionAT.MODIFICATION;
            case DEBUT_INTERVENTION:
                return TypeActionAT.VALIDATION;
            case RECONDUCTION:
                return TypeActionAT.RENOUVELLEMENT;
            case DECLARATION_FIN:
            case RECEPTION_CONJOINTE:
                return TypeActionAT.RECEPTION_TRAVAUX;
            case ARCHIVAGE_OFFICIEL:
                return TypeActionAT.CLOTURE;
            default:
                return TypeActionAT.MODIFICATION;
        }
    }

    private AutorisationTravailResponse mapToResponse(AutorisationTravail at) {
        AutorisationTravailResponse response = atMapper.toResponse(at);
        if (at.getDemandeIntervention() != null) {
            response.setTypeDocumentSource("DI");
            response.setDocumentSourceId(at.getDemandeIntervention().getId());
            response.setDocumentSourceNumero(at.getDemandeIntervention().getNumero());
        } else if (at.getOrdreTravail() != null) {
            response.setTypeDocumentSource("OT");
            response.setDocumentSourceId(at.getOrdreTravail().getId());
            response.setDocumentSourceNumero(at.getOrdreTravail().getNumero());
        } else if (at.getBonTravail() != null) {
            response.setTypeDocumentSource("BT");
            response.setDocumentSourceId(at.getBonTravail().getId());
            response.setDocumentSourceNumero(at.getBonTravail().getNumero());
        }
        
        response.setServicesIntervenants(at.getServicesIntervenants());
        response.setEntreprisesIntervenantes(at.getEntreprisesIntervenantes());
        response.setMesuresSecuriteExecutant(at.getMesuresSecuriteExecutant());
        
        // Colonnes JSON formulaire = source de vérité si renseignées
        if (at.getFormRisquesIds() != null) {
            response.setRisquesIds(fromJsonIds(at.getFormRisquesIds()));
        } else {
            response.setRisquesIds(at.getRisques() == null ? java.util.Collections.emptyList()
                    : at.getRisques().stream().map(r -> r.getId()).collect(Collectors.toList()));
        }
        if (at.getFormMesuresIds() != null) {
            response.setMesuresIds(fromJsonIds(at.getFormMesuresIds()));
        } else {
            response.setMesuresIds(at.getMesures() == null ? java.util.Collections.emptyList()
                    : at.getMesures().stream().map(m -> m.getId()).collect(Collectors.toList()));
        }
        if (at.getFormEpisIds() != null) {
            response.setEpisIds(fromJsonIds(at.getFormEpisIds()));
        } else {
            response.setEpisIds(at.getEpis() == null ? java.util.Collections.emptyList()
                    : at.getEpis().stream().map(e -> e.getId()).collect(Collectors.toList()));
        }
        if (at.getFormMoyensIds() != null) {
            response.setMoyensAccesIds(fromJsonIds(at.getFormMoyensIds()));
        } else {
            response.setMoyensAccesIds(at.getMoyensAcces() == null ? java.util.Collections.emptyList()
                    : at.getMoyensAcces().stream().map(m -> m.getId()).collect(Collectors.toList()));
        }
        java.util.List<String> permisJson = at.getFormPermisIds() != null
                ? fromJsonIds(at.getFormPermisIds()) : java.util.Collections.emptyList();
        
        if (!permisJson.isEmpty()) {
            response.setPermisIds(permisJson);
        } else {
            List<com.ocp.at.entity.Permis> permisList = permisRepository.findByAutorisationTravailId(at.getId());
            response.setPermisIds(permisList.stream()
                    .filter(p -> p.getTypePermis() != null)
                    .map(p -> p.getTypePermis().getId())
                    .collect(Collectors.toList()));
        }

        // Calcul des droits et motifs d'export PDF (HM + HC + Permis conformes)
        List<String> motifsRefus = calculerMotifsRefusExportPdf(at);
        response.setExportPdfAutorise(motifsRefus.isEmpty());
        response.setExportPdfMotifsRefus(motifsRefus);
        
        return response;
    }

    private String getVisiteIdFromDoc(DemandeIntervention di, OrdreTravail ot, BonTravail bt) {
        if (di != null && di.getVisitePrealable() != null) return di.getVisitePrealable().getId();
        if (ot != null && ot.getVisitePrealable() != null) return ot.getVisitePrealable().getId();
        if (bt != null && bt.getVisitePrealable() != null) return bt.getVisitePrealable().getId();
        return "UNKNOWN";
    }
}
