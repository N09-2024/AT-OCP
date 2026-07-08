package com.ocp.at.service;

import com.ocp.at.dto.request.ZoneRequest;
import com.ocp.at.dto.response.ZoneResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ZoneService {
    ZoneResponse create(ZoneRequest request);
    ZoneResponse update(String id, ZoneRequest request);
    ZoneResponse getById(String id);
    List<ZoneResponse> getAll();
    Page<ZoneResponse> search(String query, Pageable pageable);
    void delete(String id);
}

