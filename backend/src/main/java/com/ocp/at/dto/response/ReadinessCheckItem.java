package com.ocp.at.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadinessCheckItem {
    private String code;
    private String label;
    private Boolean passed;
    private Boolean blocking;
    private String message;
    private String details;
}
