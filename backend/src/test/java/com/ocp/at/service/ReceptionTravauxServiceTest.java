package com.ocp.at.service;

import com.ocp.at.dto.request.EssaiRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.EssaiResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.EssaiMapper;
import com.ocp.at.mapper.ReceptionTravauxMapper;
import com.ocp.at.mapper.RemiseEtatMapper;
import com.ocp.at.repository.*;
import com.ocp.at.service.impl.ReceptionTravauxServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReceptionTravauxServiceTest {

    @Mock private ReceptionTravauxRepository receptionRepository;
    @Mock private EssaiRepository essaiRepository;
    @Mock private RemiseEtatRepository remiseEtatRepository;
    @Mock private AutorisationTravailRepository atRepository;
    @Mock private HistoriqueATRepository historiqueRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;
    @Mock private ReceptionTravauxMapper receptionMapper;
    @Mock private EssaiMapper essaiMapper;
    @Mock private RemiseEtatMapper remiseEtatMapper;

    @InjectMocks
    private ReceptionTravauxServiceImpl service;

    private AutorisationTravail atValidee;
    private AutorisationTravail atBrouillon;
    private ReceptionTravaux reception;
    private ReceptionTravauxRequest request;
    private ReceptionTravauxResponse response;

    @BeforeEach
    void setUp() {
        atValidee = AutorisationTravail.builder()
                .id("at-validee-001")
                .numero("AT-2026-000001")
                .statut(StatutAT.VALIDEE)
                .build();

        atBrouillon = AutorisationTravail.builder()
                .id("at-brouillon-001")
                .numero("AT-2026-000002")
                .statut(StatutAT.BROUILLON)
                .build();

        reception = ReceptionTravaux.builder()
                .id("reception-001")
                .autorisationTravail(atValidee)
                .travauxConformes(false)
                .installationRemiseEnEtat(false)
                .essaisEffectues(false)
                .essaisConformes(false)
                .validee(false)
                .essais(new ArrayList<>())
                .build();

        request = new ReceptionTravauxRequest();
        request.setAutorisationTravailId("at-validee-001");
        request.setCommentaire("Réception initiale");

        response = new ReceptionTravauxResponse();
        response.setId("reception-001");
        response.setAutorisationTravailId("at-validee-001");

        // Stubs communs
        when(utilisateurRepository.findById(any())).thenReturn(Optional.empty());
        when(historiqueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(notificationService).createNotification(any(), any(), any(), any(), any());
        doNothing().when(notificationService).sendNotificationToRole(any(), any(), any(), any(), any());
        doNothing().when(auditService).logAction(any(), any(), any(), any(), any());
    }

    // ===================================================================
    // CREATE — Règles métier
    // ===================================================================

    @Test
    void create_Doit_LancerException_SiATNonValidee() {
        when(atRepository.findById("at-brouillon-001")).thenReturn(Optional.of(atBrouillon));

        ReceptionTravauxRequest req = new ReceptionTravauxRequest();
        req.setAutorisationTravailId("at-brouillon-001");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("VALIDÉE");
    }

    @Test
    void create_Doit_LancerException_SiReceptionDejaExistante() {
        when(atRepository.findById("at-validee-001")).thenReturn(Optional.of(atValidee));
        when(receptionRepository.existsByAutorisationTravailId("at-validee-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void create_Doit_Reussir_EtCreerHistoriqueEtNotification() {
        when(atRepository.findById("at-validee-001")).thenReturn(Optional.of(atValidee));
        when(receptionRepository.existsByAutorisationTravailId("at-validee-001")).thenReturn(false);
        when(receptionMapper.toEntity(any())).thenReturn(reception);
        when(receptionRepository.save(any())).thenReturn(reception);
        when(receptionMapper.toResponse(any())).thenReturn(response);

        ReceptionTravauxResponse result = service.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("reception-001");

        // Historique créé
        verify(historiqueRepository, times(1)).save(argThat(h ->
                h instanceof HistoriqueAT && ((HistoriqueAT) h).getAction().name().equals("RECEPTION_TRAVAUX")));

        // Notifications envoyées
        verify(notificationService, atLeastOnce()).sendNotificationToRole(eq("RESPONSABLE_OCP"), any(), any(), any(), any());
        verify(notificationService, atLeastOnce()).sendNotificationToRole(eq("RESPONSABLE_ENTREPRISE"), any(), any(), any(), any());

        // Audit logué
        verify(auditService, times(1)).logAction(eq("CREATION_RECEPTION"), eq("SUCCES"), any(), any(), any());
    }

    // ===================================================================
    // VALIDER — Règles métier
    // ===================================================================

    @Test
    void validerReception_Doit_LancerException_SiTravauxNonConformes() {
        reception.setTravauxConformes(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.validerReception("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("travaux ne sont pas déclarés conformes");
    }

    @Test
    void validerReception_Doit_LancerException_SiInstallationNonRemise() {
        reception.setTravauxConformes(true);
        reception.setInstallationRemiseEnEtat(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.validerReception("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("remise en état");
    }

    @Test
    void validerReception_Doit_LancerException_SiEssaisNonEffectues() {
        reception.setTravauxConformes(true);
        reception.setInstallationRemiseEnEtat(true);
        reception.setEssaisEffectues(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.validerReception("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("essais n'ont pas été effectués");
    }

    @Test
    void validerReception_Doit_LancerException_SiUnEssaiNonConforme() {
        reception.setTravauxConformes(true);
        reception.setInstallationRemiseEnEtat(true);
        reception.setEssaisEffectues(true);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));
        when(essaiRepository.existsByReceptionTravauxIdAndConformeIsFalse("reception-001")).thenReturn(true);

        assertThatThrownBy(() -> service.validerReception("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("essais ne sont pas conformes");
    }

    @Test
    void validerReception_Doit_Reussir_EtCreerHistoriqueEtNotification() {
        reception.setTravauxConformes(true);
        reception.setInstallationRemiseEnEtat(true);
        reception.setEssaisEffectues(true);

        ReceptionTravaux saved = ReceptionTravaux.builder()
                .id("reception-001")
                .autorisationTravail(atValidee)
                .travauxConformes(true)
                .installationRemiseEnEtat(true)
                .essaisEffectues(true)
                .essaisConformes(true)
                .validee(true)
                .essais(new ArrayList<>())
                .build();

        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));
        when(essaiRepository.existsByReceptionTravauxIdAndConformeIsFalse("reception-001")).thenReturn(false);
        when(receptionRepository.save(any())).thenReturn(saved);
        when(receptionMapper.toResponse(any())).thenReturn(response);

        ReceptionTravauxResponse result = service.validerReception("reception-001");

        assertThat(result).isNotNull();

        // Historique VALIDATION_RECEPTION créé
        verify(historiqueRepository, times(1)).save(argThat(h ->
                h instanceof HistoriqueAT && ((HistoriqueAT) h).getAction().name().equals("VALIDATION_RECEPTION")));

        // Notifications
        verify(notificationService, atLeastOnce()).sendNotificationToRole(eq("RESPONSABLE_OCP"), any(), any(), any(), any());

        // Audit
        verify(auditService, times(1)).logAction(eq("VALIDATION_RECEPTION"), eq("SUCCES"), any(), any(), any());
    }

    @Test
    void validerReception_Doit_LancerException_SiDejaValidee() {
        reception.setValidee(true);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.validerReception("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà validée");
    }

    // ===================================================================
    // ESSAIS
    // ===================================================================

    @Test
    void ajouterEssai_Doit_Reussir() {
        reception.setValidee(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        Essai essai = Essai.builder().id("essai-001").nom("Test pression").conforme(true).build();
        EssaiRequest req = new EssaiRequest();
        req.setNom("Test pression");
        req.setConforme(true);

        EssaiResponse essaiResp = new EssaiResponse();
        essaiResp.setId("essai-001");
        essaiResp.setNom("Test pression");

        when(essaiMapper.toEntity(any())).thenReturn(essai);
        when(essaiRepository.save(any())).thenReturn(essai);
        when(essaiMapper.toResponse(any())).thenReturn(essaiResp);

        EssaiResponse result = service.ajouterEssai("reception-001", req);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Test pression");
    }

    @Test
    void ajouterEssai_Doit_LancerException_SiReceptionValidee() {
        reception.setValidee(true);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.ajouterEssai("reception-001", new EssaiRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà validée");
    }

    // ===================================================================
    // GET
    // ===================================================================

    @Test
    void getById_Doit_LancerException_SiInexistant() {
        when(receptionRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("inexistant"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByAutorisationTravailId_Doit_LancerException_SiAucuneReception() {
        when(receptionRepository.findByAutorisationTravailId("at-sans-reception")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByAutorisationTravailId("at-sans-reception"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ===================================================================
    // DELETE
    // ===================================================================

    @Test
    void delete_Doit_LancerException_SiReceptionValidee() {
        reception.setValidee(true);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.delete("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("supprimer une réception déjà validée");
    }
}
