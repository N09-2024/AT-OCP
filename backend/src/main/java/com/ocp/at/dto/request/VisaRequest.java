package com.ocp.at.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VisaRequest {
    @NotBlank
    private String autorisationTravailId;
    private String commentaire;
    private Integer ordre;
}
