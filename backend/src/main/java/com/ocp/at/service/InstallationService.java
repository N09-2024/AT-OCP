package com.ocp.at.service;

import com.ocp.at.dto.request.InstallationRequest;
import com.ocp.at.dto.response.InstallationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface InstallationService {
    InstallationResponse create(InstallationRequest request);
    InstallationResponse update(String id, InstallationRequest request);
    InstallationResponse getById(String id);
    List<InstallationResponse> getAll();
    Page<InstallationResponse> search(String query, Pageable pageable);
    void delete(String id);
}

