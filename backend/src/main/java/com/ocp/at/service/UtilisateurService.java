package com.ocp.at.service;

import com.ocp.at.dto.request.UtilisateurRequest;
import com.ocp.at.dto.request.UtilisateurUpdateRequest;
import com.ocp.at.dto.response.RoleResponse;
import com.ocp.at.dto.response.UtilisateurResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UtilisateurService {
    UtilisateurResponse creer(UtilisateurRequest request);
    UtilisateurResponse trouverParId(String id);
    Page<UtilisateurResponse> listerTous(String search, Pageable pageable);
    UtilisateurResponse modifier(String id, UtilisateurUpdateRequest request);
    void supprimer(String id);
    UtilisateurResponse activer(String id);
    UtilisateurResponse desactiver(String id);
    UtilisateurResponse deverrouiller(String id);
    Set<RoleResponse> getRoles(String id);
    UtilisateurResponse affecterRole(String id, String roleId);
    UtilisateurResponse retirerRole(String id, String roleId);
}
