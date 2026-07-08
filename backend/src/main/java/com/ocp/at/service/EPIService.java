package com.ocp.at.service;

import com.ocp.at.dto.request.EPIRequest;
import com.ocp.at.dto.response.EPIResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EPIService {
    EPIResponse create(EPIRequest request);
    EPIResponse update(String id, EPIRequest request);
    EPIResponse getById(String id);
    List<EPIResponse> getAll();
    Page<EPIResponse> search(String query, Pageable pageable);
    void delete(String id);
}

