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
            new String[]{"CEEP", "Chef d'Équipe de l'Entité Propriétaire — opérationnel terrain, rédige l'AT"},
            new String[]{"CEEE", "Chef d'Équipe de l'Entité Exécutante — terrain, déclare fin des travaux"},
            new String[]{"HCEP", "Hors Cadre Responsable de l'Entité Propriétaire — garant archivage, désignateur agents habilités"},
            new String[]{"HCEE", "Hors Cadre Responsable de l'Entité Exécutante — garant visites, rédaction AT, début intervention"},
            new String[]{"HMEP", "Haute Maîtrise de l'Entité Propriétaire — garant visite chantier et début intervention"},
            new String[]{"HMEE", "Haute Maîtrise de l'Entité Exécutante — rôle non clarifié par le standard, permissions minimales"},
            new String[]{"RESPONSABLE_ENTREPRISE", "Responsable d'entreprise externe (sous-traitant) — gestion des permis liés au BT"}
        );

        for (String[] roleData : roles) {
            if (!roleRepository.existsByNom(roleData[0])) {
                Set<Permission> perms = new HashSet<>();

                switch (roleData[0]) {
                    case "ADMIN":
                        perms.addAll(permissionRepository.findAll());
                        break;

                    case "CEEP":
                        // §8.1 E (demande), §8.2 E (visite), §8.3/§8.4 E (rédaction/reconduction), §8.5 E (réception)
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "CREATE_AT", "EDIT_AT", "SUBMIT_AT",
                            "SIGN_AT",           // §8.3 signe l'AT, §8.4 vise la reconduction
                            "RENEW_AT",          // §8.4 reconduire en cas de dépassement poste
                            "RECEIVE_AT",        // §8.5 réceptionner les travaux
                            "CLOSE_AT",          // §8.5 clôturer AT et permis après réception
                            "CREATE_VISITE",     // §8.2 exécuter la visite chantier
                            "TRANSFER_AT",       // Transférer le verrou
                            "UPLOAD_FILES", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "CEEE":
                        // §8.1 I (informé), §8.2 P, §8.3 P, §4 E (début), §8.4 P, §8.5 E (déclaration fin), §8.5 P (réception)
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "EDIT_AT",
                            "SIGN_AT",             // §8.3 P co-signe, §8.4 P vise la reconduction
                            "START_INTERVENTION",  // §4 Exécute le démarrage
                            "DECLARE_FIN_TRAVAUX", // §8.5 Exécute la déclaration de fin
                            "VIEW_PERMIS", "EDIT_PERMIS",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HCEP":
                        // §8.6 G (garant archivage), §9 (désignation agents habilités), Étape 0 (classification)
                        // TODO: à valider avec OCP — signature opérationnelle directe sur l'AT non confirmée par le standard
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT",
                            "CLASSIFY_INTERVENTION",  // Étape 0 : classifie Niveau 1/2
                            "MANAGE_HABILITATIONS",   // §9 : désigne agents habilités AT
                            "VIEW_ARCHIVE",           // §8.6 G : consulte/supervise les archives
                            "MANAGE_REFERENTIALS", "VIEW_AUDIT",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HCEE":
                        // §8.2 G (visite), §8.3 G (rédaction AT), §4 G (début intervention), §8.4 G (reconduction), §8.6 E (archivage)
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT",
                            "VALIDATE_AT", "REJECT_AT",  // §8.3 Garant — valide/refuse l'AT
                            "SIGN_AT",                   // §8.3 G co-signe l'AT
                            "VALIDATE_VISITE",           // §8.2 Garant de la visite chantier
                            "ARCHIVE_AT",               // §8.6 Exécute l'archivage officiel
                            "VIEW_ARCHIVE",             // §8.6 Consulte les archives
                            "VIEW_PERMIS",
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HMEP":
                        // §8.2 G (visite chantier), §4 G (démarrage intervention) — rôle de garant à ces deux étapes uniquement
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT",
                            "VALIDATE_VISITE",  // §8.2 Garant de la visite chantier
                            "SIGN_AT",          // §4 Garant du démarrage (co-signe)
                            "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "HMEE":
                        // TODO: à valider avec OCP — rôle non clarifié par le standard
                        // Le logigramme §7 présente la colonne HMEE mais les cases sont non renseignées.
                        // Comportement fail-closed intentionnel : lecture + notifications seules.
                        // Aucun droit d'écriture tant que le rôle n'est pas clarifié.
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT",
                            "EXPORT_PDF",
                            "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                        break;

                    case "RESPONSABLE_ENTREPRISE":
                        // Hors logique P/E — sous-traitant externe, uniquement via BT (Bon de Travail)
                        // Gère les permis liés au BT. Ne participe PAS au workflow AT normal.
                        permissionRepository.findByNomIn(Arrays.asList(
                            "VIEW_PERMIS", "UPLOAD_PERMIS", "ANALYSE_PERMIS", "CREATE_PERMIS",
                            "READ_AT", "EXPORT_PDF", "RECEIVE_NOTIFICATION"
                        )).forEach(perms::add);
                }

                roleRepository.save(Role.builder()
                        .nom(roleData[0]).description(roleData[1]).permissions(perms).build());
                logger.info("Rôle créé: {}", roleData[0]);
            } else if ("ADMIN".equals(roleData[0])) {
                // Toujours s'assurer que l'ADMIN a toutes les permissions (en cas d'ajout de nouvelles permissions)
                Role adminRole = roleRepository.findByNom("ADMIN").get();
                adminRole.setPermissions(new HashSet<>(permissionRepository.findAll()));
                roleRepository.save(adminRole);
                logger.info("Permissions du rôle ADMIN synchronisées");
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
