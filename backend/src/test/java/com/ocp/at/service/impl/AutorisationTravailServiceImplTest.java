package com.ocp.at.service.impl;

import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.EtatVerrou;
import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.mapper.AutorisationTravailMapper;
import com.ocp.at.repository.*;
import com.ocp.at.service.NotificationService;
import com.ocp.at.service.WorkflowATService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ocp.at.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutorisationTravailServiceImplTest {

    @Mock private AutorisationTravailRepository atRepository;
    @Mock private DemandeInterventionRepository diRepository;
    @Mock private VisitePrealableRepository visiteRepository;
    @Mock private AnalyseRisqueRepository analyseRepository;
    @Mock private HistoriqueATRepository historiqueRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private PermisRepository permisRepository;
    @Mock private WorkflowATService workflowService;
    @Mock private NotificationService notificationService;
    @Mock private AutorisationTravailMapper atMapper;

    @InjectMocks
    private AutorisationTravailServiceImpl service;

    private Utilisateur currentUser;
    private DemandeIntervention diNiveau2;
    private VisitePrealable visiteFinalisee;
    private AutorisationTravail atBrouillon;

    @BeforeEach
    void setUp() {
        currentUser = new Utilisateur();
        currentUser.setId("user-1");
        currentUser.setNom("Doe");

        visiteFinalisee = new VisitePrealable();
        visiteFinalisee.setId("visite-1");
        visiteFinalisee.setEffectuee(true);

        diNiveau2 = new DemandeIntervention();
        diNiveau2.setId("di-1");
        diNiveau2.setNumero("DI-2026");
        diNiveau2.setNiveauIntervention(NiveauIntervention.NIVEAU_2);
        diNiveau2.setVisitePrealable(visiteFinalisee);

        atBrouillon = new AutorisationTravail();
        atBrouillon.setId("at-1");
        atBrouillon.setNumero("AT-2026-000001");
        atBrouillon.setStatut(StatutAT.BROUILLON);
        atBrouillon.setEtatVerrou(EtatVerrou.EN_COURS_EDITION);
        atBrouillon.setProprietaireBrouillon(currentUser);
        atBrouillon.setVersion(1);
    }

    @Test
    void createFromDocument_ShouldSucceed_WhenAllConditionsMet() {
        try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUtilisateurId).thenReturn(Optional.of("user-1"));
            when(utilisateurRepository.findById("user-1")).thenReturn(Optional.of(currentUser));
            
            when(atRepository.existsByDemandeInterventionId("di-1")).thenReturn(false);
            when(diRepository.findById("di-1")).thenReturn(Optional.of(diNiveau2));
            when(visiteRepository.findById("visite-1")).thenReturn(Optional.of(visiteFinalisee));
            when(analyseRepository.existsByVisitePrealableId("visite-1")).thenReturn(true);
            when(atRepository.getNextSequence()).thenReturn(1L);
            when(atRepository.save(any(AutorisationTravail.class))).thenReturn(atBrouillon);
            when(atMapper.toResponse(any())).thenReturn(new AutorisationTravailResponse());

            AutorisationTravailResponse response = service.createFromDocument("di-1", "DI");

            assertNotNull(response);
            verify(atRepository).save(any(AutorisationTravail.class));
            verify(historiqueRepository).save(any(HistoriqueAT.class));
        }
    }

    @Test
    void createFromDocument_ShouldThrow_WhenAlreadyExists() {
        when(atRepository.existsByDemandeInterventionId("di-1")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createFromDocument("di-1", "DI"));
        assertTrue(ex.getMessage().contains("existe déjà"));
    }

    @Test
    void soumettreAT_ShouldSucceed_WhenValid() {
        try (MockedStatic<SecurityUtils> utilities = Mockito.mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUtilisateurId).thenReturn(Optional.of("user-1"));
            when(utilisateurRepository.findById("user-1")).thenReturn(Optional.of(currentUser));
            
            atBrouillon.setDateDebut(LocalDateTime.now().toLocalDate());
            atBrouillon.setDateFin(LocalDateTime.now().toLocalDate());
            atBrouillon.setHeureDebut(LocalDateTime.now().toLocalTime());
            atBrouillon.setHeureFin(LocalDateTime.now().toLocalTime());
            
            when(atRepository.findById("at-1")).thenReturn(Optional.of(atBrouillon));
            when(workflowService.verifierTransition(StatutAT.BROUILLON, TypeActionAT.SOUMISSION)).thenReturn(new WorkflowAT());
            when(permisRepository.findByAutorisationTravailId("at-1")).thenReturn(java.util.Collections.emptyList());
            when(atRepository.save(any())).thenReturn(atBrouillon);
            when(atMapper.toResponse(any())).thenReturn(new AutorisationTravailResponse());

            service.soumettreAT("at-1");

            assertEquals(StatutAT.SOUMISE, atBrouillon.getStatut());
            assertEquals(EtatVerrou.LIBRE, atBrouillon.getEtatVerrou());
            verify(historiqueRepository).save(any(HistoriqueAT.class));
            verify(notificationService).sendNotificationToRole(anyString(), anyString(), anyString(), anyString(), anyString());
        }
    }

    @Test
    void calculerMotifsRefusExportPdf_ShouldRefuse_WhenSignaturesMissing() {
        AutorisationTravail at = new AutorisationTravail();
        at.setId("at-test-1");
        at.setStatut(StatutAT.VALIDEE);

        java.util.List<String> motifs = service.calculerMotifsRefusExportPdf(at);
        assertFalse(motifs.isEmpty());
        assertTrue(motifs.stream().anyMatch(m -> m.contains("HCEP")));
        assertTrue(motifs.stream().anyMatch(m -> m.contains("HCEE")));
        assertTrue(motifs.stream().anyMatch(m -> m.contains("HMEP")));
        assertTrue(motifs.stream().anyMatch(m -> m.contains("HMEE")));
    }
}
