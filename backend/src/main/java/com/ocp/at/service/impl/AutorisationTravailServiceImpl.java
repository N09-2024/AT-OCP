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
import com.ocp.at.security.RoleUtils;
import com.ocp.at.service.AutorisationTravailService;
import com.ocp.at.service.NotificationService;
import com.ocp.at.security.ATContextService;
import com.ocp.at.service.WorkflowATService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final com.ocp.at.service.PermisDocumentService permisDocumentService;
    private final com.ocp.at.service.InterventionReadinessService interventionReadinessService;
    
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
        return findAll(null, null, null, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AutorisationTravailResponse> findAll(String statut, String search, Pageable pageable) {
        return findAll(statut, search, null, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AutorisationTravailResponse> findAll(String statut, String search, Boolean mine, Boolean aValider, Pageable pageable) {
        Utilisateur currentUser = getCurrentUser();

        Specification<AutorisationTravail> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtrage STRICT de base selon le rôle de l'utilisateur (Standard OCP S-HSE-SEC-31)
            boolean isAdmin = RoleUtils.userHasRolePattern(currentUser, "ADMIN");

            if (!isAdmin) {
                String zoneId = (currentUser.getService() != null && currentUser.getService().getZone() != null)
                        ? currentUser.getService().getZone().getId() : "";
                String serviceNom = currentUser.getService() != null ? currentUser.getService().getNomService() : "";

                boolean isCeep = RoleUtils.isCeep(currentUser);
                boolean isCeee = RoleUtils.isCeee(currentUser);
                boolean isHcep = RoleUtils.userHasRolePattern(currentUser, "HCEP");
                boolean isHcee = RoleUtils.userHasRolePattern(currentUser, "HCEE");
                boolean isHmep = RoleUtils.userHasRolePattern(currentUser, "HMEP");
                boolean isHmee = RoleUtils.userHasRolePattern(currentUser, "HMEE");
                boolean isHc = RoleUtils.isHc(currentUser);
                boolean isHm = RoleUtils.isHm(currentUser);

                // Règle fondamentale OCP : AUCUN utilisateur autre que le CEEP émetteur (et ADMIN)
                // ne peut voir les brouillons en cours de rédaction
                Predicate notBrouillon = cb.notEqual(root.get("statut"), StatutAT.BROUILLON);

                if (isCeep && !isCeee && !isHc && !isHm) {
                    // CEEP pur : uniquement les AT créées par lui (ses brouillons + ses AT soumises)
                    predicates.add(cb.equal(root.get("proprietaireBrouillon").get("id"), currentUser.getId()));
                } else if (isCeee && !isCeep && !isHc && !isHm) {
                    // CEEE pur (Équipe Exécutante) :
                    // Voit uniquement les ATs transmises (hors brouillon) adressées à son service/zone ou où il a signé
                    if (!zoneId.isBlank() || !serviceNom.isBlank()) {
                        Predicate zoneMatch = cb.equal(root.get("zoneExecutante").get("id"), zoneId);
                        Predicate serviceMatch = cb.like(cb.lower(root.get("servicesIntervenants")), "%" + serviceNom.toLowerCase() + "%");
                        predicates.add(cb.and(notBrouillon, cb.or(zoneMatch, serviceMatch)));
                    } else {
                        predicates.add(notBrouillon);
                    }
                } else if (isHcep || isHcee || isHc) {
                    // Hors Cadre (HCEP / HCEE) :
                    // Voit les ATs transmises (hors brouillon) liées à son périmètre propriétaire ou exécutant
                    if (!zoneId.isBlank() || !serviceNom.isBlank()) {
                        Predicate zonePropMatch = cb.equal(root.get("zoneProprietaire").get("id"), zoneId);
                        Predicate zoneExecMatch = cb.equal(root.get("zoneExecutante").get("id"), zoneId);
                        Predicate serviceMatch = cb.like(cb.lower(root.get("servicesIntervenants")), "%" + serviceNom.toLowerCase() + "%");
                        predicates.add(cb.and(notBrouillon, cb.or(zonePropMatch, zoneExecMatch, serviceMatch)));
                    } else {
                        predicates.add(notBrouillon);
                    }
                } else if (isHmep || isHmee || isHm) {
                    // Haute Maîtrise (HMEP / HMEE) :
                    // Voit toutes les ATs transmises (hors brouillon)
                    if (!zoneId.isBlank() || !serviceNom.isBlank()) {
                        Predicate zonePropMatch = cb.equal(root.get("zoneProprietaire").get("id"), zoneId);
                        Predicate zoneExecMatch = cb.equal(root.get("zoneExecutante").get("id"), zoneId);
                        Predicate serviceMatch = cb.like(cb.lower(root.get("servicesIntervenants")), "%" + serviceNom.toLowerCase() + "%");
                        predicates.add(cb.and(notBrouillon, cb.or(zonePropMatch, zoneExecMatch, serviceMatch)));
                    } else {
                        predicates.add(notBrouillon);
                    }
                } else if (isCeep && isCeee) {
                    // Chef d'Équipe polyvalent (CE) :
                    // Voit ses propres ATs (avec ses brouillons) OU les ATs transmises adressées à son service
                    Predicate isOwner = cb.equal(root.get("proprietaireBrouillon").get("id"), currentUser.getId());
                    if (!zoneId.isBlank() || !serviceNom.isBlank()) {
                        Predicate zoneMatch = cb.equal(root.get("zoneExecutante").get("id"), zoneId);
                        Predicate serviceMatch = cb.like(cb.lower(root.get("servicesIntervenants")), "%" + serviceNom.toLowerCase() + "%");
                        predicates.add(cb.or(isOwner, cb.and(notBrouillon, cb.or(zoneMatch, serviceMatch))));
                    } else {
                        predicates.add(cb.or(isOwner, notBrouillon));
                    }
                } else {
                    // Défaut sécurisé : uniquement ses créations
                    predicates.add(cb.equal(root.get("proprietaireBrouillon").get("id"), currentUser.getId()));
                }
            }

            // 1b. Filtre optionnel « Mes AT » (mine = true)
            if (Boolean.TRUE.equals(mine)) {
                predicates.add(cb.equal(root.get("proprietaireBrouillon").get("id"), currentUser.getId()));
            }

            // 1c. Filtre optionnel « À valider » (aValider = true)
            if (Boolean.TRUE.equals(aValider)) {
                jakarta.persistence.criteria.Subquery<Long> visaSubquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<Visa> visaRoot = visaSubquery.from(Visa.class);
                visaSubquery.select(cb.literal(1L)).where(
                    cb.equal(visaRoot.get("autorisationTravail"), root),
                    cb.equal(visaRoot.get("utilisateur").get("id"), currentUser.getId()),
                    cb.equal(visaRoot.get("statut"), StatutVisa.EN_ATTENTE)
                );
                Predicate pendingVisa = cb.exists(visaSubquery);

                boolean isHcee = RoleUtils.userHasRolePattern(currentUser, "HCEE");
                boolean isHcep = RoleUtils.userHasRolePattern(currentUser, "HCEP");
                boolean isHc = RoleUtils.isHc(currentUser);
                boolean isCeee = RoleUtils.isCeee(currentUser);

                List<Predicate> aValiderList = new ArrayList<>();
                aValiderList.add(pendingVisa);

                if (isHcee || isHcep || isHc) {
                    aValiderList.add(root.get("statutWorkflow").in(StatutAT.AT_REDIGEE, StatutAT.SOUMISE));
                }
                if (isCeee) {
                    aValiderList.add(root.get("statutWorkflow").in(StatutAT.AT_VALIDEE, StatutAT.VALIDEE));
                }

                predicates.add(cb.or(aValiderList.toArray(new Predicate[0])));
            }

            // 2. Filtre par statut (avec support des statuts et alias OCP)
            if (statut != null && !statut.isBlank() && !"TOUS".equalsIgnoreCase(statut)) {
                String cleanStatut = statut.trim().toUpperCase();
                List<StatutAT> matchingStatuts = new ArrayList<>();
                try {
                    matchingStatuts.add(StatutAT.valueOf(cleanStatut));
                } catch (IllegalArgumentException ignored) {}

                if ("SOUMISE".equals(cleanStatut)) {
                    if (!matchingStatuts.contains(StatutAT.AT_REDIGEE)) matchingStatuts.add(StatutAT.AT_REDIGEE);
                } else if ("VALIDEE".equals(cleanStatut)) {
                    if (!matchingStatuts.contains(StatutAT.AT_VALIDEE)) matchingStatuts.add(StatutAT.AT_VALIDEE);
                } else if ("INTERVENTION_EN_COURS".equals(cleanStatut) || "EN_COURS".equals(cleanStatut)) {
                    if (!matchingStatuts.contains(StatutAT.EN_COURS)) matchingStatuts.add(StatutAT.EN_COURS);
                    if (!matchingStatuts.contains(StatutAT.INTERVENTION_EN_COURS)) matchingStatuts.add(StatutAT.INTERVENTION_EN_COURS);
                } else if ("FIN_TRAVAUX_DECLAREE".equals(cleanStatut) || "DECLAREE_TERMINEE".equals(cleanStatut)) {
                    if (!matchingStatuts.contains(StatutAT.FIN_TRAVAUX_DECLAREE)) matchingStatuts.add(StatutAT.FIN_TRAVAUX_DECLAREE);
                    if (!matchingStatuts.contains(StatutAT.DECLAREE_TERMINEE)) matchingStatuts.add(StatutAT.DECLAREE_TERMINEE);
                } else if ("CLOTUREE".equals(cleanStatut) || "TRAVAUX_RECEPTIONES".equals(cleanStatut) || "RECEPTIONEES".equals(cleanStatut)) {
                    if (!matchingStatuts.contains(StatutAT.CLOTUREE)) matchingStatuts.add(StatutAT.CLOTUREE);
                    if (!matchingStatuts.contains(StatutAT.TRAVAUX_RECEPTIONES)) matchingStatuts.add(StatutAT.TRAVAUX_RECEPTIONES);
                    if (!matchingStatuts.contains(StatutAT.RECEPTIONEES)) matchingStatuts.add(StatutAT.RECEPTIONEES);
                } else if ("BROUILLON".equals(cleanStatut) || "DEMANDE_CREEE".equals(cleanStatut)) {
                    if (!matchingStatuts.contains(StatutAT.BROUILLON)) matchingStatuts.add(StatutAT.BROUILLON);
                    if (!matchingStatuts.contains(StatutAT.DEMANDE_CREEE)) matchingStatuts.add(StatutAT.DEMANDE_CREEE);
                }

                if (!matchingStatuts.isEmpty()) {
                    predicates.add(cb.or(
                        root.get("statut").in(matchingStatuts),
                        root.get("statutWorkflow").in(matchingStatuts)
                    ));
                }
            }

            // 3. Recherche textuelle (N° AT, objet, description, document source, intervenants)
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("numero")), searchPattern),
                    cb.like(cb.lower(root.get("objet")), searchPattern),
                    cb.like(cb.lower(root.get("descriptionTravaux")), searchPattern),
                    cb.like(cb.lower(root.get("numeroDocumentSource")), searchPattern),
                    cb.like(cb.lower(root.get("servicesIntervenants")), searchPattern),
                    cb.like(cb.lower(root.get("entreprisesIntervenantes")), searchPattern)
                ));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return atRepository.findAll(spec, pageable).map(this::mapToResponse);
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
        // Statut : autoriser l'édition UNIQUEMENT en phase initiale (BROUILLON / DEMANDE_CREEE / CLASSIFICATION_EFFECTUEE).
        // Dès que l'AT est signée et soumise par le CEEP, AUCUNE personne ne peut la modifier (même le CEEP).
        StatutAT st = at.getStatut();
        StatutAT stWf = at.getStatutWorkflow();
        if (st == StatutAT.SOUMISE || st == StatutAT.VALIDEE || st == StatutAT.CLOTUREE || st == StatutAT.ANNULEE
                || st == StatutAT.ARCHIVEE || stWf == StatutAT.AT_REDIGEE || stWf == StatutAT.VISITE_REALISEE) {
            throw new BusinessException("Cette Autorisation de Travail a déjà été signée et transmise. Le formulaire est verrouillé et ne peut plus être modifié.");
        }

        // Seul le CEEP propriétaire du brouillon peut modifier le contenu initial de l'AT.
        // Le CEEE ne modifie jamais le formulaire : il ne fait qu'accuser réception puis signer.
        boolean estCeep = RoleUtils.userHasRolePattern(currentUser, "CEEP") || RoleUtils.userHasRolePattern(currentUser, "ADMIN");
        boolean estProprietaire = at.getProprietaireBrouillon() == null
                || at.getProprietaireBrouillon().getId().equals(currentUser.getId());
        if (!estCeep || !estProprietaire) {
            throw new com.ocp.at.exception.ForbiddenException(
                    "Seul le CEEP rédacteur de cette AT peut modifier ce formulaire.");
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

        // Document Source (DI / OT / BT)
        if (request.getTypeDocumentSource() != null && !request.getTypeDocumentSource().isBlank()) {
            try {
                String docType = request.getTypeDocumentSource().trim().toUpperCase();
                if ("DI".equals(docType)) {
                    at.setTypeDocumentSource(TypeDocumentSource.DI);
                } else if ("OT".equals(docType)) {
                    at.setTypeDocumentSource(TypeDocumentSource.OT);
                } else if ("BT".equals(docType)) {
                    at.setTypeDocumentSource(TypeDocumentSource.BT);
                }

                String numDoc = request.getDocumentSourceNumero();
                if (numDoc != null && !numDoc.isBlank()) {
                    at.setNumeroDocumentSource(numDoc.trim());
                } else if (at.getNumeroDocumentSource() == null || at.getNumeroDocumentSource().isBlank()) {
                    int randomNum = 1000 + (int)(Math.random() * 9000);
                    int year = LocalDate.now().getYear();
                    at.setNumeroDocumentSource(docType + "-" + year + "-" + randomNum);
                }

                if (request.getDocumentSourceId() != null && !request.getDocumentSourceId().isBlank()) {
                    String docId = request.getDocumentSourceId();
                    if ("DI".equals(docType)) {
                        diRepository.findById(docId).ifPresent(at::setDemandeIntervention);
                    } else if ("OT".equals(docType)) {
                        otRepository.findById(docId).ifPresent(at::setOrdreTravail);
                    } else if ("BT".equals(docType)) {
                        btRepository.findById(docId).ifPresent(at::setBonTravail);
                    }
                }
            } catch (Exception e) {
                log.warn("autoSave document source error: {}", e.getMessage());
            }
        }

        // Visite Préalable (§8.2) - mise à jour ou création sur le document source
        if (request.getLatitude() != null || request.getLongitude() != null || request.getVisiteCommentaire() != null || Boolean.TRUE.equals(request.getVisiteEffectuee())) {
            try {
                VisitePrealable vp = null;
                if (at.getDemandeIntervention() != null) {
                    vp = at.getDemandeIntervention().getVisitePrealable();
                    if (vp == null) {
                        vp = new VisitePrealable();
                        at.getDemandeIntervention().setVisitePrealable(vp);
                    }
                } else if (at.getOrdreTravail() != null) {
                    vp = at.getOrdreTravail().getVisitePrealable();
                    if (vp == null) {
                        vp = new VisitePrealable();
                        at.getOrdreTravail().setVisitePrealable(vp);
                    }
                } else if (at.getBonTravail() != null) {
                    vp = at.getBonTravail().getVisitePrealable();
                    if (vp == null) {
                        vp = new VisitePrealable();
                        at.getBonTravail().setVisitePrealable(vp);
                    }
                }
                if (vp != null) {
                    if (request.getLatitude() != null) vp.setLatitude(request.getLatitude());
                    if (request.getLongitude() != null) vp.setLongitude(request.getLongitude());
                    if (request.getVisiteCommentaire() != null) vp.setCommentaire(request.getVisiteCommentaire());
                    if (request.getVisiteEffectuee() != null) vp.setEffectuee(request.getVisiteEffectuee());
                    visiteRepository.save(vp);
                }
            } catch (Exception e) {
                log.warn("autoSave visitePrealable error: {}", e.getMessage());
            }
        }

        // Lier zone exécutante (E) pour résoudre/notifier les CEEE
        resoudreEtAffecterZones(at, request);
        verifierServicesDifferents(at, request);

        // Cases formulaire → colonnes JSON (source de vérité)
        persistFormCheckboxes(at, request);

        AutorisationTravail savedAt = atRepository.save(at);

        // Sync PermisDocument (agent IA) dans la MÊME transaction que l'autosave,
        // pour éviter toute race condition entre l'écriture de formPermisIds et sa relecture
        // (avant : le frontend déclenchait un appel réseau séparé et débouncé, qui pouvait
        // lire une valeur de formPermisIds pas encore commitée en base → PermisDocument
        // fantôme bloqué en EN_ATTENTE_UPLOAD → soumission bloquée indéfiniment).
        try {
            permisDocumentService.initialiserPermisRequis(savedAt.getId());
        } catch (Exception e) {
            log.warn("Sync PermisDocument (autoSave) AT {} : {}", savedAt.getId(), e.getMessage());
        }

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
    
    @Override
    @Transactional
    public AutorisationTravailResponse accuserReceptionCeee(String id) {
        AutorisationTravail at = getEntityById(id);
        Utilisateur currentUser = getCurrentUser();

        // 1. Le créateur/propriétaire (CEEP) ne peut PAS accuser réception à la place du CEEE
        if (at.getProprietaireBrouillon() != null && at.getProprietaireBrouillon().getId().equals(currentUser.getId())) {
            throw new BusinessException("En tant que CEEP (créateur/propriétaire), vous ne pouvez pas accuser réception à la place du CEEE. L'accusé de réception et le visa doivent être effectués par le CEEE du service exécutant depuis son propre compte.");
        }

        // 2. Vérifier que l'utilisateur a le rôle CE/CEEE
        if (!RoleUtils.userHasRolePattern(currentUser, "CE")) {
            throw new com.ocp.at.exception.ForbiddenException("Seul un Chef d'Équipe du service exécutant (CEEE) peut accuser réception de cette AT.");
        }

        at.setDateReceptionCeee(LocalDateTime.now());
        AutorisationTravail saved = atRepository.save(at);
        enregistrerHistorique(saved, TypeActionAT.AUTO_SAVE, saved.getStatut(), saved.getStatut(),
                "AT reçue et accusée par le CEEE " + currentUser.getNom() + " (" + currentUser.getEmail() + ")");
        return mapToResponse(saved);
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

        // Garde IA : vérifier que tous les permis cochés en section E sont validés
        if (at.getFormPermisIds() != null && !at.getFormPermisIds().isBlank()
                && !at.getFormPermisIds().equals("[]") && !at.getFormPermisIds().equals("null")
                && !permisDocumentService.tousPermisValides(id)) {
            throw new com.ocp.at.exception.BusinessException(
                "Soumission impossible : des permis requis ne sont pas encore validés "
                + "par l'agent IA. Validez tous les permis cochés en section E.");
        }

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
        verifierServicesDifferents(at, null);

        // Dates : avertissement souple - si absentes, on ne bloque plus systématiquement
        // (le formulaire papier peut être complété terrain §8.3)
        if (at.getDateDebut() == null && at.getDateFin() == null
                && at.getHeureDebut() == null && at.getHeureFin() == null) {
            log.warn("Soumission AT {} sans dates/heures renseignées", id);
        }

        // Permis : ne bloquer QUE s'il existe au moins un permis obligatoire non conforme
        // Resynchronise d'abord Permis.statutVerification depuis les résultats de l'agent IA
        // (PermisDocument), au cas où un document aurait été validé avant l'ajout de la
        // synchro automatique dans PermisDocumentServiceImpl.
        try {
            permisDocumentService.resynchroniserStatutsPermis(id);
        } catch (Exception e) {
            log.warn("Resynchronisation statuts permis AT {} : {}", id, e.getMessage());
        }
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
            log.warn("Transition SOUMISSION non listée pour {} - forcée vers AT_REDIGEE", ancienStatut);
        }
        // §8.3 - AT_REDIGEE (legacy statut = SOUMISE)
        at.setStatut(StatutAT.SOUMISE);
        at.setStatutWorkflow(nouvelEtat);
        at.setEtatVerrou(EtatVerrou.LIBRE);
        at.setDateLiberationVerrou(LocalDateTime.now());
        
        AutorisationTravail savedAt = atRepository.save(at);
        
        enregistrerHistorique(savedAt, TypeActionAT.SOUMISSION, ancienStatut, nouvelEtat, "Soumission AT - workflow standard (visite / rédaction)");

        // Notifications best-effort - parcours standard CE → HM → HC
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
                        "Nouvelle intervention à signer - AT " + savedAt.getNumero(),
                        String.format(
                            "Une intervention est planifiée sur votre service.\nObjet : %s\nZone : %s\nDate : %s (%s → %s)\nEntreprise(s) intervenante(s) : %s\nVous devez accuser réception puis signer la case Visa CEEE.",
                            savedAt.getObjet(),
                            savedAt.getZoneProprietaire() != null ? savedAt.getZoneProprietaire().getNomZone() : "N/A",
                            savedAt.getDateDebut(), savedAt.getHeureDebut(), savedAt.getHeureFin(),
                            savedAt.getEntreprisesIntervenantes()
                        ),
                        "ACTION",
                        "/at/" + savedAt.getId() + "/signature-ceee"
                );
            }
            log.info("Notifs CEEE: {} destinataire(s) pour AT {}", ceees.size(), savedAt.getNumero());
        } catch (Exception e) {
            log.warn("Notif CEEE: {}", e.getMessage());
        }

        // 2) HM (HMEP / HMEE) - garants terrain
        try {
            notificationService.sendNotificationToRoleForAt("HMEP", savedAt, "AT soumise - garantie HMEP",
                    "L'AT " + savedAt.getNumero() + " nécessite votre garantie (Haute Maîtrise Propriétaire).",
                    "ACTION", lienValider);
            notificationService.sendNotificationToRoleForAt("HMEE", savedAt, "AT soumise - garantie HMEE",
                    "L'AT " + savedAt.getNumero() + " nécessite votre garantie (Haute Maîtrise Exécutante).",
                    "ACTION", lienValider);
        } catch (Exception e) {
            log.warn("Notif HM: {}", e.getMessage());
        }

        // 3) HC (HCEE / HCEP) - validation / pilotage
        try {
            notificationService.sendNotificationToRoleForAt("HCEE", savedAt, "AT soumise - validation HCEE",
                    "L'AT " + savedAt.getNumero() + " est soumise. Garantir / valider le dossier.",
                    "ACTION", lienValider);
            notificationService.sendNotificationToRoleForAt("HCEP", savedAt, "AT soumise (info HCEP)",
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

        StatutAT ancienStatut = statutEffectif(at);
        try {
            workflowService.verifierTransition(ancienStatut, TypeActionAT.VALIDATION);
        } catch (Exception e) {
            log.warn("Validation AT permissive pour l'id {} depuis le statut {}", id, ancienStatut);
        }

        StatutAT nouvelEtat = StatutAT.VALIDEE;
        at.setStatut(StatutAT.VALIDEE);
        at.setStatutWorkflow(nouvelEtat);

        // Création / mise à jour du Visa de validation si nécessaire
        try {
            Utilisateur currentUser = getCurrentUser();
            if (currentUser != null) {
                Visa visa = Visa.builder()
                        .autorisationTravail(at)
                        .utilisateur(currentUser)
                        .dateVisa(LocalDateTime.now())
                        .statut(StatutVisa.VALIDATION)
                        .build();
                visaRepository.save(visa);
            }
        } catch (Exception ignored) {}

        AutorisationTravail savedAt = atRepository.save(at);

        enregistrerHistorique(savedAt, TypeActionAT.VALIDATION, ancienStatut, nouvelEtat, "AT validée - §8.3 VALIDEE");
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
        
        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(at.getProprietaireBrouillon(), "AT Annulée", "L'AT " + savedAt.getNumero() + " a été annulée.", "WARNING", "/autorisations/" + savedAt.getId());
        }
        notificationService.sendNotificationToRoleForAt("CEEE", savedAt, "AT " + savedAt.getNumero() + " Annulée", "L'AT " + savedAt.getNumero() + " a été annulée.", "WARNING", "/autorisations/" + savedAt.getId());

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
        
        enregistrerHistorique(savedAt, TypeActionAT.RECEPTION_CONJOINTE, ancienStatut, StatutAT.TRAVAUX_RECEPTIONES, "§8.5 - Clôture / réception AT");
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
    @Transactional(readOnly = true)
    public com.ocp.at.dto.response.ReadinessCheckResponse getInterventionReadiness(String id) {
        return interventionReadinessService.checkInterventionReadiness(id);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse demarrerIntervention(String id) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.DEBUT_INTERVENTION);

        // 1. Pré-check déterministe obligatoire
        com.ocp.at.dto.response.ReadinessCheckResponse readiness = interventionReadinessService.checkInterventionReadiness(id);
        if (!Boolean.TRUE.equals(readiness.getReady())) {
            List<String> failedChecks = readiness.getChecks().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getBlocking()) && !Boolean.TRUE.equals(c.getPassed()))
                    .map(c -> c.getLabel() + " : " + c.getMessage())
                    .collect(Collectors.toList());
            throw new BusinessException("Impossible de démarrer l'intervention : préconditions non satisfaites : " + String.join(", ", failedChecks));
        }

        // 2. Empêcher le double démarrage
        if (at.getDateDemarrage() != null && ancienStatut == StatutAT.INTERVENTION_EN_COURS) {
            throw new BusinessException("L'intervention a déjà été démarrée le " + at.getDateDemarrage());
        }

        // 3. Enregistrer l'utilisateur CEEE et le timestamp serveur réel
        Utilisateur ceee = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        at.setDateDemarrage(now);
        at.setCeee(ceee);

        StatutAT nouvelEtat = StatutAT.INTERVENTION_EN_COURS;
        at.setStatutWorkflow(nouvelEtat);
        at.setStatut(StatutAT.INTERVENTION_EN_COURS);
        AutorisationTravail savedAt = atRepository.save(at);

        enregistrerHistorique(savedAt, TypeActionAT.DEBUT_INTERVENTION, ancienStatut, nouvelEtat, "§8 - Démarrage travaux par CEEE " + ceee.getNom() + " (garants HCEE/HMEE)");
        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(at.getProprietaireBrouillon(), "Intervention Démarrée", "L'intervention sur l'AT " + savedAt.getNumero() + " a démarré le " + now + ".", "INFO", "/autorisations/" + savedAt.getId());
        }
        notificationService.sendNotificationToRoleForAt("CEEP", savedAt, "Intervention Démarrée - AT " + savedAt.getNumero(), "L'intervention sur l'AT " + savedAt.getNumero() + " a démarré.", "INFO", "/autorisations/" + savedAt.getId());

        log.info("Intervention démarrée pour AT {} par CEEE {}", savedAt.getNumero(), ceee.getEmail());
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse declarerFinTravaux(String id) {
        return declarerFinTravaux(id, null);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse declarerFinTravaux(String id, com.ocp.at.dto.request.EndInterventionRequest request) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        workflowService.verifierTransition(ancienStatut, TypeActionAT.DECLARATION_FIN);

        Utilisateur ceee = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        at.setDateFinReelle(now);

        StatutAT nouvelEtat = StatutAT.FIN_TRAVAUX_DECLAREE;
        at.setStatutWorkflow(nouvelEtat);
        at.setStatut(StatutAT.FIN_TRAVAUX_DECLAREE);

        // Mettre à jour ou initialiser l'entité ReceptionTravaux avec les données de fin de chantier
        ReceptionTravaux reception = receptionRepository.findByAutorisationTravailId(at.getId()).orElse(null);
        if (reception == null) {
            reception = ReceptionTravaux.builder()
                    .autorisationTravail(at)
                    .responsable(ceee)
                    .dateDebutTravauxReelle(at.getDateDemarrage())
                    .dateFinTravauxReelle(now)
                    .travauxRealises(request != null ? request.getTravauxRealises() : at.getObjet())
                    .zoneNettoyee(request != null ? request.getZoneNettoyee() : true)
                    .equipementRemisEnService(request != null ? request.getProtectionsRetablies() : true)
                    .observations(request != null ? request.getObservations() : null)
                    .build();
        } else {
            reception.setDateFinTravauxReelle(now);
            if (request != null) {
                reception.setTravauxRealises(request.getTravauxRealises());
                reception.setZoneNettoyee(request.getZoneNettoyee());
                reception.setEquipementRemisEnService(request.getProtectionsRetablies());
                reception.setObservations(request.getObservations());
            }
        }
        receptionRepository.save(reception);

        AutorisationTravail savedAt = atRepository.save(at);

        String commHisto = request != null && request.getTravauxRealises() != null
                ? "§8.5 - Fin des travaux déclarée par CEEE " + ceee.getNom() + " : " + request.getTravauxRealises()
                : "§8.5 - Fin des travaux déclarée (CEEE E, CEEP I)";
        enregistrerHistorique(savedAt, TypeActionAT.DECLARATION_FIN, ancienStatut, nouvelEtat, commHisto);

        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(at.getProprietaireBrouillon(), "Fin des Travaux Déclarée - AT " + savedAt.getNumero(), "Le CEEE a déclaré la fin des travaux sur l'AT " + savedAt.getNumero() + ". L'AT est prête pour réception conjointe.", "ACTION", "/autorisations/" + savedAt.getId());
        }
        notificationService.sendNotificationToRoleForAt("CEEP", savedAt, "AT " + savedAt.getNumero() + " prête pour réception", "Le CEEE a déclaré la fin des travaux. Veuillez procéder à la réception conjointe.", "ACTION", "/receptions?atId=" + savedAt.getId());

        log.info("Fin des travaux déclarée pour AT {} par CEEE {}", savedAt.getNumero(), ceee.getEmail());
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
        enregistrerHistorique(savedAt, TypeActionAT.VISITE_CHANTIER, ancienStatut, nouvelEtat, "§8.2 - Visite préalable chantier réalisée");
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
        enregistrerHistorique(savedAt, TypeActionAT.REDACTION_AT, ancienStatut, nouvelEtat, "§8.3 - AT et permis rédigés/signés sur le terrain");
        return mapToResponse(savedAt);
    }

    @Override
    @Transactional
    public AutorisationTravailResponse reconduireAT(String id, boolean depasse24h) {
        AutorisationTravail at = getEntityById(id);
        StatutAT ancienStatut = statutEffectif(at);
        if (depasse24h) {
            // §8.4 - > 24h : nouvelle visite obligatoire
            workflowService.verifierTransition(StatutAT.AT_RECONDUITE, TypeActionAT.VISITE_CHANTIER);
            at.setStatutWorkflow(StatutAT.VISITE_REALISEE);
            at.setVersion(at.getVersion() == null ? 2 : at.getVersion() + 1);
            AutorisationTravail savedAt = atRepository.save(at);
            enregistrerHistorique(savedAt, TypeActionAT.VISITE_CHANTIER, ancienStatut, StatutAT.VISITE_REALISEE,
                    "§8.4 - Dépassement 24h : nouvelle visite chantier obligatoire");
            return mapToResponse(savedAt);
        }
        workflowService.verifierTransition(ancienStatut, TypeActionAT.RECONDUCTION);
        at.setStatutWorkflow(StatutAT.AT_RECONDUITE);
        at.setStatut(StatutAT.RENOUVELEE);
        at.setVersion(at.getVersion() == null ? 2 : at.getVersion() + 1);
        AutorisationTravail savedAt = atRepository.save(at);
        enregistrerHistorique(savedAt, TypeActionAT.RECONDUCTION, ancienStatut, StatutAT.AT_RECONDUITE,
                "§8.4 - Reconduction AT (début de poste)");
        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(at.getProprietaireBrouillon(), "AT Reconduite", "L'AT " + savedAt.getNumero() + " a été reconduite (Version " + savedAt.getVersion() + ").", "INFO", "/autorisations/" + savedAt.getId());
        }
        notificationService.sendNotificationToRoleForAt("CEEE", savedAt, "AT " + savedAt.getNumero() + " Reconduite", "L'AT " + savedAt.getNumero() + " a été reconduite pour un nouveau poste de travail.", "INFO", "/autorisations/" + savedAt.getId());
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
                "§8.4 - Incident/changement condition : " + (motif != null ? motif : "retour visite obligatoire"));
        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(at.getProprietaireBrouillon(), "Incident signalé - AT " + savedAt.getNumero(), "Un incident ou changement de condition a été signalé sur l'AT " + savedAt.getNumero() + ". Nouvelle visite requise.", "WARNING", "/autorisations/" + savedAt.getId());
        }
        notificationService.sendNotificationToRoleForAt("CEEE", savedAt, "Incident signalé - AT " + savedAt.getNumero(), "Un incident ou changement de condition a été signalé sur l'AT " + savedAt.getNumero() + ".", "WARNING", "/autorisations/" + savedAt.getId());
        notificationService.sendNotificationToRole("ADMIN", "Incident signalé - AT " + savedAt.getNumero(), "Un incident ou changement de condition a été signalé sur l'AT " + savedAt.getNumero() + (motif != null ? " : " + motif : "") + ".", "WARNING", "/autorisations/" + savedAt.getId());
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
                "§8.5 - Réception conjointe CEEP+CEEE, clôture AT et permis");
        if (at.getProprietaireBrouillon() != null) {
            notificationService.createNotification(at.getProprietaireBrouillon(), "Travaux Réceptionnés", "La réception conjointe de l'AT " + savedAt.getNumero() + " a été validée avec succès.", "SUCCESS", "/autorisations/" + savedAt.getId());
        }
        notificationService.sendNotificationToRoleForAt("CEEE", savedAt, "Travaux Réceptionnés - AT " + savedAt.getNumero(), "La réception conjointe des travaux est validée. L'AT est clôturée.", "SUCCESS", "/autorisations/" + savedAt.getId());
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
            // 1. Résolution de la zone propriétaire (P)
            if (request.getZoneProprietaireId() != null && !request.getZoneProprietaireId().isBlank()) {
                zoneRepository.findById(request.getZoneProprietaireId()).ifPresent(at::setZoneProprietaire);
            } else if (request.getZoneProprietaireNom() != null && !request.getZoneProprietaireNom().isBlank()) {
                String nomP = request.getZoneProprietaireNom().trim();
                zoneRepository.findAll().stream()
                        .filter(z -> nomP.equalsIgnoreCase(z.getNomZone()) || nomP.equalsIgnoreCase(z.getCodeZone()))
                        .findFirst()
                        .ifPresent(at::setZoneProprietaire);
            }
            if (at.getZoneProprietaire() == null && current.getService() != null
                    && current.getService().getZone() != null) {
                at.setZoneProprietaire(current.getService().getZone());
            }

            // 2. Résolution de la zone exécutante (E)
            if (request.getZoneExecutanteId() != null && !request.getZoneExecutanteId().isBlank()) {
                zoneRepository.findById(request.getZoneExecutanteId()).ifPresent(at::setZoneExecutante);
            } else if (request.getZoneExecutanteNom() != null && !request.getZoneExecutanteNom().isBlank()) {
                String nomE = request.getZoneExecutanteNom().trim();
                zoneRepository.findAll().stream()
                        .filter(z -> nomE.equalsIgnoreCase(z.getNomZone()) || nomE.equalsIgnoreCase(z.getCodeZone()))
                        .findFirst()
                        .ifPresent(at::setZoneExecutante);
            }

            // 3. Résolution du service intervenant
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
                if (at.getZoneExecutante() == null && svc.getZone() != null) {
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

    private void verifierServicesDifferents(AutorisationTravail at, AutoSaveRequest request) {
        Utilisateur current = getCurrentUser();
        String userServiceId = current != null && current.getService() != null ? current.getService().getId() : null;
        String userServiceName = current != null && current.getService() != null ? current.getService().getNomService() : null;

        String executantServiceId = request != null ? request.getServiceIntervenantId() : null;
        String executantServiceName = request != null && request.getServicesIntervenants() != null ? request.getServicesIntervenants() : at.getServicesIntervenants();

        if (userServiceId != null && executantServiceId != null && userServiceId.equals(executantServiceId)) {
            throw new BusinessException("Une Autorisation de Travail ne peut pas être établie au sein d'un même service. Le service demandeur/propriétaire et le service exécutant doivent être différents.");
        }
        if (userServiceName != null && executantServiceName != null && !executantServiceName.isBlank()
                && userServiceName.trim().equalsIgnoreCase(executantServiceName.trim())) {
            throw new BusinessException("Une Autorisation de Travail ne peut pas être établie au sein d'un même service. Le service demandeur (" + userServiceName + ") et le service exécutant (" + executantServiceName + ") doivent être différents.");
        }

        // Contrainte stricte §2 Standard S-HSE-SEC-31 - vérification par zones
        if (at.getZoneProprietaire() != null && at.getZoneExecutante() != null) {
            if (at.getZoneProprietaire().getId().equals(at.getZoneExecutante().getId())) {
                throw new BusinessException(
                    "La zone propriétaire et la zone exécutante doivent être différentes " +
                    "(Standard S-HSE-SEC-31 §2 - une AT ne peut pas lier deux zones identiques)."
                );
            }
            // Chercher les services associés à chaque zone
            java.util.List<com.ocp.at.entity.Service> servicesZoneP = serviceRepository.findAll().stream()
                    .filter(s -> s.getZone() != null && s.getZone().getId().equals(at.getZoneProprietaire().getId()))
                    .collect(java.util.stream.Collectors.toList());
            java.util.List<com.ocp.at.entity.Service> servicesZoneE = serviceRepository.findAll().stream()
                    .filter(s -> s.getZone() != null && s.getZone().getId().equals(at.getZoneExecutante().getId()))
                    .collect(java.util.stream.Collectors.toList());
            if (!servicesZoneP.isEmpty() && !servicesZoneE.isEmpty()) {
                boolean memeService = servicesZoneP.stream()
                        .anyMatch(sP -> servicesZoneE.stream().anyMatch(sE -> sE.getId().equals(sP.getId())));
                if (memeService) {
                    throw new BusinessException(
                        "Le service propriétaire et le service exécutant doivent être différents " +
                        "(Standard S-HSE-SEC-31 §2 - une AT ne peut pas lier deux zones du même service)."
                    );
                }
            }
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
        // Bloquer uniquement les statuts vraiment non-finaux
        if (st == StatutAT.BROUILLON || st == StatutAT.REJETEE || st == StatutAT.ANNULEE
                || st == StatutAT.DEMANDE_CREEE || st == StatutAT.EN_VISITE_REDACTION) {
            motifs.add("L'AT doit être au minimum soumise avant l'export PDF (statut actuel : " + (st != null ? st.name() : "N/A") + ").");
        }

        // Vérification des permis obligatoires non conformes uniquement
        try {
            List<com.ocp.at.entity.Permis> permisList = permisRepository.findByAutorisationTravailId(at.getId());
            for (com.ocp.at.entity.Permis p : permisList) {
                if (Boolean.TRUE.equals(p.getEstObligatoire())) {
                    if (p.getStatutVerification() != com.ocp.at.entity.enums.StatutPermis.CONFORME) {
                        String nomPermis = p.getTypePermis() != null ? p.getTypePermis().getNom() : "Permis";
                        motifs.add("Permis obligatoire non conforme : " + nomPermis + " (" + p.getStatutVerification() + ")");
                    }
                }
            }
        } catch (Exception ignored) {}

        return motifs;
    }

    public boolean hasSignedAsRole(List<Visa> visas, AutorisationTravail at, String targetRole) {
        if (visas == null || visas.isEmpty()) return false;

        return visas.stream().anyMatch(v -> {
            if (!isVisaPositif(v)) return false;
            Utilisateur u = v.getUtilisateur();
            if (u == null) return false;

            String comment = v.getCommentaire() != null ? v.getCommentaire().toUpperCase() : "";

            // Check 1: Le commentaire mentionne explicitement le rôle
            if (comment.contains(targetRole.toUpperCase())) return true;

            // Check 2: L'utilisateur possède le rôle spécifique
            if (u.getRoles() != null && u.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().equalsIgnoreCase(targetRole))) {
                return true;
            }

            // Check 3: Résolution par rôle générique HC / HM et position P/E selon le territoire de l'AT
            PositionAT pos = atContextService != null ? atContextService.resolvePosition(u, at) : PositionAT.AUCUNE;
            boolean isHc = RoleUtils.userHasRolePattern(u, "HC");
            boolean isHm = RoleUtils.userHasRolePattern(u, "HM");
            boolean isAdmin = u.getRoles() != null && u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getNom()));

            switch (targetRole.toUpperCase()) {
                case "HCEP":
                    return (isHc && pos == PositionAT.PROPRIETAIRE) || (isAdmin && comment.contains("HCEP"));
                case "HCEE":
                    return (isHc && (pos == PositionAT.EXECUTANT || pos == PositionAT.AUCUNE)) || (isAdmin && comment.contains("HCEE"));
                case "HMEP":
                    return (isHm && (pos == PositionAT.PROPRIETAIRE || pos == PositionAT.AUCUNE)) || (isAdmin && comment.contains("HMEP"));
                case "HMEE":
                    return (isHm && pos == PositionAT.EXECUTANT) || (isAdmin && comment.contains("HMEE"));
                default:
                    return false;
            }
        });
    }

    private boolean isVisaPositif(Visa v) {
        if (v == null || v.getStatut() == null) return false;
        StatutVisa s = v.getStatut();
        return s == StatutVisa.VALIDE || s == StatutVisa.VALIDATION || s == StatutVisa.SIGNATURE;
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
                .flatMap(idOrEmail -> utilisateurRepository.findById(idOrEmail).or(() -> utilisateurRepository.findByEmail(idOrEmail)))
                .orElseGet(() -> SecurityUtils.getCurrentUserLogin()
                        .flatMap(utilisateurRepository::findByEmail)
                        .orElseThrow(() -> new BusinessException("Utilisateur non authentifié")));
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

    /**
     * Contrainte inter-services obligatoire (Standard S-HSE-SEC-31 §2) :
     * une AT ne peut jamais lier deux zones du même service.
     */
    private void verifierServicesIntervenantsDifferents(AutorisationTravail at) {
        if (at.getZoneProprietaire() == null || at.getZoneExecutante() == null) {
            return; // pas encore assignées, on laisse passer (peut être un brouillon)
        }
        Zone zP = at.getZoneProprietaire();
        Zone zE = at.getZoneExecutante();

        // Même zone → interdit
        if (zP.getId().equals(zE.getId())) {
            throw new BusinessException(
                "La zone propriétaire et la zone exécutante doivent être différentes (§8 OCP)."
            );
        }

        // Accès au service via le repository (zones peuvent avoir un service associé au niveau du territoire)
        // On recherche les services qui déclarent cette zone via la table services
        java.util.List<com.ocp.at.entity.Service> servicesP = serviceRepository.findAll().stream()
                .filter(s -> s.getZone() != null && s.getZone().getId().equals(zP.getId()))
                .collect(java.util.stream.Collectors.toList());
        java.util.List<com.ocp.at.entity.Service> servicesE = serviceRepository.findAll().stream()
                .filter(s -> s.getZone() != null && s.getZone().getId().equals(zE.getId()))
                .collect(java.util.stream.Collectors.toList());

        // Si les deux zones appartiennent exactement aux mêmes services → interdit
        if (!servicesP.isEmpty() && !servicesE.isEmpty()) {
            boolean memeService = servicesP.stream()
                    .anyMatch(sP -> servicesE.stream().anyMatch(sE -> sE.getId().equals(sP.getId())));
            if (memeService) {
                throw new BusinessException(
                    "Le service propriétaire et le service exécutant doivent être différents " +
                    "(Standard S-HSE-SEC-31 §2 - une AT ne peut pas lier deux zones du même service)."
                );
            }
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
        if (at.getTypeDocumentSource() != null) {
            response.setTypeDocumentSource(at.getTypeDocumentSource().name());
        }
        if (at.getNumeroDocumentSource() != null) {
            response.setDocumentSourceNumero(at.getNumeroDocumentSource());
        }
        if (at.getDemandeIntervention() != null) {
            response.setTypeDocumentSource("DI");
            response.setDocumentSourceId(at.getDemandeIntervention().getId());
            if (response.getDocumentSourceNumero() == null) {
                response.setDocumentSourceNumero(at.getDemandeIntervention().getNumero());
            }
        } else if (at.getOrdreTravail() != null) {
            response.setTypeDocumentSource("OT");
            response.setDocumentSourceId(at.getOrdreTravail().getId());
            if (response.getDocumentSourceNumero() == null) {
                response.setDocumentSourceNumero(at.getOrdreTravail().getNumero());
            }
        } else if (at.getBonTravail() != null) {
            response.setTypeDocumentSource("BT");
            response.setDocumentSourceId(at.getBonTravail().getId());
            if (response.getDocumentSourceNumero() == null) {
                response.setDocumentSourceNumero(at.getBonTravail().getNumero());
            }
        }

        if (at.getZoneProprietaire() != null) {
            response.setZoneProprietaireId(at.getZoneProprietaire().getId());
            response.setZoneProprietaireNom(at.getZoneProprietaire().getNomZone());
        }
        if (at.getZoneExecutante() != null) {
            response.setZoneExecutanteId(at.getZoneExecutante().getId());
            response.setZoneExecutanteNom(at.getZoneExecutante().getNomZone());
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

        // Noms CEEP & CEEE pour la section G
        if (at.getProprietaireBrouillon() != null) {
            response.setG1NomCeep(at.getProprietaireBrouillon().getPrenom() + " " + at.getProprietaireBrouillon().getNom());
        }
        response.setDateReceptionCeee(at.getDateReceptionCeee());

        // Récupération CEEE chef d'équipe
        if (atContextService != null) {
            try {
                List<Utilisateur> ceees = atContextService.findChefsEquipeExecutants(at.getId());
                if (!ceees.isEmpty()) {
                    String names = ceees.stream()
                            .map(u -> u.getPrenom() + " " + u.getNom())
                            .collect(Collectors.joining(" / "));
                    response.setG1NomCeee(names);
                }
            } catch (Exception ignored) {}
        }

        // Visite Préalable (§8.2) - extraite du document source ou des données d'inspection
        VisitePrealable vp = null;
        if (at.getDemandeIntervention() != null) {
            vp = at.getDemandeIntervention().getVisitePrealable();
        } else if (at.getOrdreTravail() != null) {
            vp = at.getOrdreTravail().getVisitePrealable();
        } else if (at.getBonTravail() != null) {
            vp = at.getBonTravail().getVisitePrealable();
        }
        if (vp != null) {
            response.setLatitude(vp.getLatitude());
            response.setLongitude(vp.getLongitude());
            response.setVisiteCommentaire(vp.getCommentaire());
            response.setVisiteEffectuee(vp.isEffectuee());
            if (vp.getPhotos() != null && !vp.getPhotos().isEmpty()) {
                response.setPhotoPath(vp.getPhotos().get(0).getPath());
            }
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