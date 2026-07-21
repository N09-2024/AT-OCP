package com.ocp.at.service.impl;

import com.ocp.at.dto.request.RoleRequest;
import com.ocp.at.dto.response.PermissionResponse;
import com.ocp.at.dto.response.RoleResponse;
import com.ocp.at.entity.Permission;
import com.ocp.at.entity.Role;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PermissionMapper;
import com.ocp.at.mapper.RoleMapper;
import com.ocp.at.repository.PermissionRepository;
import com.ocp.at.repository.RoleRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public RoleResponse creer(RoleRequest request) {
        if (roleRepository.existsByNom(request.getNom())) {
            throw new BusinessException("Un rôle avec le nom '" + request.getNom() + "' existe déjà");
        }
        Role role = roleMapper.toEntity(request);
        Role saved = roleRepository.save(role);
        logger.info("Rôle créé: {}", saved.getNom());
        return enrichRoleResponse(roleMapper.toResponse(saved), saved);
    }

    @Override
    public RoleResponse trouverParId(String id) {
        Role role = findRoleById(id);
        return enrichRoleResponse(roleMapper.toResponse(role), role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> listerTous(String search, Pageable pageable) {
        Page<Role> page = (search != null && !search.isBlank())
                ? roleRepository.findByNomContainingIgnoreCaseWithPermissions(search, pageable)
                : roleRepository.findAllWithPermissions(pageable);
        List<RoleResponse> content = page.getContent().stream()
                .map(role -> enrichRoleResponse(roleMapper.toResponse(role), role))
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
    
    private RoleResponse enrichRoleResponse(RoleResponse response, Role role) {
        response.setPermissionsCount(role.getPermissions() != null ? role.getPermissions().size() : 0);
        response.setUsersCount(utilisateurRepository.countByRolesId(role.getId()));
        return response;
    }

    @Override
    @Transactional
    public RoleResponse modifier(String id, RoleRequest request) {
        Role role = findRoleById(id);
        role.setNom(request.getNom());
        role.setDescription(request.getDescription());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void supprimer(String id) {
        Role role = findRoleById(id);
        roleRepository.delete(role);
        logger.info("Rôle supprimé: {}", role.getNom());
    }

    @Override
    public Set<PermissionResponse> getPermissions(String id) {
        return findRoleById(id).getPermissions().stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public RoleResponse affecterPermission(String roleId, String permissionId) {
        Role role = findRoleById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission introuvable: " + permissionId));
        role.getPermissions().add(permission);
        Role saved = roleRepository.save(role);
        logger.info("Permission '{}' affectée au rôle '{}'", permission.getNom(), role.getNom());
        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RoleResponse retirerPermission(String roleId, String permissionId) {
        Role role = findRoleById(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission introuvable: " + permissionId));
        role.getPermissions().remove(permission);
        Role saved = roleRepository.save(role);
        logger.info("Permission '{}' retirée du rôle '{}'", permission.getNom(), role.getNom());
        return roleMapper.toResponse(saved);
    }

    private Role findRoleById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable: " + id));
    }
}