package com.ocp.at.service.impl;

import com.ocp.at.dto.request.DemandeInterventionRequest;
import com.ocp.at.dto.response.DemandeInterventionResponse;
import com.ocp.at.entity.AnalyseRisque;
import com.ocp.at.entity.DemandeIntervention;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.VisitePrealable;
import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.TypeIntervention;
import com.ocp.at.mapper.DemandeInterventionMapper;
import com.ocp.at.repository.DemandeInterventionRepository;
import com.ocp.at.repository.EquipementRepository;
import com.ocp.at.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandeInterventionServiceImplTest {

    @Mock
    private DemandeInterventionRepository repository;
    @Mock
    private DemandeInterventionMapper mapper;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private EquipementRepository equipementRepository;

    @InjectMocks
    private DemandeInterventionServiceImpl service;

    private DemandeInterventionRequest request;
    private DemandeIntervention entity;
    private DemandeInterventionResponse response;

    @BeforeEach
    void setUp() {
        request = new DemandeInterventionRequest();
        request.setObjet("Test Objet");
        request.setTypeIntervention(TypeIntervention.MECANIQUE);
        request.setNiveauIntervention(NiveauIntervention.NIVEAU_2);

        entity = new DemandeIntervention();
        entity.setObjet("Test Objet");
        entity.setTypeIntervention(TypeIntervention.MECANIQUE);
        entity.setNiveauIntervention(NiveauIntervention.NIVEAU_2);

        response = new DemandeInterventionResponse();
    }

    @Test
    void create_ShouldGenerateNumeroAndReturnResponse() {
        when(mapper.toEntity(any(DemandeInterventionRequest.class))).thenReturn(entity);
        when(repository.getNextSequence()).thenReturn(1L);
        when(repository.save(any(DemandeIntervention.class))).thenReturn(entity);
        when(mapper.toResponse(any(DemandeIntervention.class))).thenReturn(response);

        DemandeInterventionResponse result = service.create(request, null);

        assertNotNull(result);
        verify(repository, times(1)).getNextSequence();
        verify(repository, times(1)).save(any(DemandeIntervention.class));
        assertTrue(entity.getNumero().contains("-000001"));
        assertFalse(result.isAtCreable(), "L'AT ne doit pas être créable par défaut");
    }

    @Test
    void calculateAtCreable_ShouldBeTrue_WhenNiveau2AndVisiteAndAnalyseDone() {
        // Arrange
        VisitePrealable vp = new VisitePrealable();
        vp.setEffectuee(true);
        AnalyseRisque ar = new AnalyseRisque();
        vp.setAnalyseRisque(ar);
        
        entity.setVisitePrealable(vp);
        
        when(repository.findById("1")).thenReturn(Optional.of(entity));
        when(mapper.toResponse(any(DemandeIntervention.class))).thenReturn(response);

        // Act
        DemandeInterventionResponse result = service.findById("1");

        // Assert
        assertTrue(result.isAtCreable(), "L'AT doit être créable si la visite et l'analyse sont effectuées");
    }

    @Test
    void calculateAtCreable_ShouldBeFalse_WhenNiveau1() {
        // Arrange
        entity.setNiveauIntervention(NiveauIntervention.NIVEAU_1);
        VisitePrealable vp = new VisitePrealable();
        vp.setEffectuee(true);
        vp.setAnalyseRisque(new AnalyseRisque());
        entity.setVisitePrealable(vp);
        
        when(repository.findById("1")).thenReturn(Optional.of(entity));
        when(mapper.toResponse(any(DemandeIntervention.class))).thenReturn(response);

        // Act
        DemandeInterventionResponse result = service.findById("1");

        // Assert
        assertFalse(result.isAtCreable(), "L'AT ne doit jamais être créable pour une NIVEAU_1");
    }
}
