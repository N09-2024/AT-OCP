package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoyenAccesResponse {
    private String id;
    private String nomMoyen;
    private String descriptionMoyen;
}

