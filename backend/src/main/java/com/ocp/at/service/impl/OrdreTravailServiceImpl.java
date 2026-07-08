package com.ocp.at.service.impl;

import com.ocp.at.dto.request.OrdreTravailRequest;
import com.ocp.at.dto.response.OrdreTravailResponse;
import com.ocp.at.entity.OrdreTravail;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.StatutDocument;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.OrdreTravailMapper;
import com.ocp.at.repository.InstallationRepository;
import com.ocp.at.repository.OrdreTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.service.OrdreTravailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdreTravailServiceImpl implements OrdreTravailService {

    private final OrdreTravailRepository repository;
    private final OrdreTravailMapper mapper;
    private final UtilisateurRepository utilisateurRepository;
    private final InstallationRepository installationRepository;

    @Override
    @Transactional
    public OrdreTravailResponse create(OrdreTravailRequest request, String demandeurId) {
        log.info("Création d'un nouvel Ordre de Travail (OT)");

        OrdreTravail ot = mapper.toEntity(request);
        
        String year = String.valueOf(Year.now().getValue());
        Long seq = repository.getNextSequence();
        ot.setNumero(String.format("OT-%s-%06d", year, seq));

        ot.setStatut(StatutDocument.BROUILLON);

        if (demandeurId != null) {
            Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                    .orElseThrow(() -> new ResourceNotFoundException("Demandeur non trouvé"));
            ot.setDemandeur(demandeur);
        }

        if (request.getInstallationId() != null) {
            ot.setInstallation(installationRepository.findById(request.getInstallationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation non trouvée")));
        }

        ot = repository.save(ot);
        return calculateAtCreable(mapper.toResponse(ot), ot);
    }

    @Override
    @Transactional
    public OrdreTravailResponse update(String id, OrdreTravailRequest request) {
        OrdreTravail ot = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT non trouvé avec l'id : " + id));

        if (ot.getStatut() == StatutDocument.CLOS || ot.getStatut() == StatutDocument.ANNULE) {
            throw new IllegalStateException("Impossible de modifier un OT clôturé ou annulé");
        }

        ot.setObjet(request.getObjet());
        ot.setDescription(request.getDescription());
        ot.setTypeIntervention(request.getTypeIntervention());
        ot.setNiveauIntervention(request.getNiveauIntervention());
        ot.setDateExecution(request.getDateExecution());

        if (request.getInstallationId() != null) {
            ot.setInstallation(installationRepository.findById(request.getInstallationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation non trouvée")));
        } else {
            ot.setInstallation(null);
        }

        ot = repository.save(ot);
        return calculateAtCreable(mapper.toResponse(ot), ot);
    }

    @Override
    @Transactional
    public void delete(String id) {
        OrdreTravail ot = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT non trouvé avec l'id : " + id));
        
        ot.setStatut(StatutDocument.ANNULE);
        repository.save(ot);
        log.info("OT annulé : {}", id);
    }

    @Override
    public OrdreTravailResponse findById(String id) {
        OrdreTravail ot = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT non trouvé avec l'id : " + id));
        return calculateAtCreable(mapper.toResponse(ot), ot);
    }

    @Override
    public Page<OrdreTravailResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(ot -> calculateAtCreable(mapper.toResponse(ot), ot));
    }

    @Override
    public Page<OrdreTravailResponse> search(String query, Pageable pageable) {
        return findAll(pageable);
    }

    private OrdreTravailResponse calculateAtCreable(OrdreTravailResponse response, OrdreTravail entity) {
        boolean creable = false;
        if (entity.getNiveauIntervention() == NiveauIntervention.NIVEAU_2) {
            if (entity.getVisitePrealable() != null && entity.getVisitePrealable().isEffectuee()) {
                if (entity.getVisitePrealable().getAnalyseRisque() != null) {
                    creable = true;
                }
            }
        }
        response.setAtCreable(creable);
        return response;
    }
}
