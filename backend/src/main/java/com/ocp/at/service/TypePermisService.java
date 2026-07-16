package com.ocp.at.service;

import com.ocp.at.dto.request.TypePermisRequest;
import com.ocp.at.dto.response.TypePermisResponse;

import java.util.List;

public interface TypePermisService {
    List<TypePermisResponse> getAllTypePermis();
    TypePermisResponse getTypePermisById(String id);
    TypePermisResponse createTypePermis(TypePermisRequest request);
    TypePermisResponse updateTypePermis(String id, TypePermisRequest request);
    void deleteTypePermis(String id);
}
