package com.ocp.at.service.impl;

import com.ocp.at.dto.request.BonTravailRequest;
import com.ocp.at.dto.response.BonTravailResponse;
import com.ocp.at.entity.BonTravail;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.StatutDocument;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.BonTravailMapper;
import com.ocp.at.repository.BonTravailRepository;
import com.ocp.at.repository.EntrepriseExterneRepository;
import com.ocp.at.repository.InstallationRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.service.BonTravailService;
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
public class BonTravailServiceImpl implements BonTravailService {

    private final BonTravailRepository repository;
    private final BonTravailMapper mapper;
    private final UtilisateurRepository utilisateurRepository;
    private final InstallationRepository installationRepository;
    private final EntrepriseExterneRepository entrepriseExterneRepository;

    @Override
    @Transactional
    public BonTravailResponse create(BonTravailRequest request, String demandeurId) {
        log.info("Création d'un nouveau Bon de Travail (BT)");

        BonTravail bt = mapper.toEntity(request);
        
        String year = String.valueOf(Year.now().getValue());
        Long seq = repository.getNextSequence();
        bt.setNumero(String.format("BT-%s-%06d", year, seq));

        bt.setStatut(StatutDocument.BROUILLON);

        if (demandeurId != null) {
            Utilisateur demandeur = utilisateurRepository.findById(demandeurId)
                    .orElseThrow(() -> new ResourceNotFoundException("Demandeur non trouvé"));
            bt.setDemandeur(demandeur);
        }

        bt.setEntrepriseExterne(entrepriseExterneRepository.findById(request.getEntrepriseExterneId())
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise externe non trouvée")));

        if (request.getInstallationId() != null) {
            bt.setInstallation(installationRepository.findById(request.getInstallationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation non trouvée")));
        }

        bt = repository.save(bt);
        return calculateAtCreable(mapper.toResponse(bt), bt);
    }

    @Override
    @Transactional
    public BonTravailResponse update(String id, BonTravailRequest request) {
        BonTravail bt = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BT non trouvé avec l'id : " + id));

        if (bt.getStatut() == StatutDocument.CLOS || bt.getStatut() == StatutDocument.ANNULE) {
            throw new IllegalStateException("Impossible de modifier un BT clôturé ou annulé");
        }

        bt.setObjet(request.getObjet());
        bt.setDescription(request.getDescription());
        bt.setTypeIntervention(request.getTypeIntervention());
        bt.setNiveauIntervention(request.getNiveauIntervention());

        bt.setEntrepriseExterne(entrepriseExterneRepository.findById(request.getEntrepriseExterneId())
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise externe non trouvée")));

        if (request.getInstallationId() != null) {
            bt.setInstallation(installationRepository.findById(request.getInstallationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation non trouvée")));
        } else {
            bt.setInstallation(null);
        }

        bt = repository.save(bt);
        return calculateAtCreable(mapper.toResponse(bt), bt);
    }

    @Override
    @Transactional
    public void delete(String id) {
        BonTravail bt = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BT non trouvé avec l'id : " + id));
        
        bt.setStatut(StatutDocument.ANNULE);
        repository.save(bt);
        log.info("BT annulé : {}", id);
    }

    @Override
    public BonTravailResponse findById(String id) {
        BonTravail bt = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BT non trouvé avec l'id : " + id));
        return calculateAtCreable(mapper.toResponse(bt), bt);
    }

    @Override
    public Page<BonTravailResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(bt -> calculateAtCreable(mapper.toResponse(bt), bt));
    }

    @Override
    public Page<BonTravailResponse> search(String query, Pageable pageable) {
        return findAll(pageable);
    }

    private BonTravailResponse calculateAtCreable(BonTravailResponse response, BonTravail entity) {
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
