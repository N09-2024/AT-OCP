package com.ocp.at.dto.request;

import lombok.Data;

@Data
public class AnalyseIARequest {
    // Peut être utilisé pour forcer des paramètres d'analyse manuellement
    private String modeleForce;
}
