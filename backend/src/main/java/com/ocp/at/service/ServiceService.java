package com.ocp.at.service;

import com.ocp.at.dto.request.ServiceRequest;
import com.ocp.at.dto.response.ServiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ServiceService {
    ServiceResponse create(ServiceRequest request);
    ServiceResponse update(String id, ServiceRequest request);
    ServiceResponse getById(String id);
    List<ServiceResponse> getAll();
    List<ServiceResponse> getByZoneId(String zoneId);
    Page<ServiceResponse> search(String query, Pageable pageable);
    void delete(String id);
}

