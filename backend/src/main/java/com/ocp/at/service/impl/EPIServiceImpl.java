package com.ocp.at.service.impl;

import com.ocp.at.dto.request.EPIRequest;
import com.ocp.at.dto.response.EPIResponse;
import com.ocp.at.entity.EPI;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.EPIMapper;
import com.ocp.at.repository.EPIRepository;
import com.ocp.at.service.EPIService;
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
public class EPIServiceImpl implements EPIService {

    private final EPIRepository repository;
    private final EPIMapper mapper;

    @Override
    @Transactional
    public EPIResponse create(EPIRequest request) {
        log.info("Création d'un(e) EPI");
        EPI entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public EPIResponse update(String id, EPIRequest request) {
        log.info("Modification d'un(e) EPI avec ID: {}", id);
        EPI entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EPI non trouvé(e)"));
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public EPIResponse getById(String id) {
        log.info("Consultation d'un(e) EPI avec ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("EPI non trouvé(e)"));
    }

    @Override
    public List<EPIResponse> getAll() {
        log.info("Consultation de tous/toutes les EPI");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EPIResponse> search(String query, Pageable pageable) {
        log.info("Recherche EPI avec query: {}", query);
        Specification<EPI> spec = Specification.where(null);
        // Implement search logic if needed
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Suppression d'un(e) EPI avec ID: {}", id);
        EPI entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EPI non trouvé(e)"));

        // Check if used in AT before deleting (A implémenter)
        repository.delete(entity);
    }
}

