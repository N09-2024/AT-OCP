package com.ocp.at.service.impl;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Visa;
import com.ocp.at.entity.WorkflowAT;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.repository.VisaRepository;
import com.ocp.at.repository.WorkflowATRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests WorkflowATServiceImpl")
class WorkflowATServiceImplTest {

    @Mock
    private WorkflowATRepository workflowRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private AutorisationTravailRepository atRepository;

    @Mock
    private VisaRepository visaRepository;

    @InjectMocks
    private WorkflowATServiceImpl workflowATService;

    private AutorisationTravail at;
    private WorkflowAT workflow;
    private List<Visa> visas;

    @BeforeEach
    void setUp() {
        at = AutorisationTravail.builder()
                .id("at-001")
                .numero("AT-2026-000001")
                .statut(StatutAT.BROUILLON)
                .build();

        workflow = WorkflowAT.builder()
                .id("wf-001")
                .etatDepart(StatutAT.BROUILLON)
                .etatArrivee(StatutAT.SOUMISE)
                .action(TypeActionAT.SOUMISSION)
                .roleAutorise("ROLE_RESPONSABLE")
                .validationObligatoire(false)
                .actif(true)
                .build();

        Visa visa1 = Visa.builder()
                .id("visa-001")
                .statut(StatutVisa.VALIDE)
                .ordre(1)
                .build();
        Visa visa2 = Visa.builder()
                .id("visa-002")
                .statut(StatutVisa.VALIDE)
                .ordre(2)
                .build();

        visas = Arrays.asList(visa1, visa2);
    }

    @Nested
    @DisplayName("verifierTransition")
    class VerifierTransitionTests {

        @Test
        @DisplayName("doit retourner le workflow pour une transition valide")
        void doitRetournerWorkflowPourTransitionValide() {
            when(workflowRepository.findActiveTransition(StatutAT.BROUILLON, TypeActionAT.SOUMISSION))
                    .thenReturn(Optional.of(workflow));

            WorkflowAT result = workflowATService.verifierTransition(StatutAT.BROUILLON, TypeActionAT.SOUMISSION);

            assertThat(result).isNotNull();
            assertThat(result.getEtatArrivee()).isEqualTo(StatutAT.SOUMISE);
            assertThat(result.getAction()).isEqualTo(TypeActionAT.SOUMISSION);
            verify(workflowRepository).findActiveTransition(StatutAT.BROUILLON, TypeActionAT.SOUMISSION);
        }

        @Test
        @DisplayName("doit lever une exception pour une transition invalide")
        void doitLeverExceptionPourTransitionInvalide() {
            when(workflowRepository.findActiveTransition(any(), any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> 
                workflowATService.verifierTransition(StatutAT.BROUILLON, TypeActionAT.VALIDATION))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Transition de workflow non autorisée");
        }
    }

    @Nested
    @DisplayName("obtenirEtatSuivant")
    class ObtenirEtatSuivantTests {

        @Test
        @DisplayName("doit retourner l'état actuel si tous les visas ne sont pas validés")
        void doitRetournerEtatActuelSiVisasNonValidés() {
            when(atRepository.findById("at-001")).thenReturn(Optional.of(at));
            
            Visa visaNonValide = Visa.builder()
                    .id("visa-001")
                    .statut(StatutVisa.EN_ATTENTE)
                    .ordre(1)
                    .build();
            when(visaRepository.findByAutorisationTravailId("at-001"))
                    .thenReturn(Collections.singletonList(visaNonValide));
            
            // Pas de transition disponible
            when(workflowRepository.findByEtatDepartAndActifTrue(StatutAT.BROUILLON))
                    .thenReturn(Collections.emptyList());

            StatutAT result = workflowATService.obtenirEtatSuivant("at-001", StatutAT.BROUILLON);

            assertThat(result).isEqualTo(StatutAT.BROUILLON);
        }

        @Test
        @DisplayName("doit retourner le nouvel état pour transition automatique")
        void doitRetournerNouvelEtatPourTransitionAutomatique() {
            when(atRepository.findById("at-001")).thenReturn(Optional.of(at));
            when(visaRepository.findByAutorisationTravailId("at-001")).thenReturn(visas);
            
            WorkflowAT transitionAuto = WorkflowAT.builder()
                    .id("wf-003")
                    .etatDepart(StatutAT.BROUILLON)
                    .etatArrivee(StatutAT.SOUMISE)
                    .validationObligatoire(false)
                    .actif(true)
                    .build();
            when(workflowRepository.findByEtatDepartAndActifTrue(StatutAT.BROUILLON))
                    .thenReturn(Collections.singletonList(transitionAuto));

            StatutAT result = workflowATService.obtenirEtatSuivant("at-001", StatutAT.BROUILLON);

            assertThat(result).isEqualTo(StatutAT.SOUMISE);
        }

        @Test
        @DisplayName("doit lever une exception si l'AT n'existe pas")
        void doitLeverExceptionSiATNonTrouvée() {
            when(atRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> 
                workflowATService.obtenirEtatSuivant("invalid-id", StatutAT.BROUILLON))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("AT non trouvée");
        }

        @Test
        @DisplayName("doit retourner l'état actuel si aucune transition n'est disponible")
        void doitRetournerEtatActuelSiPasDeTransition() {
            when(atRepository.findById("at-001")).thenReturn(Optional.of(at));
            when(visaRepository.findByAutorisationTravailId("at-001")).thenReturn(visas);
            when(workflowRepository.findByEtatDepartAndActifTrue(StatutAT.BROUILLON))
                    .thenReturn(Collections.emptyList());

            StatutAT result = workflowATService.obtenirEtatSuivant("at-001", StatutAT.BROUILLON);

            assertThat(result).isEqualTo(StatutAT.BROUILLON);
        }
    }
}
