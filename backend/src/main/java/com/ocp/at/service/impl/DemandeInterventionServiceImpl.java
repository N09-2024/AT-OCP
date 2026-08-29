package com.ocp.at.service.impl;

import com.ocp.at.dto.request.DemandeInterventionRequest;
import com.ocp.at.dto.response.DemandeInterventionResponse;
import com.ocp.at.entity.DemandeIntervention;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.StatutDocument;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.DemandeInterventionMapper;
import com.ocp.at.repository.DemandeInterventionRepository;
import com.ocp.at.repository.EquipementRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.service.DemandeInterventionService;
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
public class DemandeInterventionServiceImpl implements DemandeInterventionService {

    private final DemandeInterventionRepository repository;
    private final DemandeInterventionMapper mapper;
    private final UtilisateurRepository utilisateurRepository;
    private final EquipementRepository equipementRepository;

    @Override
    @Transactional
    public DemandeInterventionResponse create(DemandeInterventionRequest request, String demandeurId) {
        log.info("Création d'une nouvelle Demande d'Intervention (DI)");

        DemandeIntervention di = mapper.toEntity(request);
        
        // Numérotation auto : DI-2026-000001
        String year = String.valueOf(Year.now().getValue());
        Long seq = repository.getNextSequence();
        di.setNumero(String.format("DI-%s-%06d", year, seq));

        di.setStatut(StatutDocument.BROUILLON);

        // Récupération des entités liées
        if (demandeurId != null) {
            Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                    .orElseThrow(() -> new ResourceNotFoundException("Demandeur non trouvé"));
            di.setDemandeur(demandeur);
        }

        if (request.getEquipementId() != null) {
            di.setEquipement(equipementRepository.findById(request.getEquipementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipement non trouvé")));
        }

        di = repository.save(di);
        return calculateAtCreable(mapper.toResponse(di), di);
    }

    @Override
    @Transactional
    public DemandeInterventionResponse update(String id, DemandeInterventionRequest request) {
        DemandeIntervention di = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DI non trouvée avec l'id : " + id));

        if (di.getStatut() == StatutDocument.CLOS || di.getStatut() == StatutDocument.ANNULE) {
            throw new IllegalStateException("Impossible de modifier une DI clôturée ou annulée");
        }

        di.setObjet(request.getObjet());
        di.setDescription(request.getDescription());
        di.setPriorite(request.getPriorite());
        di.setTypeIntervention(request.getTypeIntervention());
        di.setNiveauIntervention(request.getNiveauIntervention());

        if (request.getEquipementId() != null) {
            di.setEquipement(equipementRepository.findById(request.getEquipementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Equipement non trouvé")));
        } else {
            di.setEquipement(null);
        }

        di = repository.save(di);
        return calculateAtCreable(mapper.toResponse(di), di);
    }

    @Override
    @Transactional
    public void delete(String id) {
        DemandeIntervention di = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DI non trouvée avec l'id : " + id));
        
        // Annulation logique
        di.setStatut(StatutDocument.ANNULE);
        repository.save(di);
        log.info("DI annulée : {}", id);
    }

    @Override
    public DemandeInterventionResponse findById(String id) {
        DemandeIntervention di = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DI non trouvée avec l'id : " + id));
        return calculateAtCreable(mapper.toResponse(di), di);
    }

    @Override
    public Page<DemandeInterventionResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(di -> calculateAtCreable(mapper.toResponse(di), di));
    }

    @Override
    public Page<DemandeInterventionResponse> search(String query, Pageable pageable) {
        // Pourrait être implémenté avec JpaSpecificationExecutor
        return findAll(pageable);
    }

    private DemandeInterventionResponse calculateAtCreable(DemandeInterventionResponse response, DemandeIntervention entity) {
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
