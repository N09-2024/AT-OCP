package com.ocp.at.service;

import com.ocp.at.dto.request.RoleRequest;
import com.ocp.at.dto.response.PermissionResponse;
import com.ocp.at.dto.response.RoleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface RoleService {
    RoleResponse creer(RoleRequest request);
    RoleResponse trouverParId(String id);
    Page<RoleResponse> listerTous(String search, Pageable pageable);
    RoleResponse modifier(String id, RoleRequest request);
    void supprimer(String id);
    Set<PermissionResponse> getPermissions(String id);
    RoleResponse affecterPermission(String roleId, String permissionId);
    RoleResponse retirerPermission(String roleId, String permissionId);
}
