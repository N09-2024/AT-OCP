package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClosureReadinessResponse {
    private Boolean canClose;
    @Builder.Default
    private List<String> blockingReasons = new ArrayList<>();
    private String atNumero;
    private Boolean hasDeclarationFin;
    private Boolean hasReception;
    private Boolean isReceptionConforme;
    private Boolean hasVisaCeee;
    private Boolean hasVisaCeep;
}
