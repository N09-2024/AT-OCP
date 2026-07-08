package com.ocp.at.service;

import com.ocp.at.dto.request.BonTravailRequest;
import com.ocp.at.dto.response.BonTravailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BonTravailService {
    BonTravailResponse create(BonTravailRequest request, String demandeurId);
    BonTravailResponse update(String id, BonTravailRequest request);
    void delete(String id);
    BonTravailResponse findById(String id);
    Page<BonTravailResponse> findAll(Pageable pageable);
    Page<BonTravailResponse> search(String query, Pageable pageable);
}
