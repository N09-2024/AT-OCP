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
    
    private final WorkflowATService workflowService;
    private final NotificationService notificationService;
    
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
            case "DI" -> atExists = atRepository.existsByDemandeInterventionId(documentId);
            case "OT" -> atExists = atRepository.existsByOrdreTravailId(documentId);
            case "BT" -> atExists = atRepository.existsByBonTravailId(documentId);
            default -> throw new BusinessException("Type de document source invalide : " + typeDocument);
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
        verifierVerrouEtProprietaire(at);
        verifierStatutModifiable(at);

        at.setObjet(request.getObjet());
        at.setDescriptionTravaux(request.getDescriptionTravaux());
        at.setDateDebut(request.getDateDebut());
        at.setDateFin(request.getDateFin());
        at.setHeureDebut(request.getHeureDebut());
        at.setHeureFin(request.getHeureFin());
        
        at.setServicesIntervenants(request.getServicesIntervenants());
        at.setEntreprisesIntervenantes(request.getEntreprisesIntervenantes());
        at.setMesuresSecuriteExecutant(request.getMesuresSecuriteExecutant());

        if (request.getRisquesIds() != null) {
            at.setRisques(risqueRepository.findAllById(request.getRisquesIds()));
        }
        if (request.getMesuresIds() != null) {
            at.setMesures(mesureRepository.findAllById(request.getMesuresIds()));
        }
        if (request.getEpisIds() != null) {
            at.setEpis(epiRepository.findAllById(request.getEpisIds()));
        }
        if (request.getMoyensAccesIds() != null) {
            at.setMoyensAcces(moyenAccesRepository.findAllById(request.getMoyensAccesIds()));
        }

        AutorisationTravail savedAt = atRepository.save(at);

        // Sync Permis
        if (request.getPermisIds() != null) {
            List<com.ocp.at.entity.Permis> existingPermis = permisRepository.findByAutorisationTravailId(savedAt.getId());
            List<String> existingTypeIds = existingPermis.stream().map(p -> p.getTypePermis().getId()).toList();

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
        verifierVerrouEtProprietaire(at);
        
        workflowService.verifierTransition(at.getStatut(), TypeActionAT.SOUMISSION);

        // Validation des champs obligatoires
        if (at.getDateDebut() == null || at.getDateFin() == null || at.getHeureDebut() == null || at.getHeureFin() == null) {
            throw new BusinessException("Les dates et heures de l'AT doivent être renseignées avant la soumission.");
        }

        // --- MODULE 7 : Contrôle des permis avant soumission ---
        List<com.ocp.at.entity.Permis> permis = permisRepository.findByAutorisationTravailId(id);
        
        // 1. Vérifier que tous les permis obligatoires sont présents et conformes
        boolean hasNonConforme = permis.stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstObligatoire()))
                .anyMatch(p -> p.getStatutVerification() != com.ocp.at.entity.enums.StatutPermis.CONFORME);
        if (hasNonConforme) {
            throw new BusinessException("Tous les permis obligatoires doivent être conformes avant la soumission.");
        }
        
        // 2. Vérifier qu'aucun permis n'est expiré
        boolean hasExpire = permis.stream()
                .anyMatch(p -> p.getStatutVerification() == com.ocp.at.entity.enums.StatutPermis.EXPIRE);
        if (hasExpire) {
            throw new BusinessException("Certains permis sont expirés. Veuillez les mettre à jour avant la soumission.");
        }
        
        // 3. Vérifier qu'aucune analyse IA n'est en erreur (INVALIDE)
        boolean hasInvalide = permis.stream()
                .anyMatch(p -> p.getStatutVerification() == com.ocp.at.entity.enums.StatutPermis.INVALIDE);
        if (hasInvalide) {
            throw new BusinessException("Certains permis ont une analyse IA invalide. Veuillez relancer l'analyse.");
        }

        StatutAT ancienStatut = at.getStatut();
        at.setStatut(StatutAT.SOUMISE);
        at.setEtatVerrou(EtatVerrou.LIBRE);
        at.setDateLiberationVerrou(LocalDateTime.now());
        
        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.SOUMISSION, ancienStatut, StatutAT.SOUMISE, "Soumission de l'AT pour validation");
        notificationService.sendNotificationToRole("Validateur", "AT à valider", "L'AT " + savedAt.getNumero() + " nécessite votre validation.", "ACTION", "/at/" + savedAt.getId() + "/validation");

        return mapToResponse(savedAt);
    }

    // --- VALIDATION / REFUS ---

    @Override
    @Transactional
    public AutorisationTravailResponse validerAT(String id) {
        AutorisationTravail at = getEntityById(id);
        workflowService.verifierTransition(at.getStatut(), TypeActionAT.VALIDATION);

        StatutAT ancienStatut = at.getStatut();
        at.setStatut(StatutAT.VALIDEE);
        
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
        
        enregistrerHistorique(savedAt, TypeActionAT.VALIDATION, ancienStatut, StatutAT.VALIDEE, "AT validée");
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
        AutorisationTravail at = getEntityById(id);
        workflowService.verifierTransition(at.getStatut(), TypeActionAT.RENOUVELLEMENT);

        StatutAT ancienStatut = at.getStatut();
        at.setStatut(StatutAT.SOUMISE);
        at.setVersion(at.getVersion() + 1); // Incrément de version
        
        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.RENOUVELLEMENT, ancienStatut, StatutAT.SOUMISE, "Renouvellement de l'AT (Version " + savedAt.getVersion() + ")");
        
        return mapToResponse(savedAt);
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
        workflowService.verifierTransition(at.getStatut(), TypeActionAT.CLOTURE);

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

        StatutAT ancienStatut = at.getStatut();
        at.setStatut(StatutAT.CLOTUREE);
        
        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.CLOTURE, ancienStatut, StatutAT.CLOTUREE, "Clôture de l'AT");
        notificationService.createNotification(at.getProprietaireBrouillon(), "AT Clôturée", "Votre AT " + savedAt.getNumero() + " a été clôturée.", "INFO", "/at/" + savedAt.getId());
        
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

    // --- METHODES PRIVEES ---

    private AutorisationTravail getEntityById(String id) {
        return atRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("AutorisationTravail non trouvée"));
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
        
        HistoriqueAT h = HistoriqueAT.builder()
                .autorisationTravail(at)
                .dateAction(LocalDateTime.now())
                .action(action)
                .ancienStatut(ancien)
                .nouveauStatut(nouveau)
                .commentaire(com)
                .utilisateur(currentUser)
                .build();
        historiqueRepository.save(h);
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
        
        if (at.getRisques() != null) {
            response.setRisquesIds(at.getRisques().stream().map(r -> r.getId()).toList());
        }
        if (at.getMesures() != null) {
            response.setMesuresIds(at.getMesures().stream().map(m -> m.getId()).toList());
        }
        if (at.getEpis() != null) {
            response.setEpisIds(at.getEpis().stream().map(e -> e.getId()).toList());
        }
        if (at.getMoyensAcces() != null) {
            response.setMoyensAccesIds(at.getMoyensAcces().stream().map(m -> m.getId()).toList());
        }
        
        List<com.ocp.at.entity.Permis> permisList = permisRepository.findByAutorisationTravailId(at.getId());
        response.setPermisIds(permisList.stream().map(p -> p.getTypePermis().getId()).toList());
        
        return response;
    }

    private String getVisiteIdFromDoc(DemandeIntervention di, OrdreTravail ot, BonTravail bt) {
        if (di != null && di.getVisitePrealable() != null) return di.getVisitePrealable().getId();
        if (ot != null && ot.getVisitePrealable() != null) return ot.getVisitePrealable().getId();
        if (bt != null && bt.getVisitePrealable() != null) return bt.getVisitePrealable().getId();
        return "UNKNOWN";
    }
}
