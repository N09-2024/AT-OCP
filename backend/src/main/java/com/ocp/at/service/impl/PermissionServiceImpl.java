package com.ocp.at.service.impl;

import com.ocp.at.dto.request.PermissionRequest;
import com.ocp.at.dto.response.PermissionResponse;
import com.ocp.at.entity.Permission;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PermissionMapper;
import com.ocp.at.repository.PermissionRepository;
import com.ocp.at.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionResponse creer(PermissionRequest request) {
        if (permissionRepository.existsByNom(request.getNom())) {
            throw new BusinessException("Une permission avec le nom '" + request.getNom() + "' existe déjà");
        }
        Permission permission = permissionMapper.toEntity(request);
        Permission saved = permissionRepository.save(permission);
        logger.info("Permission créée: {}", saved.getNom());
        return permissionMapper.toResponse(saved);
    }

    @Override
    public PermissionResponse trouverParId(String id) {
        return permissionMapper.toResponse(findPermissionById(id));
    }

    @Override
    public Page<PermissionResponse> listerTous(String search, Pageable pageable) {
        Page<Permission> page = (search != null && !search.isBlank())
                ? permissionRepository.findByNomContainingIgnoreCase(search, pageable)
                : permissionRepository.findAll(pageable);
        return page.map(permissionMapper::toResponse);
    }

    @Override
    @Transactional
    public PermissionResponse modifier(String id, PermissionRequest request) {
        Permission permission = findPermissionById(id);
        permission.setNom(request.getNom());
        permission.setDescription(request.getDescription());
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional
    public void supprimer(String id) {
        Permission permission = findPermissionById(id);
        permissionRepository.delete(permission);
        logger.info("Permission supprimée: {}", permission.getNom());
    }

    private Permission findPermissionById(String id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission introuvable: " + id));
    }
}
