package com.ocp.at.service;

import com.ocp.at.dto.request.PhotoReceptionRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.PhotoReceptionMapper;
import com.ocp.at.mapper.ReceptionTravauxMapper;
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
    @Mock private PhotoReceptionRepository photoRepository;
    @Mock private HistoriqueReceptionRepository historiqueReceptionRepository;
    @Mock private AutorisationTravailRepository atRepository;
    @Mock private HistoriqueATRepository historiqueRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private VisaRepository visaRepository;
    @Mock private PermisRepository permisRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;
    @Mock private ReceptionTravauxMapper receptionMapper;
    @Mock private PhotoReceptionMapper photoMapper;

    @InjectMocks
    private ReceptionTravauxServiceImpl service;

    private AutorisationTravail atValidee;
    private AutorisationTravail atBrouillon;
    private AutorisationTravail atCloturee;
    private ReceptionTravaux reception;
    private ReceptionTravauxRequest request;
    private ReceptionTravauxResponse response;
    private Utilisateur utilisateur;

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

        atCloturee = AutorisationTravail.builder()
                .id("at-cloturee-001")
                .numero("AT-2026-000003")
                .statut(StatutAT.CLOTUREE)
                .build();

        utilisateur = Utilisateur.builder()
                .id("user-001")
                .matricule("MAT001")
                .nom("Doe")
                .prenom("John")
                .build();

        reception = ReceptionTravaux.builder()
                .id("reception-001")
                .autorisationTravail(atValidee)
                .travauxConformes(false)
                .equipementRemisEnService(false)
                .zoneNettoyee(false)
                .consignationRetiree(false)
                .essaisEffectues(false)
                .photos(new ArrayList<>())
                .historiques(new ArrayList<>())
                .build();

        request = new ReceptionTravauxRequest();
        request.setAutorisationTravailId("at-validee-001");
        request.setTravauxRealises("Travaux terminés");

        response = new ReceptionTravauxResponse();
        response.setId("reception-001");
        response.setAutorisationTravailId("at-validee-001");

        // Stubs communs
        when(utilisateurRepository.findById(any())).thenReturn(Optional.empty());
        when(historiqueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(historiqueReceptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
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
    void create_Doit_LancerException_SiVisasNonValides() {
        when(atRepository.findById("at-validee-001")).thenReturn(Optional.of(atValidee));
        when(visaRepository.existsByAutorisationTravailIdAndStatut("at-validee-001", StatutVisa.VALIDE)).thenReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("visas doivent être validés");
    }

    @Test
    void create_Doit_LancerException_SiAucunPermis() {
        when(atRepository.findById("at-validee-001")).thenReturn(Optional.of(atValidee));
        when(visaRepository.existsByAutorisationTravailIdAndStatut("at-validee-001", StatutVisa.VALIDE)).thenReturn(true);
        when(permisRepository.existsByAutorisationTravailId("at-validee-001")).thenReturn(false);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permis est requis");
    }

    @Test
    void create_Doit_LancerException_SiReceptionDejaExistante() {
        when(atRepository.findById("at-validee-001")).thenReturn(Optional.of(atValidee));
        when(visaRepository.existsByAutorisationTravailIdAndStatut("at-validee-001", StatutVisa.VALIDE)).thenReturn(true);
        when(permisRepository.existsByAutorisationTravailId("at-validee-001")).thenReturn(true);
        when(receptionRepository.existsByAutorisationTravailId("at-validee-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void create_Doit_Reussir_EtCreerHistoriqueEtNotification() {
        when(atRepository.findById("at-validee-001")).thenReturn(Optional.of(atValidee));
        when(visaRepository.existsByAutorisationTravailIdAndStatut("at-validee-001", StatutVisa.VALIDE)).thenReturn(true);
        when(permisRepository.existsByAutorisationTravailId("at-validee-001")).thenReturn(true);
        when(receptionRepository.existsByAutorisationTravailId("at-validee-001")).thenReturn(false);
        when(receptionMapper.toEntity(any())).thenReturn(reception);
        when(receptionRepository.save(any())).thenReturn(reception);
        when(receptionMapper.toResponse(any())).thenReturn(response);

        ReceptionTravauxResponse result = service.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("reception-001");

        verify(historiqueReceptionRepository, times(1)).save(any());
        verify(historiqueRepository, times(1)).save(any());
        verify(notificationService, atLeastOnce()).sendNotificationToRole(eq("RESPONSABLE_OCP"), any(), any(), any(), any());
        verify(auditService, times(1)).logAction(eq("CREATION_RECEPTION"), eq("SUCCES"), any(), any(), any());
    }

    // ===================================================================
    // UPDATE
    // ===================================================================

    @Test
    void update_Doit_LancerException_SiATCloturee() {
        reception.setAutorisationTravail(atCloturee);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.update("reception-001", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("clôturée");
    }

    // ===================================================================
    // DELETE
    // ===================================================================

    @Test
    void delete_Doit_LancerException_SiATCloturee() {
        reception.setAutorisationTravail(atCloturee);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.delete("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("clôturée");
    }

    // ===================================================================
    // SIGNATURE
    // ===================================================================

    @Test
    void signer_Doit_LancerException_SiATCloturee() {
        reception.setAutorisationTravail(atCloturee);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.signer("reception-001", "/path/signature.png"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("clôturée");
    }

    @Test
    void signer_Doit_Reussir() {
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));
        when(receptionRepository.save(any())).thenReturn(reception);
        when(receptionMapper.toResponse(any())).thenReturn(response);

        ReceptionTravauxResponse result = service.signer("reception-001", "/path/signature.png");

        assertThat(result).isNotNull();
        assertThat(reception.getSignatureResponsable()).isEqualTo("/path/signature.png");
        assertThat(reception.getDateSignature()).isNotNull();

        verify(historiqueReceptionRepository, times(1)).save(any());
        verify(auditService, times(1)).logAction(eq("SIGNATURE_RECEPTION"), eq("SUCCES"), any(), any(), any());
    }

    // ===================================================================
    // CLOTURE AT
    // ===================================================================

    @Test
    void cloturerAT_Doit_LancerException_SiATDejaCloturee() {
        reception.setAutorisationTravail(atCloturee);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.cloturerAT("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà clôturée");
    }

    @Test
    void cloturerAT_Doit_LancerException_SiTravauxNonConformes() {
        reception.setTravauxConformes(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.cloturerAT("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("travaux ne sont pas conformes");
    }

    @Test
    void cloturerAT_Doit_LancerException_SiZoneNonNettoyee() {
        reception.setTravauxConformes(true);
        reception.setZoneNettoyee(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.cloturerAT("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("zone n'est pas nettoyée");
    }

    @Test
    void cloturerAT_Doit_LancerException_SiConsignationNonRetiree() {
        reception.setTravauxConformes(true);
        reception.setZoneNettoyee(true);
        reception.setConsignationRetiree(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.cloturerAT("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("consignation n'est pas retirée");
    }

    @Test
    void cloturerAT_Doit_LancerException_SiEquipementNonRemisEnService() {
        reception.setTravauxConformes(true);
        reception.setZoneNettoyee(true);
        reception.setConsignationRetiree(true);
        reception.setEquipementRemisEnService(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.cloturerAT("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("équipement n'est pas remis en service");
    }

    @Test
    void cloturerAT_Doit_LancerException_SiEssaisNonEffectues() {
        reception.setTravauxConformes(true);
        reception.setZoneNettoyee(true);
        reception.setConsignationRetiree(true);
        reception.setEquipementRemisEnService(true);
        reception.setEssaisEffectues(false);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.cloturerAT("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("essais n'ont pas été effectués");
    }

    @Test
    void cloturerAT_Doit_LancerException_SiSignatureAbsente() {
        reception.setTravauxConformes(true);
        reception.setZoneNettoyee(true);
        reception.setConsignationRetiree(true);
        reception.setEquipementRemisEnService(true);
        reception.setEssaisEffectues(true);
        reception.setSignatureResponsable(null);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.cloturerAT("reception-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("signature du responsable est obligatoire");
    }

    @Test
    void cloturerAT_Doit_Reussir_EtCloturerAT() {
        reception.setTravauxConformes(true);
        reception.setZoneNettoyee(true);
        reception.setConsignationRetiree(true);
        reception.setEquipementRemisEnService(true);
        reception.setEssaisEffectues(true);
        reception.setSignatureResponsable("/path/signature.png");

        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));
        when(atRepository.save(any())).thenReturn(atValidee);
        when(receptionMapper.toResponse(any())).thenReturn(response);

        ReceptionTravauxResponse result = service.cloturerAT("reception-001");

        assertThat(result).isNotNull();
        assertThat(atValidee.getStatut()).isEqualTo(StatutAT.CLOTUREE);

        verify(historiqueReceptionRepository, times(1)).save(any());
        verify(historiqueRepository, times(1)).save(any());
        verify(notificationService, atLeastOnce()).sendNotificationToRole(eq("RESPONSABLE_OCP"), any(), any(), any(), any());
        verify(auditService, times(1)).logAction(eq("CLOTURE_AT"), eq("SUCCES"), any(), any(), any());
    }

    // ===================================================================
    // PHOTOS
    // ===================================================================

    @Test
    void ajouterPhoto_Doit_LancerException_SiATCloturee() {
        reception.setAutorisationTravail(atCloturee);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        PhotoReceptionRequest photoReq = new PhotoReceptionRequest();
        photoReq.setNom("photo1.jpg");
        photoReq.setPath("/uploads/photo1.jpg");

        assertThatThrownBy(() -> service.ajouterPhoto("reception-001", photoReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("clôturée");
    }

    @Test
    void ajouterPhoto_Doit_Reussir() {
        PhotoReception photo = PhotoReception.builder()
                .id("photo-001")
                .nom("photo1.jpg")
                .path("/uploads/photo1.jpg")
                .build();

        PhotoReceptionRequest photoReq = new PhotoReceptionRequest();
        photoReq.setNom("photo1.jpg");
        photoReq.setPath("/uploads/photo1.jpg");

        PhotoReceptionResponse photoResp = new PhotoReceptionResponse();
        photoResp.setId("photo-001");
        photoResp.setNom("photo1.jpg");

        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));
        when(photoMapper.toEntity(any())).thenReturn(photo);
        when(photoRepository.save(any())).thenReturn(photo);
        when(photoMapper.toResponse(any())).thenReturn(photoResp);

        PhotoReceptionResponse result = service.ajouterPhoto("reception-001", photoReq);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("photo1.jpg");

        verify(historiqueReceptionRepository, times(1)).save(any());
        verify(auditService, times(1)).logAction(eq("AJOUT_PHOTO_RECEPTION"), eq("SUCCES"), any(), any(), any());
    }

    @Test
    void supprimerPhoto_Doit_LancerException_SiATCloturee() {
        reception.setAutorisationTravail(atCloturee);
        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));

        assertThatThrownBy(() -> service.supprimerPhoto("reception-001", "photo-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("clôturée");
    }

    @Test
    void getPhotos_Doit_RetournerListe() {
        PhotoReception photo1 = PhotoReception.builder().id("photo-001").nom("photo1.jpg").build();
        PhotoReception photo2 = PhotoReception.builder().id("photo-002").nom("photo2.jpg").build();

        PhotoReceptionResponse resp1 = new PhotoReceptionResponse();
        resp1.setId("photo-001");
        resp1.setNom("photo1.jpg");

        PhotoReceptionResponse resp2 = new PhotoReceptionResponse();
        resp2.setId("photo-002");
        resp2.setNom("photo2.jpg");

        when(receptionRepository.findById("reception-001")).thenReturn(Optional.of(reception));
        when(photoRepository.findByReceptionTravauxIdOrderByOrdreAsc("reception-001"))
                .thenReturn(List.of(photo1, photo2));
        when(photoMapper.toResponse(photo1)).thenReturn(resp1);
        when(photoMapper.toResponse(photo2)).thenReturn(resp2);

        List<PhotoReceptionResponse> result = service.getPhotos("reception-001");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNom()).isEqualTo("photo1.jpg");
        assertThat(result.get(1).getNom()).isEqualTo("photo2.jpg");
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
}
