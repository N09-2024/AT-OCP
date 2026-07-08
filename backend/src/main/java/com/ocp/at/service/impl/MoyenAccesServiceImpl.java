package com.ocp.at.service.impl;

import com.ocp.at.dto.request.MoyenAccesRequest;
import com.ocp.at.dto.response.MoyenAccesResponse;
import com.ocp.at.entity.MoyenAcces;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.MoyenAccesMapper;
import com.ocp.at.repository.MoyenAccesRepository;
import com.ocp.at.service.MoyenAccesService;
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
public class MoyenAccesServiceImpl implements MoyenAccesService {

    private final MoyenAccesRepository repository;
    private final MoyenAccesMapper mapper;

    @Override
    @Transactional
    public MoyenAccesResponse create(MoyenAccesRequest request) {
        log.info("Création d'un(e) MoyenAcces");
        MoyenAcces entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public MoyenAccesResponse update(String id, MoyenAccesRequest request) {
        log.info("Modification d'un(e) MoyenAcces avec ID: {}", id);
        MoyenAcces entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MoyenAcces non trouvé(e)"));
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public MoyenAccesResponse getById(String id) {
        log.info("Consultation d'un(e) MoyenAcces avec ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("MoyenAcces non trouvé(e)"));
    }

    @Override
    public List<MoyenAccesResponse> getAll() {
        log.info("Consultation de tous/toutes les MoyenAcces");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MoyenAccesResponse> search(String query, Pageable pageable) {
        log.info("Recherche MoyenAcces avec query: {}", query);
        Specification<MoyenAcces> spec = Specification.where(null);
        // Implement search logic if needed
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Suppression d'un(e) MoyenAcces avec ID: {}", id);
        MoyenAcces entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MoyenAcces non trouvé(e)"));

        // Check if used in AT before deleting (A implémenter)
        repository.delete(entity);
    }
}

