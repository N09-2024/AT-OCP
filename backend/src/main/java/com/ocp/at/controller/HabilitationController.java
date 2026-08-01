package com.ocp.at.controller;

import com.ocp.at.dto.request.HabilitationRequest;
import com.ocp.at.dto.response.HabilitationResponse;
import com.ocp.at.entity.Habilitation;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.repository.HabilitationRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API de gestion des Habilitations AT — Formulaire F-HSE-SEC-31-02
 * Standard OCP S-HSE-SEC-31 §9
 */
@RestController
@RequestMapping("/api/habilitations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Habilitations", description = "Gestion des agents habilités à délivrer des AT (F-HSE-SEC-31-02)")
public class HabilitationController {

    private final HabilitationRepository habilitationRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping
    @Operation(summary = "Lister toutes les habilitations actives")
    public List<HabilitationResponse> getAll() {
        return habilitationRepository.findByActifTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les habilitations (actives et inactives)")
    public List<HabilitationResponse> getAllIncludingInactive() {
        return habilitationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une habilitation par ID")
    public HabilitationResponse getById(@PathVariable String id) {
        Habilitation h = habilitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habilitation introuvable : " + id));
        return toResponse(h);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Habiliter un agent à délivrer des AT (F-HSE-SEC-31-02)")
    public HabilitationResponse habiliter(@RequestBody HabilitationRequest request) {
        if (request.getUtilisateurId() == null || request.getUtilisateurId().isBlank()) {
            throw new BusinessException("L'identifiant de l'utilisateur à habiliter est obligatoire.");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + request.getUtilisateurId()));

        // Désigner par l'utilisateur connecté (HCEP)
        Utilisateur designePar = null;
        try {
            String currentUserId = SecurityUtils.getCurrentUtilisateurId().orElse(null);
            if (currentUserId != null) {
                designePar = utilisateurRepository.findById(currentUserId).orElse(null);
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer l'utilisateur connecté pour la désignation.");
        }

        // Désactiver une éventuelle habilitation existante
        habilitationRepository.findByUtilisateurId(request.getUtilisateurId())
                .ifPresent(existing -> {
                    existing.setActif(false);
                    habilitationRepository.save(existing);
                    log.info("Ancienne habilitation désactivée pour l'utilisateur {}", request.getUtilisateurId());
                });

        Habilitation habilitation = Habilitation.builder()
                .utilisateur(utilisateur)
                .designePar(designePar)
                .dateHabilitation(LocalDate.now())
                .valideJusquAu(LocalDate.now().plusYears(1)) // Revue annuelle obligatoire §9
                .actif(true)
                .observations(request.getObservations())
                .build();

        habilitation = habilitationRepository.save(habilitation);
        log.info("Agent habilité à délivrer des AT (F-HSE-SEC-31-02): {}", utilisateur.getEmail());
        return toResponse(habilitation);
    }

    @PutMapping("/{id}/desactiver")
    @Operation(summary = "Désactiver une habilitation")
    public HabilitationResponse desactiver(@PathVariable String id) {
        Habilitation h = habilitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habilitation introuvable : " + id));
        h.setActif(false);
        h = habilitationRepository.save(h);
        log.info("Habilitation désactivée : {}", id);
        return toResponse(h);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Supprimer une habilitation")
    public void delete(@PathVariable String id) {
        Habilitation h = habilitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habilitation introuvable : " + id));
        habilitationRepository.delete(h);
        log.info("Habilitation supprimée : {}", id);
    }

    private HabilitationResponse toResponse(Habilitation h) {
        String serviceNom = null;
        String serviceCode = null;
        if (h.getUtilisateur().getService() != null) {
            serviceNom = h.getUtilisateur().getService().getNomService();
            serviceCode = h.getUtilisateur().getService().getCodeService();
        }

        String designeParNomComplet = null;
        String designeParId = null;
        if (h.getDesignePar() != null) {
            designeParId = h.getDesignePar().getId();
            designeParNomComplet = h.getDesignePar().getPrenom() + " " + h.getDesignePar().getNom();
        }

        return HabilitationResponse.builder()
                .id(h.getId())
                .utilisateurId(h.getUtilisateur().getId())
                .utilisateurNom(h.getUtilisateur().getNom())
                .utilisateurPrenom(h.getUtilisateur().getPrenom())
                .utilisateurEmail(h.getUtilisateur().getEmail())
                .utilisateurMatricule(h.getUtilisateur().getMatricule())
                .serviceNom(serviceNom)
                .serviceCode(serviceCode)
                .designeParId(designeParId)
                .designeParNomComplet(designeParNomComplet)
                .dateHabilitation(h.getDateHabilitation())
                .valideJusquAu(h.getValideJusquAu())
                .actif(h.getActif())
                .observations(h.getObservations())
                .dateCreation(h.getDateCreation())
                .build();
    }
}
