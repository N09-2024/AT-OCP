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
            new String[]{"READ_AT", "Consulter les autorisations de travail"},
            new String[]{"CREATE_AT", "Créer une autorisation de travail"},
            new String[]{"EDIT_AT", "Modifier une autorisation de travail"},
            new String[]{"SUBMIT_AT", "Soumettre une autorisation de travail"},
            new String[]{"VALIDATE_AT", "Valider une autorisation de travail"},
            new String[]{"REJECT_AT", "Rejeter une autorisation de travail"},
            new String[]{"CLOSE_AT", "Clôturer une autorisation de travail"},
            new String[]{"MANAGE_USERS", "Gérer les utilisateurs"},
            new String[]{"MANAGE_ROLES", "Gérer les rôles et permissions"},
            new String[]{"MANAGE_REFERENTIALS", "Gérer les référentiels"},
            new String[]{"VIEW_AUDIT", "Consulter les logs d'audit"},
            new String[]{"EXPORT_PDF", "Exporter une AT en PDF"},
            new String[]{"UPLOAD_FILES", "Uploader des fichiers"},
            new String[]{"VIEW_PERMIS", "Consulter les permis"},
            new String[]{"CREATE_PERMIS", "Créer un permis"},
            new String[]{"EDIT_PERMIS", "Modifier un permis"},
            new String[]{"DELETE_PERMIS", "Supprimer un permis"},
            new String[]{"UPLOAD_PERMIS", "Uploader un fichier de permis"},
            new String[]{"ANALYSE_PERMIS", "Analyser un permis avec l'IA"},
            new String[]{"MANAGE_DOCUMENTS", "Gérer les documents source et visites"}
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
            new String[]{"DEMANDEUR", "Demandeur d'autorisation de travail"},
            new String[]{"RESPONSABLE_OCP", "Responsable OCP validateur"},
            new String[]{"RESPONSABLE_ENTREPRISE", "Responsable d'entreprise externe"}
        );

        for (String[] roleData : roles) {
            if (!roleRepository.existsByNom(roleData[0])) {
                Set<Permission> perms = new HashSet<>();

                switch (roleData[0]) {
                    case "ADMIN" ->
                        perms.addAll(permissionRepository.findAll());

                    case "DEMANDEUR" ->
                        // Use case: Créer Demande d'Intervention, Consulter état AT, Clôturer AT
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "CREATE_AT", "EDIT_AT", "SUBMIT_AT", "CLOSE_AT", "UPLOAD_FILES", "MANAGE_DOCUMENTS"
                        )).forEach(perms::add);

                    case "RESPONSABLE_OCP" ->
                        // Use case: Consulter autorisations, Signer, Vérifier AT, Valider, Rejeter, Réceptionner travaux
                        permissionRepository.findByNomIn(Arrays.asList(
                            "READ_AT", "VALIDATE_AT", "REJECT_AT", "SIGN_AT", "RECEIVE_AT", "VIEW_PERMIS"
                        )).forEach(perms::add);

                    case "RESPONSABLE_ENTREPRISE" ->
                        // Use case: Importer permis, Photographier permis, Consulter résultats IA
                        permissionRepository.findByNomIn(Arrays.asList(
                            "VIEW_PERMIS", "UPLOAD_PERMIS", "ANALYSE_PERMIS", "CREATE_PERMIS"
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
