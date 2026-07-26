package com.ocp.at.service.impl;

import com.ocp.at.dto.request.InstallationRequest;
import com.ocp.at.dto.response.InstallationResponse;
import com.ocp.at.entity.Installation;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.InstallationMapper;
import com.ocp.at.repository.InstallationRepository;
import com.ocp.at.service.InstallationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.ocp.at.repository.EquipementRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InstallationServiceImpl implements InstallationService {

    private final InstallationRepository repository;
    private final InstallationMapper mapper;
    private final EquipementRepository equipementRepository;

    @Override
    @Transactional
    public InstallationResponse create(InstallationRequest request) {
        log.info("Création d'un(e) Installation");
        Installation entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public InstallationResponse update(String id, InstallationRequest request) {
        log.info("Modification d'un(e) Installation avec ID: {}", id);
        Installation entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installation non trouvé(e)"));
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public InstallationResponse getById(String id) {
        log.info("Consultation d'un(e) Installation avec ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Installation non trouvé(e)"));
    }

    @Override
    public List<InstallationResponse> getAll() {
        log.info("Consultation de tous/toutes les Installation");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<InstallationResponse> search(String query, Pageable pageable) {
        log.info("Recherche Installation avec query: {}", query);
        Specification<Installation> spec = Specification.where(null);
        // Implement search logic if needed
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Suppression d'un(e) Installation avec ID: {}", id);
        Installation entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installation non trouvé(e)"));

        if (equipementRepository.existsByInstallationId(id)) {
            throw new BusinessException("Impossible de supprimer une Installation qui contient des Equipements.");
        }
        repository.delete(entity);
    }
}

