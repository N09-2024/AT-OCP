package com.ocp.at.service;

import com.ocp.at.dto.request.RisqueRequest;
import com.ocp.at.dto.response.RisqueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RisqueService {
    RisqueResponse create(RisqueRequest request);
    RisqueResponse update(String id, RisqueRequest request);
    RisqueResponse getById(String id);
    List<RisqueResponse> getAll();
    Page<RisqueResponse> search(String query, Pageable pageable);
    void delete(String id);
}

