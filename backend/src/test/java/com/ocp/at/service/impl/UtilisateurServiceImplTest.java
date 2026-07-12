package com.ocp.at.service.impl;

import com.ocp.at.dto.request.UtilisateurRequest;
import com.ocp.at.dto.response.UtilisateurResponse;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.UtilisateurMapper;
import com.ocp.at.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests UtilisateurServiceImpl")
class UtilisateurServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    private Utilisateur utilisateur;
    private UtilisateurRequest utilisateurRequest;
    private UtilisateurResponse utilisateurResponse;

    @BeforeEach
    void setUp() {
        Role role1 = Role.builder()
                .id("role-001")
                .nom("ROLE_RESPONSABLE")
                .description("Responsable AT")
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(role1);

        utilisateur = Utilisateur.builder()
                .id("user-001")
                .matricule("EMP001")
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@ocp.ma")
                .motDePasse("encodedPassword")
                .actif(true)
                .roles(roles)
                .dateCreation(LocalDateTime.now())
                .build();

        utilisateurRequest = new UtilisateurRequest();
        utilisateurRequest.setMatricule("EMP001");
        utilisateurRequest.setNom("Dupont");
        utilisateurRequest.setPrenom("Jean");
        utilisateurRequest.setEmail("jean.dupont@ocp.ma");

        utilisateurResponse = UtilisateurResponse.builder()
                .id("user-001")
                .matricule("EMP001")
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont@ocp.ma")
                .build();
    }

    @Nested
    @DisplayName("creer")
    class CreerTests {

        @Test
        @DisplayName("doit créer un utilisateur")
        void doitCreerUtilisateur() {
            when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);
            when(utilisateurMapper.toEntity(any(UtilisateurRequest.class))).thenReturn(utilisateur);
            when(utilisateurMapper.toResponse(any(Utilisateur.class))).thenReturn(utilisateurResponse);

            UtilisateurResponse result = utilisateurService.creer(utilisateurRequest);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("jean.dupont@ocp.ma");
            verify(utilisateurRepository).save(any(Utilisateur.class));
        }
    }

    @Nested
    @DisplayName("trouverParId")
    class TrouverParIdTests {

        @Test
        @DisplayName("doit retourner l'utilisateur par ID")
        void doitRetournerUtilisateurParId() {
            when(utilisateurRepository.findById("user-001")).thenReturn(Optional.of(utilisateur));
            when(utilisateurMapper.toResponse(utilisateur)).thenReturn(utilisateurResponse);

            UtilisateurResponse result = utilisateurService.trouverParId("user-001");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-001");
            assertThat(result.getEmail()).isEqualTo("jean.dupont@ocp.ma");
        }

        @Test
        @DisplayName("doit lever une exception si l'utilisateur n'existe pas")
        void doitLeverExceptionSiUtilisateurInexistant() {
            when(utilisateurRepository.findById(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> 
                utilisateurService.trouverParId("unknown-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("modifier")
    class ModifierTests {

        @Test
        @DisplayName("doit modifier l'utilisateur")
        void doitModifierUtilisateur() {
            when(utilisateurRepository.findById("user-001")).thenReturn(Optional.of(utilisateur));
            when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);
            when(utilisateurMapper.toResponse(any(Utilisateur.class))).thenReturn(utilisateurResponse);

            UtilisateurResponse result = utilisateurService.modifier("user-001", null);

            assertThat(result).isNotNull();
            verify(utilisateurRepository).save(any(Utilisateur.class));
        }
    }

    @Nested
    @DisplayName("activer")
    class ActiverTests {

        @Test
        @DisplayName("doit activer l'utilisateur")
        void doitActiverUtilisateur() {
            utilisateur.setActif(false);
            when(utilisateurRepository.findById("user-001")).thenReturn(Optional.of(utilisateur));
            when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);
            when(utilisateurMapper.toResponse(any(Utilisateur.class))).thenReturn(utilisateurResponse);

            utilisateurService.activer("user-001");

            verify(utilisateurRepository).save(argThat(u -> u.isActif()));
        }
    }

    @Nested
    @DisplayName("desactiver")
    class DesactiverTests {

        @Test
        @DisplayName("doit désactiver l'utilisateur")
        void doitDesactiverUtilisateur() {
            when(utilisateurRepository.findById("user-001")).thenReturn(Optional.of(utilisateur));
            when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);
            when(utilisateurMapper.toResponse(any(Utilisateur.class))).thenReturn(utilisateurResponse);

            utilisateurService.desactiver("user-001");

            verify(utilisateurRepository).save(argThat(u -> !u.isActif()));
        }
    }
}
