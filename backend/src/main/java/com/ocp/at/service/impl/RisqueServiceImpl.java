package com.ocp.at.service.impl;

import com.ocp.at.dto.request.RisqueRequest;
import com.ocp.at.dto.response.RisqueResponse;
import com.ocp.at.entity.Risque;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.RisqueMapper;
import com.ocp.at.repository.RisqueRepository;
import com.ocp.at.service.RisqueService;
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
public class RisqueServiceImpl implements RisqueService {

    private final RisqueRepository repository;
    private final RisqueMapper mapper;

    @Override
    @Transactional
    public RisqueResponse create(RisqueRequest request) {
        log.info("Création d'un(e) Risque");
        Risque entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public RisqueResponse update(String id, RisqueRequest request) {
        log.info("Modification d'un(e) Risque avec ID: {}", id);
        Risque entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Risque non trouvé(e)"));
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public RisqueResponse getById(String id) {
        log.info("Consultation d'un(e) Risque avec ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Risque non trouvé(e)"));
    }

    @Override
    public List<RisqueResponse> getAll() {
        log.info("Consultation de tous/toutes les Risque");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RisqueResponse> search(String query, Pageable pageable) {
        log.info("Recherche Risque avec query: {}", query);
        Specification<Risque> spec = Specification.where(null);
        // Implement search logic if needed
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Suppression d'un(e) Risque avec ID: {}", id);
        Risque entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Risque non trouvé(e)"));

        // Check if used in AT before deleting (A implémenter)
        repository.delete(entity);
    }
}

