package com.ocp.at.service;

import com.ocp.at.dto.request.OrdreTravailRequest;
import com.ocp.at.dto.response.OrdreTravailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdreTravailService {
    OrdreTravailResponse create(OrdreTravailRequest request, String demandeurId);
    OrdreTravailResponse update(String id, OrdreTravailRequest request);
    void delete(String id);
    OrdreTravailResponse findById(String id);
    Page<OrdreTravailResponse> findAll(Pageable pageable);
    Page<OrdreTravailResponse> search(String query, Pageable pageable);
}
