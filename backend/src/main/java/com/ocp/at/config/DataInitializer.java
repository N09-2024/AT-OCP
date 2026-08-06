package com.ocp.at.config;

import com.ocp.at.entity.Permission;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.repository.PermissionRepository;
import com.ocp.at.repository.RoleRepository;
import com.ocp.at.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
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
            // Nouvelles permissions granulaires — Standard S-HSE-SEC-31
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
                logger.info("Permission créée: {}", perm[0]);
            }
        }
    }

    private void initRoles() {
        List<String[]> roles = Arrays.asList(
            new String[]{"ADMIN", "Administrateur système"},
            new String[]{"CE", "Chef d'Équipe — position CEEP ou CEEE selon le territoire de l'AT"},
            new String[]{"HM", "Haute Maîtrise — position HMEP ou HMEE selon le territoire de l'AT"},
            new String[]{"HC", "Hors Cadre — position HCEP ou HCEE selon le territoire de l'AT"},
            new String[]{"RESPONSABLE_EXTERIEUR", "Responsable Entreprise Extérieure (BT + permis uniquement)"}
        );

        for (String[] roleData : roles) {
            if (!roleRepository.existsByNom(roleData[0])) {
                Set<Permission> perms = new HashSet<>();

                switch (roleData[0]) {
                    case "ADMIN":
                        perms.addAll(permissionRepository.findAll());
                        break;

                    case "CE":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "CREATE_AT", "EDIT_AT", "SUBMIT_AT", "READ_AT",
                            "CREATE_VISITE", "SIGN_AT", "CLOSE_AT", "RECEIVE_AT",
                            "START_INTERVENTION", "DECLARE_FIN_TRAVAUX", "RENEW_AT",
                            "VIEW_PERMIS", "EDIT_PERMIS", "UPLOAD_FILES", "EXPORT_PDF",
                            "RECEIVE_NOTIFICATION", "TRANSFER_AT"
                        )).forEach(perms::add);
                        break;

                    case "HM":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "VALIDATE_VISITE", "SIGN_AT", "START_INTERVENTION",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION", "VIEW_PERMIS"
                        )).forEach(perms::add);
                        break;

                    case "HC":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "CLASSIFY_INTERVENTION", "VALIDATE_AT", "REJECT_AT",
                            "VALIDATE_VISITE", "SIGN_AT", "ARCHIVE_AT", "VIEW_ARCHIVE",
                            "MANAGE_HABILITATIONS", "MANAGE_REFERENTIALS", "VIEW_AUDIT",
                            "VIEW_PERMIS", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "RESPONSABLE_EXTERIEUR":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "VIEW_PERMIS", "EDIT_PERMIS", "UPLOAD_PERMIS",
                            "UPLOAD_FILES", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;
                }

                roleRepository.save(Role.builder()
                        .nom(roleData[0]).description(roleData[1]).permissions(perms).build());
                logger.info("Rôle créé: {}", roleData[0]);
            } else {
                // Synchroniser les permissions des rôles existants (corrige gaps post-V28)
                Role existing = roleRepository.findByNom(roleData[0]).orElse(null);
                if (existing == null) {
                    continue;
                }
                Set<Permission> perms = new HashSet<>();
                switch (roleData[0]) {
                    case "ADMIN":
                        perms.addAll(permissionRepository.findAll());
                        break;
                    case "CE":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "CREATE_AT", "EDIT_AT", "SUBMIT_AT", "READ_AT",
                            "CREATE_VISITE", "SIGN_AT", "CLOSE_AT", "RECEIVE_AT",
                            "START_INTERVENTION", "DECLARE_FIN_TRAVAUX", "RENEW_AT",
                            "VIEW_PERMIS", "EDIT_PERMIS", "UPLOAD_FILES", "EXPORT_PDF",
                            "RECEIVE_NOTIFICATION", "TRANSFER_AT"
                        )).forEach(perms::add);
                        break;
                    case "HM":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "VALIDATE_VISITE", "SIGN_AT", "START_INTERVENTION",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION", "VIEW_PERMIS"
                        )).forEach(perms::add);
                        break;
                    case "HC":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "CLASSIFY_INTERVENTION", "VALIDATE_AT", "REJECT_AT",
                            "VALIDATE_VISITE", "SIGN_AT", "ARCHIVE_AT", "VIEW_ARCHIVE",
                            "MANAGE_HABILITATIONS", "MANAGE_REFERENTIALS", "VIEW_AUDIT",
                            "VIEW_PERMIS", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;
                    case "RESPONSABLE_EXTERIEUR":
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "VIEW_PERMIS", "EDIT_PERMIS", "UPLOAD_PERMIS",
                            "UPLOAD_FILES", "EXPORT_PDF", "RECEIVE_NOTIFICATION", "MANAGE_BT"
                        )).forEach(perms::add);
                        break;
                    default:
                        continue;
                }
                if (!perms.isEmpty()) {
                    existing.setPermissions(perms);
                    roleRepository.save(existing);
                    logger.info("Permissions du rôle {} synchronisées ({} perms)", roleData[0], perms.size());
                }
            }
        }
    }

    private void initAdminUser() {
        if (!utilisateurRepository.existsByEmail("admin@ocp.ma")) {
            Role adminRole = roleRepository.findByNom("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rôle ADMIN introuvable lors de l'initialisation"));

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
            logger.info("Utilisateur administrateur créé: admin@ocp.ma / Admin@123");
        }
    }
}
