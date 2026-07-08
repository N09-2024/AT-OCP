package com.ocp.at.service;

import com.ocp.at.dto.request.MoyenAccesRequest;
import com.ocp.at.dto.response.MoyenAccesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MoyenAccesService {
    MoyenAccesResponse create(MoyenAccesRequest request);
    MoyenAccesResponse update(String id, MoyenAccesRequest request);
    MoyenAccesResponse getById(String id);
    List<MoyenAccesResponse> getAll();
    Page<MoyenAccesResponse> search(String query, Pageable pageable);
    void delete(String id);
}

