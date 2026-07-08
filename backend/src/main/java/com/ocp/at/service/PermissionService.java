package com.ocp.at.service;

import com.ocp.at.dto.request.PermissionRequest;
import com.ocp.at.dto.response.PermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionService {
    PermissionResponse creer(PermissionRequest request);
    PermissionResponse trouverParId(String id);
    Page<PermissionResponse> listerTous(String search, Pageable pageable);
    PermissionResponse modifier(String id, PermissionRequest request);
    void supprimer(String id);
}
