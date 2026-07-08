package com.ocp.at.service;

import com.ocp.at.dto.request.VisaRequest;
import com.ocp.at.dto.response.VisaResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VisaService {
    VisaResponse createVisa(VisaRequest request);
    List<VisaResponse> getVisasByAtId(String atId);
    VisaResponse signVisa(String visaId, MultipartFile signature, String commentaire);
    Resource downloadSignature(String visaId);
}
