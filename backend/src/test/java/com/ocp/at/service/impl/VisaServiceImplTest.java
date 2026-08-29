package com.ocp.at.service.impl;

import com.ocp.at.dto.response.VisaResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.Visa;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.mapper.VisaMapper;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.repository.VisaRepository;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.AuditService;
import com.ocp.at.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisaServiceImplTest {

    @Mock
    private VisaRepository visaRepository;
    @Mock
    private AutorisationTravailRepository atRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private VisaMapper visaMapper;
    @Mock
    private StorageService storageService;
    @Mock
    private AuditService auditService;
    @Mock
    private com.ocp.at.service.NotificationService notificationService;

    @InjectMocks
    private VisaServiceImpl visaService;

    private Visa visa;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "TestBrowser");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Role ceepRole = Role.builder().nom("CEEP").permissions(Set.of()).build();
        utilisateur = new Utilisateur();
        utilisateur.setId("user-123");
        utilisateur.setEmail("user123@ocp.ma");
        utilisateur.setNom("Doe");
        utilisateur.setRoles(Set.of(ceepRole));

        AutorisationTravail at = new AutorisationTravail();
        at.setId("at-123");
        at.setNumero("AT-2026-000001");

        visa = new Visa();
        visa.setId("visa-123");
        visa.setUtilisateur(utilisateur);
        visa.setAutorisationTravail(at);
        visa.setStatut(StatutVisa.EN_ATTENTE);
        visa.setCommentaire("Visa CEEP");
    }

    @Test
    void signVisa_Success() {
        try (org.mockito.MockedStatic<SecurityUtils> utilities = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            when(visaRepository.findById("visa-123")).thenReturn(Optional.of(visa));
            utilities.when(SecurityUtils::getCurrentUtilisateurId).thenReturn(Optional.of("user-123"));
            when(utilisateurRepository.findById("user-123")).thenReturn(Optional.of(utilisateur));
            when(storageService.saveSignatureBytes(any(), any())).thenReturn("signatures/test.png");
            when(visaRepository.save(any(Visa.class))).thenReturn(visa);
            
            VisaResponse mockResponse = new VisaResponse();
            mockResponse.setStatut(StatutVisa.VALIDE);
            when(visaMapper.toResponse(any())).thenReturn(mockResponse);
    
            MockMultipartFile file = new MockMultipartFile("signature", "sign.png", "image/png", "dummy-image-data".getBytes());
            
            VisaResponse response = visaService.signVisa("visa-123", file, "Lu et approuvé");
    
            assertNotNull(response);
            assertEquals(StatutVisa.VALIDE, response.getStatut());
            verify(auditService).logAction(eq("SIGN_VISA"), eq("SUCCES"), any(), anyString(), anyString());
        }
    }

    @Test
    void signVisa_WrongUser_ThrowsException() {
        try (org.mockito.MockedStatic<SecurityUtils> utilities = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            Utilisateur otherUser = new Utilisateur();
            otherUser.setId("other-user");
            otherUser.setEmail("other@ocp.ma");
            otherUser.setRoles(Set.of());

            when(visaRepository.findById("visa-123")).thenReturn(Optional.of(visa));
            utilities.when(SecurityUtils::getCurrentUtilisateurId).thenReturn(Optional.of("other-user"));
            when(utilisateurRepository.findById("other-user")).thenReturn(Optional.of(otherUser));
    
            MockMultipartFile file = new MockMultipartFile("signature", "sign.png", "image/png", "dummy-image-data".getBytes());
            
            assertThrows(BusinessException.class, () -> visaService.signVisa("visa-123", file, null));
        }
    }

    @Test
    void signVisa_WrongStatus_ThrowsException() {
        try (org.mockito.MockedStatic<SecurityUtils> utilities = org.mockito.Mockito.mockStatic(SecurityUtils.class)) {
            visa.setStatut(StatutVisa.VALIDE);
            when(visaRepository.findById("visa-123")).thenReturn(Optional.of(visa));
            utilities.when(SecurityUtils::getCurrentUtilisateurId).thenReturn(Optional.of("user-123"));
            when(utilisateurRepository.findById("user-123")).thenReturn(Optional.of(utilisateur));
    
            MockMultipartFile file = new MockMultipartFile("signature", "sign.png", "image/png", "dummy-image-data".getBytes());
            
            assertThrows(BusinessException.class, () -> visaService.signVisa("visa-123", file, null));
        }
    }
}
