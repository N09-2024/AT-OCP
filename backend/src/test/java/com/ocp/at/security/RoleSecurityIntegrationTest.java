package com.ocp.at.security;

import com.ocp.at.config.TestDataInitializer;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.repository.RoleRepository;
import com.ocp.at.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration de la sécurité par rôle et des permissions
 * conformes au Standard OCP S-HSE-SEC-31 v1.0.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // S'assurer que les utilisateurs de test existent pour chaque rôle
        createTestUserIfNotExists("ceep@ocp.ma", "CEEP");
        createTestUserIfNotExists("ceee@ocp.ma", "CEEE");
        createTestUserIfNotExists("hcep@ocp.ma", "HCEP");
        createTestUserIfNotExists("hcee@ocp.ma", "HCEE");
        createTestUserIfNotExists("hmep@ocp.ma", "HMEP");
        createTestUserIfNotExists("hmee@ocp.ma", "HMEE");
        createTestUserIfNotExists("prestataire@externe.ma", "RESPONSABLE_ENTREPRISE");
    }

    private void createTestUserIfNotExists(String email, String roleNom) {
        if (!utilisateurRepository.existsByEmail(email)) {
            Role role = roleRepository.findByNom(roleNom)
                    .orElseThrow(() -> new IllegalStateException("Rôle introuvable : " + roleNom));
            Utilisateur user = Utilisateur.builder()
                    .matricule("MAT-" + roleNom)
                    .nom("Nom " + roleNom)
                    .prenom("Prenom " + roleNom)
                    .email(email)
                    .motDePasse(passwordEncoder.encode("Test@123"))
                    .actif(true)
                    .roles(Set.of(role))
                    .build();
            utilisateurRepository.save(user);
        }
    }

    // =========================================================================
    // Test 1 : Non-authentifié -> 401 Unauthorized
    // =========================================================================
    @Test
    @DisplayName("Test 1: Accès anonyme sur route protégée doit retourner 401")
    void unauthenticatedAccess_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/autorisations-travail"))
                .andExpect(status().isUnauthorized());
    }

    // =========================================================================
    // Test 2 : HMEE (Fail-closed - lecture seule)
    // =========================================================================
    @Nested
    @DisplayName("Tests du rôle HMEE (Fail-closed)")
    class HmeeTests {

        @Test
        @WithMockUser(username = "hmee@ocp.ma", authorities = {"READ_AT", "EXPORT_PDF", "RECEIVE_NOTIFICATION"})
        @DisplayName("Test 2a: HMEE peut lire la liste des AT")
        void hmee_canReadATList() throws Exception {
            mockMvc.perform(get("/api/autorisations-travail"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "hmee@ocp.ma", authorities = {"READ_AT", "EXPORT_PDF", "RECEIVE_NOTIFICATION"})
        @DisplayName("Test 2b: HMEE ne peut pas créer une AT (403 Forbidden)")
        void hmee_cannotCreateAT() throws Exception {
            mockMvc.perform(post("/api/autorisations-travail"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "hmee@ocp.ma", authorities = {"READ_AT", "EXPORT_PDF", "RECEIVE_NOTIFICATION"})
        @DisplayName("Test 2c: HMEE ne peut pas valider une visite préalable (403 Forbidden)")
        void hmee_cannotValidateVisite() throws Exception {
            mockMvc.perform(put("/api/visites-prealables/test-id/finaliser"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Test 3 : CEEP (Création AT & Visite)
    // =========================================================================
    @Nested
    @DisplayName("Tests du rôle CEEP (Chef d'Équipe Entité Propriétaire)")
    class CeepTests {

        @Test
        @WithMockUser(username = "ceep@ocp.ma", authorities = {"READ_AT", "CREATE_AT", "CREATE_VISITE"})
        @DisplayName("Test 3: CEEP a l'autorité de créer une AT")
        void ceep_canCreateAT() throws Exception {
            mockMvc.perform(post("/api/autorisations-travail"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "ceep@ocp.ma", authorities = {"READ_AT", "CREATE_VISITE"})
        @DisplayName("Test 3b: CEEP a l'autorité de créer une visite préalable")
        void ceep_canCreateVisite() throws Exception {
            String visiteJson = "{\"documentId\":\"di-123\",\"typeDocument\":\"DI\",\"commentaire\":\"Visite terrain CEEP\"}";
            mockMvc.perform(post("/api/visites-prealables")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(visiteJson))
                    .andExpect(status().isCreated());
        }
    }

    // =========================================================================
    // Test 4 : HCEE (Validation Visite & Archivage)
    // =========================================================================
    @Nested
    @DisplayName("Tests du rôle HCEE (Hors Cadre Entité Exécutante)")
    class HceeTests {

        @Test
        @WithMockUser(username = "hcee@ocp.ma", authorities = {"READ_AT", "VALIDATE_VISITE", "ARCHIVE_AT"})
        @DisplayName("Test 4a: HCEE peut valider/finaliser une visite préalable")
        void hcee_canValidateVisite() throws Exception {
            // Note: service test MockMvc verifiera uniquement la couche securite @PreAuthorize
            mockMvc.perform(put("/api/visites-prealables/non-existant/finaliser"))
                    .andExpect(status().isNotFound()); // 404 prouve que la securite 403 est passee
        }

        @Test
        @WithMockUser(username = "hcee@ocp.ma", authorities = {"READ_AT", "ARCHIVE_AT"})
        @DisplayName("Test 4b: HCEE a l'autorité d'archiver officiellement (§8.6)")
        void hcee_canArchiveAT() throws Exception {
            mockMvc.perform(post("/api/archives/archive/non-existant"))
                    .andExpect(status().isNotFound()); // 404 prouve que 403 est passee
        }
    }

    // =========================================================================
    // Test 5 : HCEP (Classification & Supervision Archivage)
    // =========================================================================
    @Nested
    @DisplayName("Tests du rôle HCEP (Hors Cadre Entité Propriétaire)")
    class HcepTests {

        @Test
        @WithMockUser(username = "hcep@ocp.ma", authorities = {"READ_AT", "CLASSIFY_INTERVENTION", "VIEW_ARCHIVE"})
        @DisplayName("Test 5a: HCEP peut classifier une intervention en Niveau 2 (Étape 0)")
        void hcep_canClassifyIntervention() throws Exception {
            mockMvc.perform(post("/api/documents/DI/di-123/classifier?niveau=2"))
                    .andExpect(status().isNotFound()); // 404 prouve que 403 est passee
        }

        @Test
        @WithMockUser(username = "hcep@ocp.ma", authorities = {"READ_AT", "VIEW_ARCHIVE"})
        @DisplayName("Test 5b: HCEP ne peut PAS archiver directement (Garant §8.6 - pas Exécutant -> 403)")
        void hcep_cannotArchiveDirectly() throws Exception {
            mockMvc.perform(post("/api/archives/archive/at-123"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "hcep@ocp.ma", authorities = {"READ_AT", "VIEW_ARCHIVE"})
        @DisplayName("Test 5c: HCEP peut consulter les archives (Garant §8.6)")
        void hcep_canViewArchives() throws Exception {
            mockMvc.perform(get("/api/archives"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Test 6 : CEEE (Démarrage & Déclaration Fin)
    // =========================================================================
    @Nested
    @DisplayName("Tests du rôle CEEE (Chef d'Équipe Entité Exécutante)")
    class CeeeTests {

        @Test
        @WithMockUser(username = "ceee@ocp.ma", authorities = {"READ_AT", "START_INTERVENTION", "DECLARE_FIN_TRAVAUX"})
        @DisplayName("Test 6a: CEEE a l'autorité de démarrer l'intervention (Étape 4)")
        void ceee_canStartIntervention() throws Exception {
            mockMvc.perform(post("/api/autorisations-travail/test-id/demarrer-intervention"))
                    .andExpect(status().isNotFound()); // 404 prouve que 403 est passee
        }

        @Test
        @WithMockUser(username = "ceee@ocp.ma", authorities = {"READ_AT", "START_INTERVENTION", "DECLARE_FIN_TRAVAUX"})
        @DisplayName("Test 6b: CEEE a l'autorité de déclarer la fin des travaux (Étape 6)")
        void ceee_canDeclareFinTravaux() throws Exception {
            mockMvc.perform(post("/api/autorisations-travail/test-id/declarer-fin"))
                    .andExpect(status().isNotFound()); // 404 prouve que 403 est passee
        }
    }

    // =========================================================================
    // Test 7 : RESPONSABLE_ENTREPRISE (Sous-traitant - Permis uniquement)
    // =========================================================================
    @Nested
    @DisplayName("Tests du rôle RESPONSABLE_ENTREPRISE")
    class ResponsableEntrepriseTests {

        @Test
        @WithMockUser(username = "prestataire@externe.ma", authorities = {"READ_AT", "VIEW_PERMIS", "CREATE_PERMIS", "UPLOAD_PERMIS"})
        @DisplayName("Test 7a: Responsable entreprise peut consulter les permis")
        void entreprise_canViewPermis() throws Exception {
            mockMvc.perform(get("/api/permis"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "prestataire@externe.ma", authorities = {"READ_AT", "VIEW_PERMIS", "CREATE_PERMIS"})
        @DisplayName("Test 7b: Responsable entreprise ne peut pas créer une AT (403 Forbidden)")
        void entreprise_cannotCreateAT() throws Exception {
            mockMvc.perform(post("/api/autorisations-travail"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Test 8 : MANAGE_DOCUMENTS obsolète
    // =========================================================================
    @Test
    @WithMockUser(username = "legacy@ocp.ma", authorities = {"MANAGE_DOCUMENTS"})
    @DisplayName("Test 8: Un utilisateur n'ayant que MANAGE_DOCUMENTS ne peut plus créer de visite (403)")
    void legacyManageDocuments_cannotCreateVisite() throws Exception {
        String visiteJson = "{\"documentId\":\"di-123\",\"typeDocument\":\"DI\"}";
        mockMvc.perform(post("/api/visites-prealables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(visiteJson))
                .andExpect(status().isForbidden());
    }
}
