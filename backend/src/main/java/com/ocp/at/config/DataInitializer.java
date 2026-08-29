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

    private final com.ocp.at.repository.ServiceRepository serviceRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        initPermissions();
        initRoles();
        initDefaultUsers();
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
            new String[]{"VALIDATE_AT",           "Valider une autorisation de travail"},
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
            new String[]{"TRANSFER_AT",           "Transférer le verrou d'édition d'une AT"},
            new String[]{"VIEW_RECEPTION",        "Consulter les réceptions des travaux"},
            new String[]{"CREATE_RECEPTION",      "Créer une réception des travaux"},
            new String[]{"EDIT_RECEPTION",        "Modifier une réception des travaux"},
            new String[]{"SIGN_RECEPTION",        "Signer une réception des travaux"},
            new String[]{"DELETE_RECEPTION",      "Supprimer une réception des travaux"}
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
            new String[]{"CEEP", "Chef d'Équipe de l'Entité Propriétaire"},
            new String[]{"CEEE", "Chef d'Équipe de l'Entité Exécutante"},
            new String[]{"HCEP", "Hors Cadre Responsable de l'Entité Propriétaire"},
            new String[]{"HCEE", "Hors Cadre Responsable de l'Entité Exécutante"},
            new String[]{"HMEP", "Haute Maîtrise de l'Entité Propriétaire"},
            new String[]{"HMEE", "Haute Maîtrise de l'Entité Exécutante"},
            new String[]{"CE", "Chef d'Équipe - position CEEP ou CEEE selon le territoire de l'AT"},
            new String[]{"HM", "Haute Maîtrise - position HMEP ou HMEE selon le territoire de l'AT"},
            new String[]{"HC", "Hors Cadre - position HCEP ou HCEE selon le territoire de l'AT"},
            new String[]{"RESPONSABLE_EXTERIEUR", "Responsable Entreprise Extérieure (BT + permis uniquement)"}
        );

        for (String[] roleData : roles) {
            Role role = roleRepository.findByNom(roleData[0]).orElse(null);
            if (role == null) {
                role = Role.builder().nom(roleData[0]).description(roleData[1]).permissions(new HashSet<>()).build();
            }

            Set<Permission> perms = new HashSet<>();
            switch (roleData[0]) {
                case "ADMIN":
                    perms.addAll(permissionRepository.findAll());
                    break;
                case "CEEP":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "CREATE_AT", "EDIT_AT", "SUBMIT_AT", "SIGN_AT", "VALIDATE_AT",
                        "RENEW_AT", "RECEIVE_AT", "CLOSE_AT", "CREATE_VISITE", "TRANSFER_AT",
                        "VIEW_RECEPTION", "CREATE_RECEPTION", "EDIT_RECEPTION", "SIGN_RECEPTION",
                        "VIEW_PERMIS", "CREATE_PERMIS", "EDIT_PERMIS", "UPLOAD_PERMIS", "ANALYSE_PERMIS",
                        "UPLOAD_FILES", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                    )).forEach(perms::add);
                    break;
                case "CEEE":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "EDIT_AT", "SIGN_AT", "VALIDATE_AT", "START_INTERVENTION",
                        "DECLARE_FIN_TRAVAUX", "RECEIVE_AT", "CLOSE_AT", "CREATE_VISITE", "RENEW_AT",
                        "VIEW_RECEPTION", "CREATE_RECEPTION", "SIGN_RECEPTION",
                        "VIEW_PERMIS", "CREATE_PERMIS", "EDIT_PERMIS", "UPLOAD_PERMIS", "ANALYSE_PERMIS",
                        "UPLOAD_FILES", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                    )).forEach(perms::add);
                    break;
                case "HCEP":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "CLASSIFY_INTERVENTION", "SIGN_AT", "VALIDATE_AT", "REJECT_AT",
                        "VALIDATE_VISITE", "VIEW_ARCHIVE", "ARCHIVE_AT", "MANAGE_HABILITATIONS",
                        "VIEW_RECEPTION", "VIEW_PERMIS", "EXPORT_PDF", "RECEIVE_NOTIFICATION",
                        "MANAGE_REFERENTIALS", "VIEW_AUDIT"
                    )).forEach(perms::add);
                    break;
                case "HCEE":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "SIGN_AT", "VALIDATE_AT", "REJECT_AT", "VALIDATE_VISITE",
                        "START_INTERVENTION", "ARCHIVE_AT", "VIEW_ARCHIVE", "VIEW_RECEPTION",
                        "VIEW_PERMIS", "EXPORT_PDF", "RECEIVE_NOTIFICATION", "MANAGE_REFERENTIALS", "VIEW_AUDIT"
                    )).forEach(perms::add);
                    break;
                case "HMEP":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "SIGN_AT", "VALIDATE_AT", "REJECT_AT", "VALIDATE_VISITE",
                        "VIEW_ARCHIVE", "ARCHIVE_AT", "VIEW_RECEPTION", "VIEW_PERMIS", "EXPORT_PDF",
                        "RECEIVE_NOTIFICATION"
                    )).forEach(perms::add);
                    break;
                case "HMEE":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "SIGN_AT", "VALIDATE_AT", "REJECT_AT", "VALIDATE_VISITE",
                        "START_INTERVENTION", "VIEW_ARCHIVE", "VIEW_RECEPTION", "VIEW_PERMIS",
                        "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                    )).forEach(perms::add);
                    break;
                case "CE":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "CREATE_AT", "EDIT_AT", "SUBMIT_AT", "READ_AT", "CREATE_VISITE", "SIGN_AT",
                        "CLOSE_AT", "RECEIVE_AT", "START_INTERVENTION", "DECLARE_FIN_TRAVAUX",
                        "RENEW_AT", "VALIDATE_AT", "REJECT_AT", "VIEW_RECEPTION", "CREATE_RECEPTION",
                        "EDIT_RECEPTION", "SIGN_RECEPTION", "VIEW_PERMIS", "CREATE_PERMIS", "EDIT_PERMIS",
                        "UPLOAD_PERMIS", "ANALYSE_PERMIS", "UPLOAD_FILES", "EXPORT_PDF", "RECEIVE_NOTIFICATION",
                        "TRANSFER_AT"
                    )).forEach(perms::add);
                    break;
                case "HM":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "VALIDATE_VISITE", "SIGN_AT", "START_INTERVENTION", "VALIDATE_AT",
                        "REJECT_AT", "VIEW_ARCHIVE", "ARCHIVE_AT", "VIEW_RECEPTION", "VIEW_PERMIS",
                        "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                    )).forEach(perms::add);
                    break;
                case "HC":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "CLASSIFY_INTERVENTION", "VALIDATE_AT", "REJECT_AT", "VALIDATE_VISITE",
                        "SIGN_AT", "START_INTERVENTION", "ARCHIVE_AT", "VIEW_ARCHIVE", "VIEW_RECEPTION",
                        "MANAGE_HABILITATIONS", "MANAGE_REFERENTIALS", "VIEW_AUDIT", "VIEW_PERMIS",
                        "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                    )).forEach(perms::add);
                    break;
                case "RESPONSABLE_EXTERIEUR":
                    permissionRepository.findByNomIn(Arrays.asList(
                        "READ_AT", "VIEW_PERMIS", "EDIT_PERMIS", "UPLOAD_PERMIS",
                        "UPLOAD_FILES", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                    )).forEach(perms::add);
                    break;
            }
            role.setPermissions(perms);
            roleRepository.save(role);
            logger.info("Rôle synchronisé: {} ({} perms)", roleData[0], perms.size());
        }
    }

    private void initDefaultUsers() {
        createOrUpdateUser("admin@ocp.ma", "ADMIN001", "Administrateur", "Système", "Admin@123", "ADMIN");
        createOrUpdateUser("ceep@ocp.ma", "CEEP001", "Chef Equipe", "Propriétaire", "Password123!", "CEEP");
        createOrUpdateUser("ceee@ocp.ma", "CEEE001", "Chef Equipe", "Exécutant", "Password123!", "CEEE");
        createOrUpdateUser("hcep@ocp.ma", "HCEP001", "Hors Cadre", "Propriétaire", "Password123!", "HCEP");
        createOrUpdateUser("hcee@ocp.ma", "HCEE001", "Hors Cadre", "Exécutant", "Password123!", "HCEE");
        createOrUpdateUser("hmep@ocp.ma", "HMEP001", "Haute Maîtrise", "Propriétaire", "Password123!", "HMEP");
        createOrUpdateUser("hmee@ocp.ma", "HMEE001", "Haute Maîtrise", "Exécutante", "Password123!", "HMEE");
    }

    private void createOrUpdateUser(String email, String matricule, String nom, String prenom, String rawPassword, String roleName) {
        Role role = roleRepository.findByNom(roleName).orElse(null);
        if (role == null) return;

        Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = Utilisateur.builder()
                    .matricule(matricule)
                    .nom(nom)
                    .prenom(prenom)
                    .email(email)
                    .motDePasse(passwordEncoder.encode(rawPassword))
                    .actif(true)
                    .roles(new HashSet<>(Set.of(role)))
                    .build();
            utilisateurRepository.save(user);
            logger.info("Utilisateur créé: {} ({})", email, roleName);
        } else {
            if (user.getRoles() == null || user.getRoles().isEmpty() || !user.getRoles().contains(role)) {
                Set<Role> roles = user.getRoles() != null ? new HashSet<>(user.getRoles()) : new HashSet<>();
                roles.add(role);
                user.setRoles(roles);
                utilisateurRepository.save(user);
                logger.info("Rôle {} ajouté à l'utilisateur: {}", roleName, email);
            }
        }
    }
}
