package com.ocp.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocp.at.controller.ZoneController;
import com.ocp.at.dto.request.ZoneRequest;
import com.ocp.at.dto.response.ZoneResponse;
import com.ocp.at.service.ZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ZoneController.class)
public class ZoneControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ZoneService zoneService;

    @MockBean
    private com.ocp.at.service.ServiceService serviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private ZoneResponse zoneResponse;

    @BeforeEach
    void setUp() {
        zoneResponse = ZoneResponse.builder()
                .id("zone-1")
                .nomZone("Zone Test 1")
                .codeZone("Z-T1")
                .descriptionZone("Description")
                .build();
    }

    @Test
    @WithMockUser(authorities = "MANAGE_REFERENTIELS")
    void shouldCreateZone() throws Exception {
        ZoneRequest request = ZoneRequest.builder()
                .nomZone("Nouvelle Zone")
                .codeZone("NZ")
                .descriptionZone("Nouvelle description")
                .build();

        ZoneResponse createdResponse = ZoneResponse.builder()
                .id("zone-new")
                .nomZone("Nouvelle Zone")
                .codeZone("NZ")
                .build();

        when(zoneService.create(any(ZoneRequest.class))).thenReturn(createdResponse);

        mockMvc.perform(post("/api/zones")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomZone", is("Nouvelle Zone")))
                .andExpect(jsonPath("$.codeZone", is("NZ")));
    }

    @Test
    @WithMockUser(authorities = "MANAGE_REFERENTIELS")
    void shouldFailCreateZoneValidation() throws Exception {
        ZoneRequest request = ZoneRequest.builder()
                .nomZone("") // Invalid: NotBlank
                .codeZone("NZ")
                .build();

        mockMvc.perform(post("/api/zones")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldGetAllZones() throws Exception {
        when(zoneService.getAll()).thenReturn(List.of(zoneResponse));

        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nomZone", is("Zone Test 1")));
    }

    @Test
    @WithMockUser(authorities = "MANAGE_REFERENTIELS")
    void shouldDeleteZone() throws Exception {
        mockMvc.perform(delete("/api/zones/{id}", "zone-1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
