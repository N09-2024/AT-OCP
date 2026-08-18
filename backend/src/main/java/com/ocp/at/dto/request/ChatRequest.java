package com.ocp.at.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    private String message;
    private String conversationId;
    private Map<String, Object> atContext;
}
