package com.ocp.at.service.impl;

import com.ocp.at.dto.request.ZoneRequest;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.entity.Zone;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.ZoneMapper;
import com.ocp.at.repository.ZoneRepository;
import com.ocp.at.service.ZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.ocp.at.repository.ServiceRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository repository;
    private final ZoneMapper mapper;
    private final ServiceRepository serviceRepository;

    @Override
    @Transactional
    public ZoneResponse create(ZoneRequest request) {
        log.info("Création d'un(e) Zone");
        Zone entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ZoneResponse update(String id, ZoneRequest request) {
        log.info("Modification d'un(e) Zone avec ID: {}", id);
        Zone entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvé(e)"));
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public ZoneResponse getById(String id) {
        log.info("Consultation d'un(e) Zone avec ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvé(e)"));
    }

    @Override
    public List<ZoneResponse> getAll() {
        log.info("Consultation de tous/toutes les Zone");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ZoneResponse> search(String query, Pageable pageable) {
        log.info("Recherche Zone avec query: {}", query);
        Specification<Zone> spec = Specification.where(null);
        // Implement search logic if needed
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Suppression d'un(e) Zone avec ID: {}", id);
        Zone entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvé(e)"));

        if (serviceRepository.existsByZoneId(id)) {
            throw new BusinessException("Impossible de supprimer une Zone qui contient des Services.");
        }
        repository.delete(entity);
    }
}

