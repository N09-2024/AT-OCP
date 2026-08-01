package com.ocp.at.controller;

import com.ocp.at.dto.request.ClassificationInterventionRequest;
import com.ocp.at.dto.response.ClassificationInterventionResponse;
import com.ocp.at.entity.ClassificationIntervention;
import com.ocp.at.entity.Service;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.Zone;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.repository.ClassificationInterventionRepository;
import com.ocp.at.repository.ServiceRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.repository.ZoneRepository;
import com.ocp.at.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * API de gestion des Classifications d'interventions (§6 Standard OCP S-HSE-SEC-31).
 * Décision HCEP : Niveau 1 (pas d'AT requise) ou Niveau 2 (AT obligatoire).
 */
@RestController
@RequestMapping("/api/classifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Classifications", description = "Classification des interventions par HCEP (§6 Standard OCP S-HSE-SEC-31)")
public class ClassificationController {

    private final ClassificationInterventionRepository classificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ZoneRepository zoneRepository;
    private final ServiceRepository serviceRepository;

    @GetMapping
    @Operation(summary = "Lister toutes les classifications")
    public List<ClassificationInterventionResponse> getAll() {
        return classificationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/niveau/{niveau}")
    @Operation(summary = "Lister les classifications par niveau (NIVEAU_1 ou NIVEAU_2)")
    public List<ClassificationInterventionResponse> getByNiveau(@PathVariable String niveau) {
        if (!"NIVEAU_1".equals(niveau) && !"NIVEAU_2".equals(niveau)) {
            throw new BusinessException("Niveau invalide. Valeurs acceptées : NIVEAU_1, NIVEAU_2");
        }
        return classificationRepository.findByNiveauOrderByDateClassificationDesc(niveau)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une classification par ID")
    public ClassificationInterventionResponse getById(@PathVariable String id) {
        ClassificationIntervention c = classificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classification introuvable : " + id));
        return toResponse(c);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistrer la classification d'une intervention (HCEP §6)")
    public ClassificationInterventionResponse classifier(@RequestBody ClassificationInterventionRequest request) {
        if (request.getNiveau() == null || request.getNiveau().isBlank()) {
            throw new BusinessException("Le niveau de classification (NIVEAU_1 ou NIVEAU_2) est obligatoire.");
        }
        if (!"NIVEAU_1".equals(request.getNiveau()) && !"NIVEAU_2".equals(request.getNiveau())) {
            throw new BusinessException("Niveau invalide. Valeurs acceptées : NIVEAU_1, NIVEAU_2");
        }

        // Récupérer le HCEP connecté
        Utilisateur classifiePar = null;
        try {
            String currentUserId = SecurityUtils.getCurrentUtilisateurId().orElse(null);
            if (currentUserId != null) {
                classifiePar = utilisateurRepository.findById(currentUserId).orElse(null);
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer l'utilisateur connecté pour la classification.");
        }

        Zone zone = null;
        if (request.getZoneId() != null && !request.getZoneId().isBlank()) {
            zone = zoneRepository.findById(request.getZoneId()).orElse(null);
        }

        Service service = null;
        if (request.getServiceId() != null && !request.getServiceId().isBlank()) {
            service = serviceRepository.findById(request.getServiceId()).orElse(null);
        }

        // Générer une référence unique : CLF-YYYYMMDD-XXXXX
        String reference = "CLF-" +
                java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%05d", ThreadLocalRandom.current().nextInt(1, 99999));

        // Si tiers → forcément Niveau 2 (§8.1 NB)
        boolean estTiers = Boolean.TRUE.equals(request.getEstTiers());
        String niveau = estTiers ? "NIVEAU_2" : request.getNiveau();

        ClassificationIntervention classification = ClassificationIntervention.builder()
                .reference(reference)
                .niveau(niveau)
                .estTiers(estTiers)
                .natureIntervention(request.getNatureIntervention())
                .zone(zone)
                .service(service)
                .classifiePar(classifiePar)
                .observations(request.getObservations())
                .statut("EFFECTUEE")
                .build();

        classification = classificationRepository.save(classification);
        log.info("Classification enregistrée : {} — Niveau : {}", reference, niveau);
        return toResponse(classification);
    }

    private ClassificationInterventionResponse toResponse(ClassificationIntervention c) {
        return ClassificationInterventionResponse.builder()
                .id(c.getId())
                .reference(c.getReference())
                .niveau(c.getNiveau())
                .estTiers(c.getEstTiers())
                .natureIntervention(c.getNatureIntervention())
                .zoneId(c.getZone() != null ? c.getZone().getId() : null)
                .zoneNom(c.getZone() != null ? c.getZone().getNomZone() : null)
                .serviceId(c.getService() != null ? c.getService().getId() : null)
                .serviceNom(c.getService() != null ? c.getService().getNomService() : null)
                .classifieParId(c.getClassifiePar() != null ? c.getClassifiePar().getId() : null)
                .classifieParNomComplet(c.getClassifiePar() != null
                        ? c.getClassifiePar().getPrenom() + " " + c.getClassifiePar().getNom()
                        : null)
                .dateClassification(c.getDateClassification())
                .observations(c.getObservations())
                .statut(c.getStatut())
                .autorisationTravailId(c.getAutorisationTravail() != null ? c.getAutorisationTravail().getId() : null)
                .autorisationTravailNumero(c.getAutorisationTravail() != null ? c.getAutorisationTravail().getNumero() : null)
                .build();
    }
}
