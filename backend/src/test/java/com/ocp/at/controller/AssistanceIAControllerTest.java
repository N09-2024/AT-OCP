package com.ocp.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;
import com.ocp.at.service.AssistanceIAService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistanceIAController.class)
@AutoConfigureMockMvc
class AssistanceIAControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssistanceIAService assistanceIAService;

    @Test
    @WithMockUser
    void testAnalyzeAtEndpoint() throws Exception {
        AnalyzeAtResponse mockResponse = AnalyzeAtResponse.builder()
                .summary("Synthèse IA")
                .identifiedRisks(List.of("Travail en hauteur"))
                .recommendedMeasures(List.of("Balisage"))
                .sources(List.of("Standard OCP S-HSE-SEC-31"))
                .confidence("HIGH")
                .build();

        Mockito.when(assistanceIAService.analyzeAt(any())).thenReturn(mockResponse);

        AnalyzeAtRequest request = AnalyzeAtRequest.builder()
                .description("Travaux de toiture")
                .visiteFaite(true)
                .build();

        mockMvc.perform(post("/api/ai/analyze-at")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Synthèse IA"))
                .andExpect(jsonPath("$.identifiedRisks[0]").value("Travail en hauteur"));
    }

    @Test
    @WithMockUser
    void testChatEndpoint() throws Exception {
        ChatResponse mockResponse = ChatResponse.builder()
                .answer("Le CEEP valide la préparation.")
                .sources(List.of("Standard OCP S-HSE-SEC-31 §8"))
                .confidence("HIGH")
                .build();

        Mockito.when(assistanceIAService.chat(any())).thenReturn(mockResponse);

        ChatRequest request = ChatRequest.builder()
                .message("Quel est le rôle du CEEP ?")
                .build();

        mockMvc.perform(post("/api/ai/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Le CEEP valide la préparation."))
                .andExpect(jsonPath("$.sources[0]").value("Standard OCP S-HSE-SEC-31 §8"));
    }
}
