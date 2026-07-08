package com.ocp.at.service.impl;

import com.ocp.at.dto.request.EquipementRequest;
import com.ocp.at.dto.response.EquipementResponse;
import com.ocp.at.entity.Equipement;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.EquipementMapper;
import com.ocp.at.repository.EquipementRepository;
import com.ocp.at.service.EquipementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class EquipementServiceImpl implements EquipementService {

    private final EquipementRepository repository;
    private final EquipementMapper mapper;

    @Override
    @Transactional
    public EquipementResponse create(EquipementRequest request) {
        log.info("Création d'un(e) Equipement");
        Equipement entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public EquipementResponse update(String id, EquipementRequest request) {
        log.info("Modification d'un(e) Equipement avec ID: {}", id);
        Equipement entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement non trouvé(e)"));
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public EquipementResponse getById(String id) {
        log.info("Consultation d'un(e) Equipement avec ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement non trouvé(e)"));
    }

    @Override
    public List<EquipementResponse> getAll() {
        log.info("Consultation de tous/toutes les Equipement");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EquipementResponse> search(String query, Pageable pageable) {
        log.info("Recherche Equipement avec query: {}", query);
        Specification<Equipement> spec = Specification.where(null);
        // Implement search logic if needed
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Suppression d'un(e) Equipement avec ID: {}", id);
        Equipement entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement non trouvé(e)"));

        // Check if used in AT before deleting (A implémenter)
        repository.delete(entity);
    }
}

