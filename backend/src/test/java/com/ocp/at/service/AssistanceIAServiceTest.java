package com.ocp.at.service;

import com.ocp.at.ai.MockAIProvider;
import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;
import com.ocp.at.service.OCRService;
import com.ocp.at.service.impl.AssistanceIAServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class AssistanceIAServiceTest {

    private AssistanceIAService assistanceIAService;
    private MockAIProvider mockAIProvider;

    @BeforeEach
    void setUp() {
        OCRService ocrService = Mockito.mock(OCRService.class);
        mockAIProvider = new MockAIProvider(ocrService);
        assistanceIAService = new AssistanceIAServiceImpl(mockAIProvider, mockAIProvider);
    }

    @Test
    void testAnalyzeAt() {
        AnalyzeAtRequest request = AnalyzeAtRequest.builder()
                .atId("AT-2026-TEST")
                .description("Travaux de chaudronnerie et soudure en hauteur")
                .installation("Unité phosphorique")
                .visiteFaite(true)
                .build();

        AnalyzeAtResponse response = assistanceIAService.analyzeAt(request);

        assertNotNull(response);
        assertNotNull(response.getIdentifiedRisks());
        assertTrue(response.getIdentifiedRisks().contains("Travail en hauteur") || response.getIdentifiedRisks().contains("Produits inflammables"));
        assertNotNull(response.getRecommendedMeasures());
        assertNotNull(response.getSources());
        assertFalse(response.getSources().isEmpty());
    }

    @Test
    void testChat() {
        ChatRequest request = ChatRequest.builder()
                .message("Quelles sont les obligations pour un permis de feu ?")
                .build();

        ChatResponse response = assistanceIAService.chat(request);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertFalse(response.getAnswer().isBlank());
        assertNotNull(response.getSources());
        assertFalse(response.getSources().isEmpty());
    }
}
