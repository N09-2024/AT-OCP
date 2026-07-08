package com.ocp.at.service.impl;

import com.ocp.at.dto.request.VisitePrealableRequest;
import com.ocp.at.dto.response.VisitePrealableResponse;
import com.ocp.at.entity.*;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PhotoMapper;
import com.ocp.at.mapper.VisitePrealableMapper;
import com.ocp.at.repository.*;
import com.ocp.at.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitePrealableServiceImplTest {

    @Mock private VisitePrealableRepository visiteRepository;
    @Mock private PhotoRepository photoRepository;
    @Mock private DemandeInterventionRepository diRepository;
    @Mock private OrdreTravailRepository otRepository;
    @Mock private BonTravailRepository btRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private RisqueRepository risqueRepository;
    @Mock private StorageService storageService;
    @Mock private VisitePrealableMapper visiteMapper;
    @Mock private PhotoMapper photoMapper;

    @InjectMocks
    private VisitePrealableServiceImpl service;

    private VisitePrealableRequest request;
    private VisitePrealable visite;
    private DemandeIntervention di;

    @BeforeEach
    void setUp() {
        request = new VisitePrealableRequest();
        request.setDocumentSourceId("di-123");
        request.setTypeDocumentSource("DI");
        request.setCommentaire("Visite de test");
        request.setLatitude(33.5);
        request.setLongitude(-7.6);

        visite = VisitePrealable.builder()
                .id("visite-1")
                .commentaire("Visite de test")
                .latitude(33.5)
                .longitude(-7.6)
                .effectuee(false)
                .build();

        di = new DemandeIntervention();
        di.setId("di-123");
        di.setNumero("DI-2026-000001");
    }

    // ─── Tests : Création ──────────────────────────────────────────────────────

    @Test
    void create_ShouldSucceed_WhenNoPreviousVisiteForDI() {
        lenient().when(visiteRepository.existsForDI("di-123")).thenReturn(false);
        lenient().when(risqueRepository.findAllById(any())).thenReturn(List.of());
        lenient().when(visiteRepository.save(any())).thenReturn(visite);
        lenient().when(diRepository.findById("di-123")).thenReturn(Optional.of(di));
        lenient().when(diRepository.save(any())).thenReturn(di);
        di.setVisitePrealable(visite);
        lenient().when(visiteMapper.toResponse(any())).thenReturn(new VisitePrealableResponse());

        VisitePrealableResponse result = service.create(request);

        assertNotNull(result);
        verify(visiteRepository, times(1)).save(any(VisitePrealable.class));
    }

    @Test
    void create_ShouldThrow_WhenVisiteAlreadyExistsForDI() {
        when(visiteRepository.existsForDI("di-123")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(visiteRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrow_WhenTypeDocumentIsInvalid() {
        request.setTypeDocumentSource("INVALIDE");

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(visiteRepository, never()).existsForDI(anyString());
    }

    // ─── Tests : Finalisation ─────────────────────────────────────────────────

    @Test
    void finaliser_ShouldSucceed_WhenAllConditionsMet() {
        Photo photo = new Photo();
        visite.setPhotos(List.of(photo));

        when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visite));
        when(visiteRepository.save(any())).thenReturn(visite);
        when(diRepository.findAll()).thenReturn(List.of());
        when(otRepository.findAll()).thenReturn(List.of());
        when(btRepository.findAll()).thenReturn(List.of());
        when(visiteMapper.toResponse(any())).thenReturn(new VisitePrealableResponse());

        service.finaliser("visite-1");

        assertTrue(visite.isEffectuee(), "La visite doit être marquée comme effectuée");
        verify(visiteRepository, times(1)).save(visite);
    }

    @Test
    void finaliser_ShouldThrow_WhenAlreadyFinalisee() {
        visite.setEffectuee(true);
        when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visite));

        assertThrows(BusinessException.class, () -> service.finaliser("visite-1"));
        verify(visiteRepository, never()).save(any());
    }

    @Test
    void finaliser_ShouldThrow_WhenGPSMissing() {
        visite.setLatitude(null);
        visite.setLongitude(null);
        visite.setPhotos(List.of(new Photo()));
        when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visite));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.finaliser("visite-1"));
        assertTrue(ex.getMessage().contains("GPS"));
    }

    @Test
    void finaliser_ShouldThrow_WhenNoPhotos() {
        visite.setPhotos(List.of());
        when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visite));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.finaliser("visite-1"));
        assertTrue(ex.getMessage().contains("photo"));
    }

    @Test
    void finaliser_ShouldThrow_WhenCommentaireVide() {
        visite.setCommentaire("");
        visite.setPhotos(List.of(new Photo()));
        when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visite));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.finaliser("visite-1"));
        assertTrue(ex.getMessage().contains("commentaire"));
    }

    // ─── Tests : Modification ─────────────────────────────────────────────────

    @Test
    void update_ShouldThrow_WhenVisiteIsFinalisee() {
        visite.setEffectuee(true);
        when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visite));

        assertThrows(BusinessException.class, () -> service.update("visite-1", request));
        verify(visiteRepository, never()).save(any());
    }

    // ─── Tests : Suppression ─────────────────────────────────────────────────

    @Test
    void delete_ShouldThrow_WhenAnalyseExists() {
        visite.setAnalyseRisque(new AnalyseRisque());
        when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visite));

        assertThrows(BusinessException.class, () -> service.delete("visite-1"));
        verify(visiteRepository, never()).deleteById(anyString());
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(visiteRepository.findById("inconnu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById("inconnu"));
    }
}
