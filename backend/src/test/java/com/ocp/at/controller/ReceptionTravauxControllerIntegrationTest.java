package com.ocp.at.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocp.at.dto.request.PhotoReceptionRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.service.ReceptionTravauxService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceptionTravauxController.class)
public class ReceptionTravauxControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceptionTravauxService receptionService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReceptionTravauxResponse receptionResponse;
    private PhotoReceptionResponse photoResponse;
    private ReceptionTravauxRequest receptionRequest;

    @BeforeEach
    void setUp() {
        receptionResponse = ReceptionTravauxResponse.builder()
                .id("reception-001")
                .autorisationTravailId("at-001")
                .autorisationTravailNumero("AT-2026-000001")
                .travauxConformes(false)
                .atCloturee(false)
                .build();

        photoResponse = PhotoReceptionResponse.builder()
                .id("photo-001")
                .nom("photo1.jpg")
                .path("/uploads/photo1.jpg")
                .build();

        receptionRequest = ReceptionTravauxRequest.builder()
                .autorisationTravailId("at-001")
                .travauxRealises("Travaux terminés")
                .build();
    }

    @Test
    @WithMockUser(authorities = "CREATE_RECEPTION")
    void shouldCreateReception() throws Exception {
        when(receptionService.create(any(ReceptionTravauxRequest.class))).thenReturn(receptionResponse);

        mockMvc.perform(post("/api/receptions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receptionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("reception-001")))
                .andExpect(jsonPath("$.autorisationTravailId", is("at-001")));
    }

    @Test
    @WithMockUser(authorities = "CREATE_RECEPTION")
    void shouldFailCreateReceptionValidation() throws Exception {
        ReceptionTravauxRequest invalidRequest = new ReceptionTravauxRequest();
        // Missing autorisationTravailId which is @NotNull

        mockMvc.perform(post("/api/receptions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void shouldGetAllReceptions() throws Exception {
        when(receptionService.getAll(any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/receptions")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void shouldGetReceptionById() throws Exception {
        when(receptionService.getById("reception-001")).thenReturn(receptionResponse);

        mockMvc.perform(get("/api/receptions/reception-001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("reception-001")))
                .andExpect(jsonPath("$.autorisationTravailNumero", is("AT-2026-000001")));
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void shouldGetReceptionByAtId() throws Exception {
        when(receptionService.getByAutorisationTravailId("at-001")).thenReturn(receptionResponse);

        mockMvc.perform(get("/api/receptions/at/at-001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("reception-001")));
    }

    @Test
    @WithMockUser(authorities = "EDIT_RECEPTION")
    void shouldUpdateReception() throws Exception {
        when(receptionService.update(eq("reception-001"), any(ReceptionTravauxRequest.class))).thenReturn(receptionResponse);

        mockMvc.perform(put("/api/receptions/reception-001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receptionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("reception-001")));
    }

    @Test
    @WithMockUser(authorities = "DELETE_RECEPTION")
    void shouldDeleteReception() throws Exception {
        mockMvc.perform(delete("/api/receptions/reception-001")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "SIGN_RECEPTION")
    void shouldSignReception() throws Exception {
        when(receptionService.signer("reception-001", "/path/signature.png")).thenReturn(receptionResponse);

        mockMvc.perform(put("/api/receptions/reception-001/signer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("/path/signature.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("reception-001")));
    }

    @Test
    @WithMockUser(authorities = "CLOSE_AT")
    void shouldClotureAT() throws Exception {
        receptionResponse.setAtCloturee(true);
        when(receptionService.cloturerAT("reception-001")).thenReturn(receptionResponse);

        mockMvc.perform(put("/api/receptions/reception-001/cloturer")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atCloturee", is(true)));
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void shouldGetPhotos() throws Exception {
        when(receptionService.getPhotos("reception-001")).thenReturn(List.of(photoResponse));

        mockMvc.perform(get("/api/receptions/reception-001/photos")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nom", is("photo1.jpg")));
    }

    @Test
    @WithMockUser(authorities = "EDIT_RECEPTION")
    void shouldAddPhoto() throws Exception {
        PhotoReceptionRequest photoRequest = new PhotoReceptionRequest();
        photoRequest.setNom("photo1.jpg");
        photoRequest.setPath("/uploads/photo1.jpg");

        when(receptionService.ajouterPhoto(eq("reception-001"), any(PhotoReceptionRequest.class))).thenReturn(photoResponse);

        mockMvc.perform(post("/api/receptions/reception-001/photos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(photoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("photo-001")))
                .andExpect(jsonPath("$.nom", is("photo1.jpg")));
    }

    @Test
    @WithMockUser(authorities = "EDIT_RECEPTION")
    void shouldFailAddPhotoValidation() throws Exception {
        PhotoReceptionRequest invalidRequest = new PhotoReceptionRequest();
        // Missing nom and path which are @NotBlank

        mockMvc.perform(post("/api/receptions/reception-001/photos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "EDIT_RECEPTION")
    void shouldDeletePhoto() throws Exception {
        mockMvc.perform(delete("/api/receptions/reception-001/photos/photo-001")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
