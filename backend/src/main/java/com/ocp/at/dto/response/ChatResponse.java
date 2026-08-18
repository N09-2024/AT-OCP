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
public class ChatResponse {

    private String answer;

    @Builder.Default
    private List<String> sources = new ArrayList<>();

    private String confidence;

    @Builder.Default
    private List<String> suggestedQuestions = new ArrayList<>();
}
