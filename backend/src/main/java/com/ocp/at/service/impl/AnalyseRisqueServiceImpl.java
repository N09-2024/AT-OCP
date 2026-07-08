package com.ocp.at.service.impl;

import com.ocp.at.dto.request.AnalyseRisqueRequest;
import com.ocp.at.dto.response.AnalyseRisqueResponse;
import com.ocp.at.entity.*;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.AnalyseRisqueMapper;
import com.ocp.at.repository.*;
import com.ocp.at.service.AnalyseRisqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyseRisqueServiceImpl implements AnalyseRisqueService {

    private final AnalyseRisqueRepository analyseRepository;
    private final VisitePrealableRepository visiteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RisqueRepository risqueRepository;
    private final MesurePreparationRepository mesureRepository;
    private final EPIRepository epiRepository;
    private final MoyenAccesRepository moyenAccesRepository;
    private final AnalyseRisqueMapper analyseMapper;

    @Override
    @Transactional
    public AnalyseRisqueResponse create(AnalyseRisqueRequest request) {
        log.info("Création d'une analyse des risques pour la visite {}", request.getVisitePrealableId());

        VisitePrealable visite = visiteRepository.findById(request.getVisitePrealableId())
                .orElseThrow(() -> new ResourceNotFoundException("Visite préalable non trouvée : " + request.getVisitePrealableId()));

        // Règle métier : la visite doit être finalisée
        if (!visite.isEffectuee()) {
            throw new BusinessException("Impossible de créer une analyse des risques : la visite préalable n'est pas encore finalisée");
        }

        // Règle métier : une seule analyse par visite
        if (analyseRepository.existsByVisitePrealableId(request.getVisitePrealableId())) {
            throw new BusinessException("Une analyse des risques existe déjà pour cette visite préalable");
        }

        // Chargement des référentiels
        Set<Risque> risques = loadRisques(request);
        Set<MesurePreparation> mesures = loadMesures(request);
        Set<EPI> epis = loadEpis(request);
        Set<MoyenAcces> moyens = loadMoyens(request);

        AnalyseRisque analyse = AnalyseRisque.builder()
                .visitePrealable(visite)
                .commentaire(request.getCommentaire())
                .risques(risques)
                .mesures(mesures)
                .epis(epis)
                .moyensAcces(moyens)
                .build();

        // Association de l'analyseur
        if (request.getAnalyseurId() != null) {
            Utilisateur analyseur = utilisateurRepository.findById(request.getAnalyseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Analyseur non trouvé : " + request.getAnalyseurId()));
            analyse.setAnalyseur(analyseur);
        }

        analyse = analyseRepository.save(analyse);
        log.info("Analyse des risques créée : {}", analyse.getId());
        return analyseMapper.toResponse(analyse);
    }

    @Override
    @Transactional
    public AnalyseRisqueResponse update(String id, AnalyseRisqueRequest request) {
        AnalyseRisque analyse = findEntity(id);

        analyse.setCommentaire(request.getCommentaire());
        analyse.setRisques(loadRisques(request));
        analyse.setMesures(loadMesures(request));
        analyse.setEpis(loadEpis(request));
        analyse.setMoyensAcces(loadMoyens(request));

        if (request.getAnalyseurId() != null) {
            Utilisateur analyseur = utilisateurRepository.findById(request.getAnalyseurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Analyseur non trouvé : " + request.getAnalyseurId()));
            analyse.setAnalyseur(analyseur);
        }

        analyse = analyseRepository.save(analyse);
        return analyseMapper.toResponse(analyse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        findEntity(id); // vérifie l'existence
        analyseRepository.deleteById(id);
        log.info("Analyse des risques {} supprimée", id);
    }

    @Override
    public AnalyseRisqueResponse findById(String id) {
        return analyseMapper.toResponse(findEntity(id));
    }

    @Override
    public AnalyseRisqueResponse findByVisitePrealableId(String visiteId) {
        return analyseRepository.findByVisitePrealableId(visiteId)
                .map(analyseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune analyse trouvée pour la visite : " + visiteId));
    }

    @Override
    public Page<AnalyseRisqueResponse> findAll(Pageable pageable) {
        return analyseRepository.findAll(pageable).map(analyseMapper::toResponse);
    }

    // ─────────────────── Méthodes privées ───────────────────

    private AnalyseRisque findEntity(String id) {
        return analyseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analyse des risques non trouvée : " + id));
    }

    private Set<Risque> loadRisques(AnalyseRisqueRequest request) {
        if (request.getRisquesIds() == null || request.getRisquesIds().isEmpty())
            throw new BusinessException("Au moins un risque est obligatoire");
        return new HashSet<>(risqueRepository.findAllById(request.getRisquesIds()));
    }

    private Set<MesurePreparation> loadMesures(AnalyseRisqueRequest request) {
        if (request.getMesuresIds() == null || request.getMesuresIds().isEmpty())
            throw new BusinessException("Au moins une mesure de prévention est obligatoire");
        return new HashSet<>(mesureRepository.findAllById(request.getMesuresIds()));
    }

    private Set<EPI> loadEpis(AnalyseRisqueRequest request) {
        if (request.getEpisIds() == null || request.getEpisIds().isEmpty())
            throw new BusinessException("Au moins un EPI est obligatoire");
        return new HashSet<>(epiRepository.findAllById(request.getEpisIds()));
    }

    private Set<MoyenAcces> loadMoyens(AnalyseRisqueRequest request) {
        if (request.getMoyensAccesIds() == null || request.getMoyensAccesIds().isEmpty())
            throw new BusinessException("Au moins un moyen d'accès est obligatoire");
        return new HashSet<>(moyenAccesRepository.findAllById(request.getMoyensAccesIds()));
    }
}
