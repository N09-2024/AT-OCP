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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReceptionTravauxController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceptionTravauxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReceptionTravauxService receptionService;

    private ReceptionTravauxResponse mockResponse;
    private PhotoReceptionResponse mockPhotoResponse;
    private ReceptionTravauxRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockResponse = new ReceptionTravauxResponse();
        mockResponse.setId("reception-001");
        mockResponse.setAutorisationTravailId("at-001");
        mockResponse.setAutorisationTravailNumero("AT-2026-000001");
        mockResponse.setTravauxConformes(false);
        mockResponse.setAtCloturee(false);

        mockPhotoResponse = new PhotoReceptionResponse();
        mockPhotoResponse.setId("photo-001");
        mockPhotoResponse.setNom("photo1.jpg");
        mockPhotoResponse.setPath("/uploads/photo1.jpg");

        mockRequest = new ReceptionTravauxRequest();
        mockRequest.setAutorisationTravailId("at-001");
        mockRequest.setTravauxRealises("Travaux terminés");
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void getAll_ShouldReturnList() throws Exception {
        when(receptionService.getAll(any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/receptions")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(receptionService).getAll(any());
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void getById_ShouldReturnReception() throws Exception {
        when(receptionService.getById("reception-001")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/receptions/reception-001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reception-001"))
                .andExpect(jsonPath("$.autorisationTravailId").value("at-001"));

        verify(receptionService).getById("reception-001");
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void getByAt_ShouldReturnReception() throws Exception {
        when(receptionService.getByAutorisationTravailId("at-001")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/receptions/at/at-001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reception-001"))
                .andExpect(jsonPath("$.autorisationTravailId").value("at-001"));

        verify(receptionService).getByAutorisationTravailId("at-001");
    }

    @Test
    @WithMockUser(authorities = "CREATE_RECEPTION")
    void create_ShouldReturnCreated() throws Exception {
        when(receptionService.create(any(ReceptionTravauxRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/receptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("reception-001"));

        verify(receptionService).create(any(ReceptionTravauxRequest.class));
    }

    @Test
    @WithMockUser(authorities = "EDIT_RECEPTION")
    void update_ShouldReturnOk() throws Exception {
        when(receptionService.update(eq("reception-001"), any(ReceptionTravauxRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/receptions/reception-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reception-001"));

        verify(receptionService).update(eq("reception-001"), any(ReceptionTravauxRequest.class));
    }

    @Test
    @WithMockUser(authorities = "DELETE_RECEPTION")
    void delete_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/receptions/reception-001")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(receptionService).delete("reception-001");
    }

    @Test
    @WithMockUser(authorities = "SIGN_RECEPTION")
    void signer_ShouldReturnOk() throws Exception {
        when(receptionService.signer("reception-001", "/path/signature.png")).thenReturn(mockResponse);

        mockMvc.perform(put("/api/receptions/reception-001/signer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("/path/signature.png")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reception-001"));

        verify(receptionService).signer("reception-001", "/path/signature.png");
    }

    @Test
    @WithMockUser(authorities = "CLOSE_AT")
    void cloturer_ShouldReturnOk() throws Exception {
        mockResponse.setAtCloturee(true);
        when(receptionService.cloturerAT("reception-001")).thenReturn(mockResponse);

        mockMvc.perform(put("/api/receptions/reception-001/cloturer")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reception-001"))
                .andExpect(jsonPath("$.atCloturee").value(true));

        verify(receptionService).cloturerAT("reception-001");
    }

    @Test
    @WithMockUser(authorities = "VIEW_RECEPTION")
    void getPhotos_ShouldReturnList() throws Exception {
        when(receptionService.getPhotos("reception-001")).thenReturn(List.of(mockPhotoResponse));

        mockMvc.perform(get("/api/receptions/reception-001/photos")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("photo-001"))
                .andExpect(jsonPath("$[0].nom").value("photo1.jpg"));

        verify(receptionService).getPhotos("reception-001");
    }

    @Test
    @WithMockUser(authorities = "EDIT_RECEPTION")
    void ajouterPhoto_ShouldReturnCreated() throws Exception {
        PhotoReceptionRequest photoRequest = new PhotoReceptionRequest();
        photoRequest.setNom("photo1.jpg");
        photoRequest.setPath("/uploads/photo1.jpg");

        when(receptionService.ajouterPhoto(eq("reception-001"), any(PhotoReceptionRequest.class))).thenReturn(mockPhotoResponse);

        mockMvc.perform(post("/api/receptions/reception-001/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(photoRequest))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("photo-001"))
                .andExpect(jsonPath("$.nom").value("photo1.jpg"));

        verify(receptionService).ajouterPhoto(eq("reception-001"), any(PhotoReceptionRequest.class));
    }

    @Test
    @WithMockUser(authorities = "EDIT_RECEPTION")
    void supprimerPhoto_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/receptions/reception-001/photos/photo-001")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(receptionService).supprimerPhoto("reception-001", "photo-001");
    }
}
