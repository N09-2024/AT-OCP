package com.ocp.at.service.impl;

import com.ocp.at.dto.request.VisitePrealableRequest;
import com.ocp.at.dto.response.PhotoResponse;
import com.ocp.at.dto.response.VisitePrealableResponse;
import com.ocp.at.entity.*;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PhotoMapper;
import com.ocp.at.mapper.VisitePrealableMapper;
import com.ocp.at.repository.*;
import com.ocp.at.service.VisitePrealableService;
import com.ocp.at.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitePrealableServiceImpl implements VisitePrealableService {

    private final VisitePrealableRepository visiteRepository;
    private final PhotoRepository photoRepository;
    private final DemandeInterventionRepository diRepository;
    private final OrdreTravailRepository otRepository;
    private final BonTravailRepository btRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RisqueRepository risqueRepository;
    private final StorageService storageService;
    private final VisitePrealableMapper visiteMapper;
    private final PhotoMapper photoMapper;

    @Override
    @Transactional
    public VisitePrealableResponse create(VisitePrealableRequest request) {
        log.info("Création d'une visite préalable pour {} {}", request.getTypeDocumentSource(), request.getDocumentSourceId());

        String type = request.getTypeDocumentSource() != null ? request.getTypeDocumentSource().toUpperCase() : null;

        // Vérification unicité : un document ne peut avoir qu'une visite
        if (type != null && request.getDocumentSourceId() != null) {
            switch (type) {
                case "DI" -> {
                    if (visiteRepository.existsForDI(request.getDocumentSourceId()))
                        throw new BusinessException("Une visite préalable existe déjà pour cette DI");
                }
                case "OT" -> {
                    if (visiteRepository.existsForOT(request.getDocumentSourceId()))
                        throw new BusinessException("Une visite préalable existe déjà pour cet OT");
                }
                case "BT" -> {
                    if (visiteRepository.existsForBT(request.getDocumentSourceId()))
                        throw new BusinessException("Une visite préalable existe déjà pour ce BT");
                }
                default -> throw new BusinessException("Type de document invalide : " + request.getTypeDocumentSource() + ". Valeurs acceptées : DI, OT, BT");
            }
        }

        VisitePrealable visite = VisitePrealable.builder()
                .dateHeureDebut(request.getDateHeureDebut() != null ? request.getDateHeureDebut() : LocalDateTime.now())
                .dateHeureFin(request.getDateHeureFin())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .commentaire(request.getCommentaire())
                .effectuee(false)
                .build();

        // Association du visiteur
        if (request.getVisiteurId() != null) {
            Utilisateur visiteur = utilisateurRepository.findById(request.getVisiteurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Visiteur non trouvé : " + request.getVisiteurId()));
            visite.setVisiteur(visiteur);
        }

        // Risques identifiés (optionnel à la création)
        if (request.getRisquesIdentifiesIds() != null && !request.getRisquesIdentifiesIds().isEmpty()) {
            Set<Risque> risques = new HashSet<>(risqueRepository.findAllById(request.getRisquesIdentifiesIds()));
            visite.setRisquesIdentifies(risques);
        }

        visite = visiteRepository.save(visite);

        // Liaison au document source (si spécifié)
        if (type != null && request.getDocumentSourceId() != null) {
            linkDocumentToVisite(type, request.getDocumentSourceId(), visite);
        }

        return buildResponse(visite, type, request.getDocumentSourceId());
    }

    @Override
    @Transactional
    public VisitePrealableResponse update(String id, VisitePrealableRequest request) {
        VisitePrealable visite = findEntity(id);

        if (visite.isEffectuee()) {
            throw new BusinessException("Impossible de modifier une visite préalable finalisée");
        }

        visite.setDateHeureFin(request.getDateHeureFin());
        visite.setLatitude(request.getLatitude());
        visite.setLongitude(request.getLongitude());
        visite.setCommentaire(request.getCommentaire());

        if (request.getVisiteurId() != null) {
            Utilisateur visiteur = utilisateurRepository.findById(request.getVisiteurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Visiteur non trouvé : " + request.getVisiteurId()));
            visite.setVisiteur(visiteur);
        }

        if (request.getRisquesIdentifiesIds() != null) {
            Set<Risque> risques = new HashSet<>(risqueRepository.findAllById(request.getRisquesIdentifiesIds()));
            visite.setRisquesIdentifies(risques);
        }

        visite = visiteRepository.save(visite);

        // Retrouver le type et l'ID du document source depuis la DB
        String[] docInfo = findDocumentSource(id);
        return buildResponse(visite, docInfo[0], docInfo[1]);
    }

    @Override
    @Transactional
    public VisitePrealableResponse finaliser(String id) {
        VisitePrealable visite = findEntity(id);

        if (visite.isEffectuee()) {
            throw new BusinessException("La visite est déjà finalisée");
        }

        // Validation des conditions de finalisation
        List<String> erreurs = new java.util.ArrayList<>();
        if (visite.getLatitude() == null || visite.getLongitude() == null) {
            erreurs.add("Les coordonnées GPS (latitude/longitude) sont obligatoires");
        }
        if (visite.getCommentaire() == null || visite.getCommentaire().isBlank()) {
            erreurs.add("Le commentaire de visite est obligatoire");
        }
        if (visite.getPhotos() == null || visite.getPhotos().isEmpty()) {
            erreurs.add("Au moins une photo est obligatoire pour finaliser la visite");
        }

        if (!erreurs.isEmpty()) {
            throw new BusinessException("Impossible de finaliser la visite : " + String.join("; ", erreurs));
        }

        visite.setEffectuee(true);
        visite.setDateHeureFin(LocalDateTime.now());
        visite = visiteRepository.save(visite);
        log.info("Visite préalable {} finalisée avec succès", id);

        String[] docInfo = findDocumentSource(id);
        return buildResponse(visite, docInfo[0], docInfo[1]);
    }

    @Override
    @Transactional
    public void delete(String id) {
        VisitePrealable visite = findEntity(id);

        if (visite.getAnalyseRisque() != null) {
            throw new BusinessException("Impossible de supprimer une visite préalable liée à une analyse des risques");
        }

        // Supprimer les fichiers photos du disque
        if (visite.getPhotos() != null) {
            visite.getPhotos().forEach(photo -> {
                try {
                    storageService.delete(photo.getNom());
                } catch (Exception e) {
                    log.warn("Impossible de supprimer le fichier : {}", photo.getNom());
                }
            });
        }

        visiteRepository.deleteById(id);
        log.info("Visite préalable {} supprimée", id);
    }

    @Override
    public VisitePrealableResponse findById(String id) {
        VisitePrealable visite = findEntity(id);
        String[] docInfo = findDocumentSource(id);
        return buildResponse(visite, docInfo[0], docInfo[1]);
    }

    @Override
    public Page<VisitePrealableResponse> findAll(Pageable pageable) {
        return visiteRepository.findAll(pageable).map(v -> {
            String[] docInfo = findDocumentSource(v.getId());
            return buildResponse(v, docInfo[0], docInfo[1]);
        });
    }

    @Override
    @Transactional
    public PhotoResponse addPhoto(String visiteId, MultipartFile file, String legende) {
        VisitePrealable visite = findEntity(visiteId);

        if (visite.isEffectuee()) {
            throw new BusinessException("Impossible d'ajouter une photo à une visite finalisée");
        }

        // Stocker le fichier sur disque
        String storedFilename = storageService.store(file);

        int ordre = photoRepository.countByVisitePrealableId(visiteId) + 1;

        Photo photo = Photo.builder()
                .nom(storedFilename)
                .path(storedFilename)
                .taille(file.getSize())
                .typeMime(file.getContentType())
                .ordre(ordre)
                .legende(legende)
                .dateCreation(LocalDateTime.now())
                .visitePrealable(visite)
                .build();

        photo = photoRepository.save(photo);
        log.info("Photo ajoutée à la visite {} : {}", visiteId, storedFilename);
        return photoMapper.toResponse(photo);
    }

    @Override
    @Transactional
    public void deletePhoto(String visiteId, String photoId) {
        VisitePrealable visite = findEntity(visiteId);

        if (visite.isEffectuee()) {
            throw new BusinessException("Impossible de supprimer une photo d'une visite finalisée");
        }

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo non trouvée : " + photoId));

        if (!photo.getVisitePrealable().getId().equals(visiteId)) {
            throw new BusinessException("Cette photo n'appartient pas à la visite spécifiée");
        }

        try {
            storageService.delete(photo.getNom());
        } catch (Exception e) {
            log.warn("Impossible de supprimer le fichier disque : {}", photo.getNom());
        }
        photoRepository.deleteById(photoId);
        log.info("Photo {} supprimée de la visite {}", photoId, visiteId);
    }

    // ─────────────────── Méthodes privées ───────────────────

    private VisitePrealable findEntity(String id) {
        return visiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visite préalable non trouvée : " + id));
    }

    /**
     * Lie la visite au document source (DI, OT ou BT) en mettant à jour la FK.
     */
    private void linkDocumentToVisite(String type, String documentId, VisitePrealable visite) {
        switch (type) {
            case "DI" -> {
                DemandeIntervention di = diRepository.findById(documentId)
                        .orElseThrow(() -> new ResourceNotFoundException("DI non trouvée : " + documentId));
                di.setVisitePrealable(visite);
                diRepository.save(di);
            }
            case "OT" -> {
                OrdreTravail ot = otRepository.findById(documentId)
                        .orElseThrow(() -> new ResourceNotFoundException("OT non trouvé : " + documentId));
                ot.setVisitePrealable(visite);
                otRepository.save(ot);
            }
            case "BT" -> {
                BonTravail bt = btRepository.findById(documentId)
                        .orElseThrow(() -> new ResourceNotFoundException("BT non trouvé : " + documentId));
                bt.setVisitePrealable(visite);
                btRepository.save(bt);
            }
        }
    }

    /**
     * Retrouve le type et l'ID du document source lié à une visite.
     * Retourne ["INCONNU", ""] si aucun document trouvé.
     */
    private String[] findDocumentSource(String visiteId) {
        // Cherche dans DI
        var diOpt = diRepository.findAll().stream()
                .filter(di -> di.getVisitePrealable() != null && di.getVisitePrealable().getId().equals(visiteId))
                .findFirst();
        if (diOpt.isPresent()) return new String[]{"DI", diOpt.get().getId()};

        // Cherche dans OT
        var otOpt = otRepository.findAll().stream()
                .filter(ot -> ot.getVisitePrealable() != null && ot.getVisitePrealable().getId().equals(visiteId))
                .findFirst();
        if (otOpt.isPresent()) return new String[]{"OT", otOpt.get().getId()};

        // Cherche dans BT
        var btOpt = btRepository.findAll().stream()
                .filter(bt -> bt.getVisitePrealable() != null && bt.getVisitePrealable().getId().equals(visiteId))
                .findFirst();
        if (btOpt.isPresent()) return new String[]{"BT", btOpt.get().getId()};

        return new String[]{"INCONNU", ""};
    }

    /**
     * Construit le DTO de réponse complet avec les champs de document source.
     */
    private VisitePrealableResponse buildResponse(VisitePrealable visite, String type, String documentId) {
        VisitePrealableResponse response = visiteMapper.toResponse(visite);
        response.setTypeDocumentSource(type);
        response.setDocumentSourceId(documentId);

        // Récupérer le numéro du document source pour le DTO
        if (type != null && documentId != null) {
            switch (type) {
                case "DI" -> diRepository.findById(documentId).ifPresent(di -> response.setDocumentSourceNumero(di.getNumero()));
                case "OT" -> otRepository.findById(documentId).ifPresent(ot -> response.setDocumentSourceNumero(ot.getNumero()));
                case "BT" -> btRepository.findById(documentId).ifPresent(bt -> response.setDocumentSourceNumero(bt.getNumero()));
            }
        }
        return response;
    }
}
