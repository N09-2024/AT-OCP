package com.ocp.at.service;

import com.ocp.at.dto.request.MesurePreparationRequest;
import com.ocp.at.dto.response.MesurePreparationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MesurePreparationService {
    MesurePreparationResponse create(MesurePreparationRequest request);
    MesurePreparationResponse update(String id, MesurePreparationRequest request);
    MesurePreparationResponse getById(String id);
    List<MesurePreparationResponse> getAll();
    Page<MesurePreparationResponse> search(String query, Pageable pageable);
    void delete(String id);
}

