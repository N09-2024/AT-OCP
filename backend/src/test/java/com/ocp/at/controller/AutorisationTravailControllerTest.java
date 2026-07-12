package com.ocp.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocp.at.dto.request.AutoSaveRequest;
import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.entity.enums.EtatVerrou;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.service.AutorisationTravailService;
import com.ocp.at.service.PdfGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AutorisationTravailController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simplicity in these unit tests
class AutorisationTravailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutorisationTravailService atService;

    @MockBean
    private PdfGeneratorService pdfGeneratorService;

    private AutorisationTravailResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new AutorisationTravailResponse();
        mockResponse.setId("at-1");
        mockResponse.setNumero("AT-2026-000001");
        mockResponse.setStatut(StatutAT.BROUILLON);
        mockResponse.setEtatVerrou(EtatVerrou.EN_COURS_EDITION);
        mockResponse.setVersion(1);
    }

    @Test
    @WithMockUser(authorities = "CREATE_AT")
    void createFromDocument_ShouldReturnCreatedAT() throws Exception {
        when(atService.createFromDocument("di-1", "DI")).thenReturn(mockResponse);

        mockMvc.perform(post("/api/documents/DI/di-1/creer-at")
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("at-1"))
                .andExpect(jsonPath("$.numero").value("AT-2026-000001"));
                
        verify(atService).createFromDocument("di-1", "DI");
    }

    @Test
    @WithMockUser(authorities = "EDIT_AT")
    void autoSave_ShouldReturnOk() throws Exception {
        AutoSaveRequest request = new AutoSaveRequest();
        request.setObjet("Mise à jour objet");
        
        when(atService.autoSave(eq("at-1"), any(AutoSaveRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/autorisations-travail/at-1/autosave")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());
                
        verify(atService).autoSave(eq("at-1"), any(AutoSaveRequest.class));
    }

    @Test
    @WithMockUser(authorities = "SUBMIT_AT")
    void soumettreAT_ShouldReturnOk() throws Exception {
        mockResponse.setStatut(StatutAT.SOUMISE);
        mockResponse.setEtatVerrou(EtatVerrou.LIBRE);
        when(atService.soumettreAT("at-1")).thenReturn(mockResponse);

        mockMvc.perform(post("/api/at/at-1/submit")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SOUMISE"))
                .andExpect(jsonPath("$.etatVerrou").value("LIBRE"));
                
        verify(atService).soumettreAT("at-1");
    }

    @Test
    @WithMockUser(authorities = "VALIDATE_AT")
    void validerAT_ShouldReturnOk() throws Exception {
        mockResponse.setStatut(StatutAT.VALIDEE);
        when(atService.validerAT("at-1")).thenReturn(mockResponse);

        mockMvc.perform(post("/api/at/at-1/validate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDEE"));
                
        verify(atService).validerAT("at-1");
    }

    @Test
    @WithMockUser(authorities = "CLOSE_AT")
    void cloturerAT_ShouldReturnOk() throws Exception {
        mockResponse.setStatut(StatutAT.CLOTUREE);
        when(atService.cloturerAT("at-1")).thenReturn(mockResponse);

        mockMvc.perform(post("/api/at/at-1/close")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CLOTUREE"));
                
        verify(atService).cloturerAT("at-1");
    }
}
