package com.ocp.at.config;

import com.ocp.at.entity.Permission;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.repository.PermissionRepository;
import com.ocp.at.repository.RoleRepository;
import com.ocp.at.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Initialise les données minimales nécessaires pour les tests d'intégration.
 * Actif uniquement avec le profil "test".
 */
@Component
@Profile("test")
@RequiredArgsConstructor
@Slf4j
public class TestDataInitializer {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initTestData() {
        initPermissions();
        initRoles();
        initAdminUser();
    }

    private void initPermissions() {
        List<String[]> permissions = Arrays.asList(
            // ============================================================
            // Permissions existantes
            // ============================================================
            new String[]{"READ_AT",              "Consulter les autorisations de travail"},
            new String[]{"CREATE_AT",             "Créer une autorisation de travail (CEEP §8.1)"},
            new String[]{"EDIT_AT",               "Modifier une autorisation de travail"},
            new String[]{"SUBMIT_AT",             "Soumettre une autorisation de travail"},
            new String[]{"VALIDATE_AT",           "Valider une autorisation de travail (HCEE Garant §8.3)"},
            new String[]{"REJECT_AT",             "Rejeter une autorisation de travail"},
            new String[]{"CLOSE_AT",              "Clôturer une autorisation de travail"},
            new String[]{"MANAGE_USERS",          "Gérer les utilisateurs"},
            new String[]{"MANAGE_ROLES",          "Gérer les rôles et permissions"},
            new String[]{"MANAGE_REFERENTIALS",   "Gérer les référentiels"},
            new String[]{"VIEW_AUDIT",            "Consulter les logs d'audit"},
            new String[]{"EXPORT_PDF",            "Exporter une AT en PDF"},
            new String[]{"UPLOAD_FILES",          "Uploader des fichiers"},
            new String[]{"VIEW_PERMIS",           "Consulter les permis"},
            new String[]{"CREATE_PERMIS",         "Créer un permis"},
            new String[]{"EDIT_PERMIS",           "Modifier un permis"},
            new String[]{"DELETE_PERMIS",         "Supprimer un permis"},
            new String[]{"UPLOAD_PERMIS",         "Uploader un fichier de permis"},
            new String[]{"ANALYSE_PERMIS",        "Analyser un permis avec l'IA"},
            new String[]{"MANAGE_DOCUMENTS",      "Gérer les documents source et visites (legacy)"},

            // ============================================================
            // Nouvelles permissions granulaires - Standard S-HSE-SEC-31
            // ============================================================
            new String[]{"CLASSIFY_INTERVENTION", "Classifier une intervention Niveau 1/2 (HCEP Étape 0)"},
            new String[]{"CREATE_VISITE",         "Créer et réaliser une visite préalable du chantier (CEEP §8.2)"},
            new String[]{"VALIDATE_VISITE",       "Valider/garantir une visite préalable (HCEE G, HMEP G §8.2)"},
            new String[]{"SIGN_AT",               "Signer ou viser une AT ou un permis (§8.3, §8.4)"},
            new String[]{"START_INTERVENTION",    "Démarrer une intervention (CEEE E §4)"},
            new String[]{"DECLARE_FIN_TRAVAUX",   "Déclarer la fin des travaux (CEEE E §8.5)"},
            new String[]{"RECEIVE_AT",            "Réceptionner les travaux (CEEP E §8.5)"},
            new String[]{"RENEW_AT",              "Reconduire/renouveler une AT (CEEP §8.4 dépassement poste)"},
            new String[]{"RECEIVE_NOTIFICATION",  "Recevoir des notifications système liées aux AT"},
            new String[]{"ARCHIVE_AT",            "Archiver officiellement une AT (HCEE E §8.6)"},
            new String[]{"VIEW_ARCHIVE",          "Consulter et télécharger les archives AT (HCEP G, HCEE E §8.6)"},
            new String[]{"MANAGE_HABILITATIONS",  "Désigner et gérer les agents habilités AT (HCEP §9)"},
            new String[]{"TRANSFER_AT",           "Transférer le verrou d'édition d'une AT"}
        );

        for (String[] perm : permissions) {
            if (!permissionRepository.existsByNom(perm[0])) {
                permissionRepository.save(Permission.builder()
                        .nom(perm[0]).description(perm[1]).build());
            }
        }
    }

    private void initRoles() {
        List<String[]> roles = Arrays.asList(
            new String[]{"ADMIN", "Administrateur système"},
            new String[]{"CEEP", "Chef d'Équipe de l'Entité Propriétaire"},
            new String[]{"CEEE", "Chef d'Équipe de l'Entité Exécutante"},
            new String[]{"HCEP", "Hors Cadre Responsable de l'Entité Propriétaire"},
            new String[]{"HCEE", "Hors Cadre Responsable de l'Entité Exécutante"},
            new String[]{"HMEP", "Haute Maîtrise de l'Entité Propriétaire"},
            new String[]{"HMEE", "Haute Maîtrise de l'Entité Exécutante"},
            new String[]{"RESPONSABLE_ENTREPRISE", "Responsable d'entreprise externe"}
        );

        for (String[] roleData : roles) {
            if (!roleRepository.existsByNom(roleData[0])) {
                Set<Permission> perms = new HashSet<>();

                switch (roleData[0]) {
                    case "ADMIN":
                        perms.addAll(permissionRepository.findAll());
                        break;

                    case "CEEP":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "CREATE_AT", "EDIT_AT", "SUBMIT_AT",
                            "SIGN_AT", "RENEW_AT", "RECEIVE_AT", "CLOSE_AT",
                            "CREATE_VISITE", "TRANSFER_AT", "UPLOAD_FILES", "EXPORT_PDF",
                            "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "CEEE":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "EDIT_AT", "SIGN_AT", "START_INTERVENTION",
                            "DECLARE_FIN_TRAVAUX", "VIEW_PERMIS", "EDIT_PERMIS",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HCEP":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "CLASSIFY_INTERVENTION", "MANAGE_HABILITATIONS",
                            "VIEW_ARCHIVE", "MANAGE_REFERENTIALS", "VIEW_AUDIT",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HCEE":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "VALIDATE_AT", "REJECT_AT", "SIGN_AT",
                            "VALIDATE_VISITE", "ARCHIVE_AT", "VIEW_ARCHIVE",
                            "VIEW_PERMIS", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HMEP":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "VALIDATE_VISITE", "SIGN_AT",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HMEE":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "RESPONSABLE_ENTREPRISE":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "VIEW_PERMIS", "UPLOAD_PERMIS", "ANALYSE_PERMIS", "CREATE_PERMIS",
                            "READ_AT", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;
                }

                roleRepository.save(Role.builder()
                        .nom(roleData[0]).description(roleData[1]).permissions(perms).build());
                log.info("Rôle test créé: {}", roleData[0]);
            } else if ("ADMIN".equals(roleData[0])) {
                // Toujours s'assurer que l'ADMIN a toutes les permissions
                Role adminRole = roleRepository.findByNom("ADMIN").get();
                adminRole.setPermissions(new HashSet<>(permissionRepository.findAll()));
                roleRepository.save(adminRole);
                log.info("Permissions du rôle ADMIN synchronisées (TestDataInitializer)");
            }
        }
    }

    private void initAdminUser() {
        if (!utilisateurRepository.existsByEmail("admin@ocp.ma")) {
            Role adminRole = roleRepository.findByNom("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rôle ADMIN introuvable lors de l'initialisation test"));

            Utilisateur admin = Utilisateur.builder()
                    .matricule("ADMIN001")
                    .nom("Administrateur")
                    .prenom("Système")
                    .email("admin@ocp.ma")
                    .motDePasse(passwordEncoder.encode("Admin@123"))
                    .actif(true)
                    .roles(Set.of(adminRole))
                    .build();

            utilisateurRepository.save(admin);
        }
    }
}
