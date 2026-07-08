package com.ocp.at.service;

import com.ocp.at.dto.request.EquipementRequest;
import com.ocp.at.dto.response.EquipementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EquipementService {
    EquipementResponse create(EquipementRequest request);
    EquipementResponse update(String id, EquipementRequest request);
    EquipementResponse getById(String id);
    List<EquipementResponse> getAll();
    Page<EquipementResponse> search(String query, Pageable pageable);
    void delete(String id);
}

