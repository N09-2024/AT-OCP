package com.ocp.at.service.impl;

import com.ocp.at.dto.request.AnalyseRisqueRequest;
import com.ocp.at.dto.response.AnalyseRisqueResponse;
import com.ocp.at.entity.*;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.AnalyseRisqueMapper;
import com.ocp.at.repository.*;
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
class AnalyseRisqueServiceImplTest {

    @Mock private AnalyseRisqueRepository analyseRepository;
    @Mock private VisitePrealableRepository visiteRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private RisqueRepository risqueRepository;
    @Mock private MesurePreparationRepository mesureRepository;
    @Mock private EPIRepository epiRepository;
    @Mock private MoyenAccesRepository moyenAccesRepository;
    @Mock private AnalyseRisqueMapper analyseMapper;

    @InjectMocks
    private AnalyseRisqueServiceImpl service;

    private VisitePrealable visiteFinalisee;
    private VisitePrealable visiteNonFinalisee;
    private AnalyseRisqueRequest request;

    @BeforeEach
    void setUp() {
        visiteFinalisee = VisitePrealable.builder()
                .id("visite-ok")
                .effectuee(true)
                .commentaire("OK")
                .build();

        visiteNonFinalisee = VisitePrealable.builder()
                .id("visite-ko")
                .effectuee(false)
                .build();

        request = new AnalyseRisqueRequest();
        request.setVisitePrealableId("visite-ok");
        request.setCommentaire("Analyse complète");
        request.setRisquesIds(List.of("r1"));
        request.setMesuresIds(List.of("m1"));
        request.setEpisIds(List.of("e1"));
        request.setMoyensAccesIds(List.of("ma1"));
    }

    // ─── Tests : Création ──────────────────────────────────────────────────────

    @Test
    void create_ShouldSucceed_WhenVisiteIsEffectueeAndNoAnalyseExists() {
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteFinalisee));
        when(analyseRepository.existsByVisitePrealableId("visite-ok")).thenReturn(false);
        when(risqueRepository.findAllById(any())).thenReturn(List.of(new Risque()));
        when(mesureRepository.findAllById(any())).thenReturn(List.of(new MesurePreparation()));
        when(epiRepository.findAllById(any())).thenReturn(List.of(new EPI()));
        when(moyenAccesRepository.findAllById(any())).thenReturn(List.of(new MoyenAcces()));
        when(analyseRepository.save(any())).thenReturn(new AnalyseRisque());
        when(analyseMapper.toResponse(any())).thenReturn(new AnalyseRisqueResponse());

        AnalyseRisqueResponse result = service.create(request);

        assertNotNull(result);
        verify(analyseRepository, times(1)).save(any(AnalyseRisque.class));
    }

    @Test
    void create_ShouldThrow_WhenVisiteNotEffectuee() {
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteNonFinalisee));
        request.setVisitePrealableId("visite-ok");

        // Re-mock to return non finalized
        visiteNonFinalisee = VisitePrealable.builder().id("visite-ok").effectuee(false).build();
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteNonFinalisee));

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(analyseRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrow_WhenAnalyseAlreadyExists() {
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteFinalisee));
        when(analyseRepository.existsByVisitePrealableId("visite-ok")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(request));
        assertTrue(ex.getMessage().contains("existe déjà"));
    }

    @Test
    void create_ShouldThrow_WhenNoRisques() {
        request.setRisquesIds(List.of());
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteFinalisee));
        when(analyseRepository.existsByVisitePrealableId("visite-ok")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void create_ShouldThrow_WhenNoMesures() {
        request.setMesuresIds(List.of());
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteFinalisee));
        when(analyseRepository.existsByVisitePrealableId("visite-ok")).thenReturn(false);
        when(risqueRepository.findAllById(any())).thenReturn(List.of(new Risque()));

        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void create_ShouldThrow_WhenNoEpis() {
        request.setEpisIds(List.of());
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteFinalisee));
        when(analyseRepository.existsByVisitePrealableId("visite-ok")).thenReturn(false);
        when(risqueRepository.findAllById(any())).thenReturn(List.of(new Risque()));
        when(mesureRepository.findAllById(any())).thenReturn(List.of(new MesurePreparation()));

        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void create_ShouldThrow_WhenNoMoyensAcces() {
        request.setMoyensAccesIds(List.of());
        when(visiteRepository.findById("visite-ok")).thenReturn(Optional.of(visiteFinalisee));
        when(analyseRepository.existsByVisitePrealableId("visite-ok")).thenReturn(false);
        when(risqueRepository.findAllById(any())).thenReturn(List.of(new Risque()));
        when(mesureRepository.findAllById(any())).thenReturn(List.of(new MesurePreparation()));
        when(epiRepository.findAllById(any())).thenReturn(List.of(new EPI()));

        assertThrows(BusinessException.class, () -> service.create(request));
    }

    // ─── Tests : Consultation ─────────────────────────────────────────────────

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(analyseRepository.findById("inconnu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById("inconnu"));
    }

    @Test
    void findByVisitePrealableId_ShouldThrow_WhenNotFound() {
        when(analyseRepository.findByVisitePrealableId("visite-inconnu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByVisitePrealableId("visite-inconnu"));
    }

    // ─── Tests : Suppression ─────────────────────────────────────────────────

    @Test
    void delete_ShouldSucceed_WhenExists() {
        when(analyseRepository.findById("analyse-1")).thenReturn(Optional.of(new AnalyseRisque()));

        assertDoesNotThrow(() -> service.delete("analyse-1"));
        verify(analyseRepository, times(1)).deleteById("analyse-1");
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(analyseRepository.findById("inconnu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete("inconnu"));
        verify(analyseRepository, never()).deleteById(anyString());
    }
}
