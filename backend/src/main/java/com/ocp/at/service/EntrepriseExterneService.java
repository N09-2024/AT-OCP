package com.ocp.at.service;

import com.ocp.at.dto.request.EntrepriseExterneRequest;
import com.ocp.at.dto.response.EntrepriseExterneResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EntrepriseExterneService {
    EntrepriseExterneResponse create(EntrepriseExterneRequest request);
    EntrepriseExterneResponse update(String id, EntrepriseExterneRequest request);
    EntrepriseExterneResponse getById(String id);
    List<EntrepriseExterneResponse> getAll();
    Page<EntrepriseExterneResponse> search(String query, Pageable pageable);
    void delete(String id);
}

