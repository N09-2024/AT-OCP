package com.ocp.at.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadPermisResponse {
    private String id;
    private String message;
    private String fichierJointId;
    private String analyseIAId;
    private String statutVerification;
}
