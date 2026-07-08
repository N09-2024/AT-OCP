package com.ocp.at.service;

import com.ocp.at.dto.request.UtilisateurRequest;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.mapper.RoleMapper;
import com.ocp.at.mapper.UtilisateurMapper;
import com.ocp.at.repository.RoleRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.service.impl.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UtilisateurMapper utilisateurMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private UtilisateurRequest request;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        request = new UtilisateurRequest();
        request.setMatricule("OCP001");
        request.setNom("Dupont");
        request.setPrenom("Jean");
        request.setEmail("jean.dupont@ocp.ma");
        request.setMotDePasse("Test@1234");

        utilisateur = Utilisateur.builder()
                .id("uuid-1")
                .matricule("OCP001")
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@ocp.ma")
                .actif(true)
                .build();
    }

    @Test
    void creerUtilisateur_EmailExistant_DoitLeverBusinessException() {
        when(utilisateurRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> utilisateurService.creer(request));
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void creerUtilisateur_MatriculeExistant_DoitLeverBusinessException() {
        when(utilisateurRepository.existsByEmail(anyString())).thenReturn(false);
        when(utilisateurRepository.existsByMatricule(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> utilisateurService.creer(request));
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void creerUtilisateur_ValidesDonnees_DoitReussir() {
        when(utilisateurRepository.existsByEmail(anyString())).thenReturn(false);
        when(utilisateurRepository.existsByMatricule(anyString())).thenReturn(false);
        when(utilisateurMapper.toEntity(request)).thenReturn(utilisateur);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(utilisateurRepository.save(any())).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(new UtilisateurResponse());

        UtilisateurResponse result = utilisateurService.creer(request);

        assertNotNull(result);
        verify(utilisateurRepository, times(1)).save(any());
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void activerUtilisateur_DoitMettreActifATrue() {
        utilisateur.setActif(false);
        when(utilisateurRepository.findById("uuid-1")).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any())).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(any())).thenReturn(new UtilisateurResponse());

        utilisateurService.activer("uuid-1");

        assertTrue(utilisateur.isActif());
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void deverrouillerCompte_DoitReinitialiserCompteurEtVerrou() {
        utilisateur.setCompteVerrouille(true);
        utilisateur.setCompteurEchecsConnexion(5);
        when(utilisateurRepository.findById("uuid-1")).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any())).thenReturn(utilisateur);
        when(utilisateurMapper.toResponse(any())).thenReturn(new UtilisateurResponse());

        utilisateurService.deverrouiller("uuid-1");

        assertFalse(utilisateur.isCompteVerrouille());
        assertEquals(0, utilisateur.getCompteurEchecsConnexion());
    }
}
