package com.ocp.at.service.impl;

import com.ocp.at.dto.request.ServiceRequest;
import com.ocp.at.dto.response.ServiceResponse;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.ServiceMapper;
import com.ocp.at.repository.InstallationRepository;
import com.ocp.at.repository.ServiceRepository;
import com.ocp.at.service.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository repository;
    private final ServiceMapper mapper;
    private final InstallationRepository installationRepository;

    @Override
    @Transactional
    public ServiceResponse create(ServiceRequest request) {
        log.info("Création d'un(e) Service");
        com.ocp.at.entity.Service entity = mapper.toEntity(request);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ServiceResponse update(String id, ServiceRequest request) {
        log.info("Modification d'un(e) Service avec ID: {}", id);
        com.ocp.at.entity.Service entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service non trouvé(e)"));
        mapper.updateEntityFromRequest(request, entity);
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public ServiceResponse getById(String id) {
        log.info("Consultation d'un(e) Service avec ID: {}", id);
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Service non trouvé(e)"));
    }

    @Override
    public List<ServiceResponse> getAll() {
        log.info("Consultation de tous/toutes les Service");
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceResponse> getByZoneId(String zoneId) {
        log.info("Consultation des services pour la zone ID: {}", zoneId);
        return repository.findByZoneId(zoneId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ServiceResponse> search(String query, Pageable pageable) {
        log.info("Recherche Service avec query: {}", query);
        Specification<com.ocp.at.entity.Service> spec = Specification.where(null);
        // Implement search logic if needed
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Suppression d'un(e) Service avec ID: {}", id);
        com.ocp.at.entity.Service entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service non trouvé(e)"));

        if (installationRepository.existsByServiceId(id)) {
            throw new BusinessException("Impossible de supprimer un Service qui contient des Installations.");
        }
        repository.delete(entity);
    }
}

