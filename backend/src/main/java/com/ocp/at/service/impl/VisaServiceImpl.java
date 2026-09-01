package com.ocp.at.service.impl;

import com.ocp.at.dto.request.VisaRequest;
import com.ocp.at.dto.response.VisaResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.Visa;
import com.ocp.at.entity.enums.StatutVisa;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.VisaMapper;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import com.ocp.at.repository.VisaRepository;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.AuditService;
import com.ocp.at.service.NotificationService;
import com.ocp.at.service.VisaService;
import com.ocp.at.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.ocp.at.security.RoleUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisaServiceImpl implements VisaService {

    private final VisaRepository visaRepository;
    private final AutorisationTravailRepository atRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VisaMapper visaMapper;
    private final StorageService storageService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public VisaResponse createVisa(VisaRequest request) {
        AutorisationTravail at = atRepository.findById(request.getAutorisationTravailId())
                .orElseThrow(() -> new ResourceNotFoundException("AT non trouvée"));

        String currentUserId = SecurityUtils.getCurrentUtilisateurId()
                .orElseThrow(() -> new BusinessException("Non authentifié"));
        Utilisateur currentUser = utilisateurRepository.findById(currentUserId)
                .or(() -> utilisateurRepository.findByEmail(currentUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Visa visa = Visa.builder()
                .dateVisa(LocalDateTime.now())
                .statut(StatutVisa.EN_ATTENTE)
                .commentaire(request.getCommentaire())
                .ordre(request.getOrdre() != null ? request.getOrdre() : 1)
                .utilisateur(currentUser)
                .autorisationTravail(at)
                .build();

        visa = visaRepository.save(visa);
        return visaMapper.toResponse(visa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisaResponse> getVisasByAtId(String atId) {
        return visaRepository.findByAutorisationTravailId(atId).stream()
                .map(visaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VisaResponse signVisa(String visaId, MultipartFile signature, String commentaire) {
        Visa visa = visaRepository.findById(visaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visa non trouvé"));

        String currentUserId = SecurityUtils.getCurrentUtilisateurId()
                .orElseThrow(() -> new BusinessException("Non authentifié"));
        Utilisateur currentUser = utilisateurRepository.findById(currentUserId)
                .or(() -> utilisateurRepository.findByEmail(currentUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (visa.getStatut() != StatutVisa.EN_ATTENTE) {
            throw new BusinessException("Ce visa n'est pas en attente de signature");
        }

        // Permettre au signataire habilité effectif d'apposer son visa même si le visa en attente a été créé par un tiers
        if (!visa.getUtilisateur().getId().equals(currentUser.getId())) {
            boolean hasSignPerm = currentUser.getRoles() != null && currentUser.getRoles().stream()
                    .anyMatch(r -> r.getPermissions() != null && r.getPermissions().stream()
                            .anyMatch(p -> "SIGN_AT".equals(p.getNom()) || "VALIDATE_AT".equals(p.getNom())));
            if (RoleUtils.userHasRolePattern(currentUser, "ADMIN") || hasSignPerm) {
                visa.setUtilisateur(currentUser);
            } else {
                throw new BusinessException("Vous n'êtes pas autorisé à signer ce visa");
            }
        }

        if (signature == null || signature.isEmpty()) {
            throw new BusinessException("La signature est obligatoire");
        }

        AutorisationTravail at = visa.getAutorisationTravail();
        List<Visa> existingVisas = visaRepository.findByAutorisationTravailId(at.getId());

        boolean ceepSigned = isRoleSigned(existingVisas, "CEEP") || (at.getStatut() != null && at.getStatut() != com.ocp.at.entity.enums.StatutAT.BROUILLON);
        boolean ceeeSigned = isRoleSigned(existingVisas, "CEEE")
                || (at.getDateReceptionCeee() != null)
                || (at.getStatut() != null && at.getStatut() != com.ocp.at.entity.enums.StatutAT.BROUILLON && at.getStatut() != com.ocp.at.entity.enums.StatutAT.SOUMISE && at.getStatut() != com.ocp.at.entity.enums.StatutAT.DEMANDE_CREEE);
        boolean hcepSigned = isRoleSigned(existingVisas, "HCEP");
        boolean hceeSigned = isRoleSigned(existingVisas, "HCEE");
        boolean hmepSigned = isRoleSigned(existingVisas, "HMEP");

        String targetRole = resolveSigningRole(visa, commentaire, currentUser, hcepSigned, hmepSigned);
        boolean isAdmin = RoleUtils.userHasRolePattern(currentUser, "ADMIN");

        // Ordre strict selon le standard OCP S-HSE-SEC-31 :
        // 1. CEEP -> 2. CEEE -> 3. HCEP -> 4. HCEE -> 5. HMEP -> 6. HMEE
        if (!isAdmin) {
            switch (targetRole) {
                case "CEEP":
                    // Le CEEP est le premier à signer (Étape 1 lors de la création/transmission)
                    break;
                case "CEEE":
                    // 1. Le CEEE doit signer APRÈS le CEEP
                    if (!ceepSigned) {
                        throw new BusinessException("Le CEEP doit d'abord signer l'AT (Étape 1) avant la signature du CEEE (Étape 2).");
                    }
                    break;
                case "HCEP":
                    // 2. Le HCEP doit signer APRÈS CEEP et CEEE
                    if (!ceepSigned || !ceeeSigned) {
                        throw new BusinessException("Le CEEP et le CEEE doivent d'abord signer l'AT avant la signature du Hors Cadre Émetteur / Propriétaire (HCEP, Étape 3).");
                    }
                    break;
                case "HCEE":
                    // 3. Le HCEE doit signer APRÈS le HCEP
                    if (!hcepSigned) {
                        throw new BusinessException("Le Hors Cadre Émetteur (HCEP) doit d'abord signer l'AT (Étape 3) avant la signature du Hors Cadre Exécutant (HCEE, Étape 4).");
                    }
                    break;
                case "HMEP":
                    // 4. Le HMEP doit signer APRÈS les Hors Cadre (HCEP et HCEE)
                    if (!hcepSigned || !hceeSigned) {
                        throw new BusinessException("Les Hors Cadre (HCEP et HCEE) doivent d'abord signer l'AT avant la signature de la Haute Maîtrise Émetteur / Propriétaire (HMEP, Étape 5).");
                    }
                    break;
                case "HMEE":
                    // 5. Le HMEE doit signer APRÈS le HMEP
                    if (!hmepSigned) {
                        throw new BusinessException("La Haute Maîtrise Émetteur (HMEP) doit d'abord signer l'AT (Étape 5) avant la signature de la Haute Maîtrise Exécutante (HMEE, Étape 6).");
                    }
                    break;
                default:
                    // Rôle générique ou inconnu : autoriser si les premières étapes sont respectées
                    break;
            }
        }

        String contentType = signature.getContentType();
        String originalName = signature.getOriginalFilename();
        boolean isPng = (contentType != null && "image/png".equalsIgnoreCase(contentType))
                || (originalName != null && originalName.toLowerCase().endsWith(".png"))
                || contentType == null
                || "application/octet-stream".equalsIgnoreCase(contentType);
        if (!isPng) {
            throw new BusinessException("Seules les images PNG sont acceptées pour la signature");
        }

        // Read once - MultipartFile stream cannot be consumed twice
        final byte[] signatureBytes;
        try {
            signatureBytes = signature.getBytes();
        } catch (IOException e) {
            log.error("Erreur lecture signature", e);
            throw new BusinessException("Impossible de lire le fichier de signature");
        }
        if (signatureBytes.length == 0) {
            throw new BusinessException("La signature est obligatoire");
        }

        final String hash;
        try {
            hash = calculateSHA256(signatureBytes);
        } catch (Exception e) {
            log.error("Erreur calcul SHA-256", e);
            throw new BusinessException("Erreur lors de la validation de la signature");
        }

        String filename = UUID.randomUUID().toString() + ".png";
        String path = storageService.saveSignatureBytes(signatureBytes, filename);

        visa.setSignaturePath(path);
        visa.setSignatureHash(hash);
        visa.setDateSignature(LocalDateTime.now());
        visa.setStatut(StatutVisa.VALIDE);
        if (commentaire != null && !commentaire.trim().isEmpty()) {
            visa.setCommentaire(commentaire);
        }

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        visa.setAdresseIP(req.getRemoteAddr());
        visa.setNavigateur(req.getHeader("User-Agent"));

        visa = visaRepository.save(visa);

        if ("CEEE".equals(targetRole) && at.getDateReceptionCeee() == null) {
            at.setDateReceptionCeee(LocalDateTime.now());
            atRepository.save(at);
        }

        auditService.logAction("SIGN_VISA", "SUCCES", visa.getUtilisateur(), req.getRemoteAddr(),
                req.getHeader("User-Agent"));

        // --- Notifications post-signature selon le rôle signataire ---
        try {
            envoyerNotificationsPostSignature(at, targetRole, visa);
        } catch (Exception e) {
            log.warn("Notification post-signature non bloquante: {}", e.getMessage());
        }

        return visaMapper.toResponse(visa);
    }

    /**
     * Envoie les notifications contextuelle après une signature, selon le rôle du signataire.
     * Respecte la chaîne standard OCP S-HSE-SEC-31 :
     *   CEEP (1) -> CEEE (2) -> HCEP (3) -> HCEE (4) -> HMEP (5) -> HMEE (6)
     */
    private void envoyerNotificationsPostSignature(AutorisationTravail at, String roleSignataire, Visa visa) {
        String atNumero = at.getNumero();
        String lienAt = "/autorisations/" + at.getId();

        switch (roleSignataire) {
            case "CEEP":
                // Après visa CEEP : notifier le CEEE du service exécutant pour accuser réception + signer
                if (at.getServicesIntervenants() != null) {
                    utilisateurRepository.findAll().stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("CEEE"))
                                && u.getService() != null
                                && at.getServicesIntervenants().equalsIgnoreCase(u.getService().getNomService()))
                        .forEach(ceee -> notificationService.createNotification(
                                ceee,
                                "AT " + atNumero + " - Signature CEEE requise",
                                "Le CEEP a signé l'AT " + atNumero + ". Vous devez accuser réception puis apposer votre visa CEEE (Étape 2).",
                                "ACTION",
                                lienAt
                        ));
                }
                // Fallback : envoyer à tous les CEEE
                if (at.getZoneExecutante() != null) {
                    utilisateurRepository.findChefsEquipeByZoneId(at.getZoneExecutante().getId())
                        .stream()
                        .filter(u -> u.getRoles() != null && u.getRoles().stream()
                                .anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("CEEE")))
                        .forEach(ceee -> notificationService.createNotification(
                                ceee,
                                "AT " + atNumero + " - Signature CEEE requise",
                                "Le CEEP a signé l'AT " + atNumero + ". Vous devez accuser réception et signer (Étape 2).",
                                "ACTION",
                                lienAt
                        ));
                }
                break;

            case "CEEE":
                // Après visa CEEE : notifier HCEP et HCEE pour signature Hors Cadre (Étapes 3 & 4)
                notificationService.sendNotificationToRoleForAt("HCEP", at,
                        "AT " + atNumero + " - Visa HCEP requis (Étape 3)",
                        "Le CEEP et CEEE ont signé l'AT " + atNumero + ". Vous devez apposer votre visa Hors Cadre Émetteur (HCEP, Étape 3).",
                        "ACTION", lienAt);
                notificationService.sendNotificationToRoleForAt("HCEE", at,
                        "AT " + atNumero + " - Visa HCEE requis (Étape 4)",
                        "Le CEEP et CEEE ont signé l'AT " + atNumero + ". Vous devez apposer votre visa Hors Cadre Exécutant (HCEE, Étape 4) après le HCEP.",
                        "INFO", lienAt);
                // Informer aussi le CEEP que le CEEE a signé
                if (at.getProprietaireBrouillon() != null) {
                    notificationService.createNotification(
                            at.getProprietaireBrouillon(),
                            "AT " + atNumero + " - Visa CEEE reçu",
                            "Le CEEE a apposé son visa sur l'AT " + atNumero + ". L'AT attend maintenant la signature des Hors Cadre.",
                            "INFO", lienAt);
                }
                break;

            case "HCEP":
                // Après visa HCEP : notifier HCEE pour sa signature (Étape 4)
                notificationService.sendNotificationToRoleForAt("HCEE", at,
                        "AT " + atNumero + " - Visa HCEE requis (Étape 4)",
                        "Le HCEP a signé l'AT " + atNumero + ". Votre visa Hors Cadre Exécutant est maintenant requis (Étape 4).",
                        "ACTION", lienAt);
                break;

            case "HCEE":
                // Après visa HCEE : notifier HMEP et HMEE pour signature Haute Maîtrise (Étapes 5 & 6)
                notificationService.sendNotificationToRoleForAt("HMEP", at,
                        "AT " + atNumero + " - Visa HMEP requis (Étape 5)",
                        "Les Hors Cadre ont signé l'AT " + atNumero + ". Votre visa Haute Maîtrise Émetteur (HMEP, Étape 5) est requis.",
                        "ACTION", lienAt);
                notificationService.sendNotificationToRoleForAt("HMEE", at,
                        "AT " + atNumero + " - Visa HMEE requis (Étape 6)",
                        "Les Hors Cadre ont signé l'AT " + atNumero + ". Votre visa Haute Maîtrise Exécutant (HMEE, Étape 6) sera requis après le HMEP.",
                        "INFO", lienAt);
                break;

            case "HMEP":
                // Après visa HMEP : notifier HMEE pour sa signature (Étape 6)
                notificationService.sendNotificationToRoleForAt("HMEE", at,
                        "AT " + atNumero + " - Visa HMEE requis (Étape 6)",
                        "Le HMEP a signé l'AT " + atNumero + ". Votre visa Haute Maîtrise Exécutant est maintenant requis (Étape 6).",
                        "ACTION", lienAt);
                break;

            case "HMEE":
                // Après visa HMEE : AT entièrement visée, notifier CEEP + CEEE que l'AT peut démarrer
                if (at.getProprietaireBrouillon() != null) {
                    notificationService.createNotification(
                            at.getProprietaireBrouillon(),
                            "AT " + atNumero + " - Toutes signatures obtenues",
                            "L'AT " + atNumero + " a été visée par toutes les parties (CEEP, CEEE, HCEP, HCEE, HMEP, HMEE). L'intervention peut démarrer.",
                            "SUCCESS", lienAt);
                }
                notificationService.sendNotificationToRoleForAt("CEEE", at,
                        "AT " + atNumero + " - Prête au démarrage",
                        "L'AT " + atNumero + " a reçu l'ensemble des visas requis. L'intervention peut maintenant démarrer.",
                        "SUCCESS", lienAt);
                break;

            default:
                log.debug("Aucune notification spécifique pour le rôle signataire: {}", roleSignataire);
        }
    }

    @Override
    public Resource downloadSignature(String visaId) {
        Visa visa = visaRepository.findById(visaId)
                .orElseThrow(() -> new ResourceNotFoundException("Visa non trouvé"));

        if (visa.getSignaturePath() == null) {
            throw new BusinessException("Aucune signature associée à ce visa");
        }

        return storageService.loadSignature(visa.getSignaturePath());
    }

    private String calculateSHA256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String resolveSigningRole(Visa visa, String commentaire, Utilisateur user, boolean hcepSigned, boolean hmepSigned) {
        String comm = ((commentaire != null ? commentaire : "") + " " + (visa.getCommentaire() != null ? visa.getCommentaire() : "")).toUpperCase();
        if (comm.contains("CEEP") || comm.contains("G1VISACEEP")) {
            return "CEEP";
        }
        if (comm.contains("CEEE") || comm.contains("G1VISACEEE")) {
            return "CEEE";
        }
        if (comm.contains("HCEP")) {
            return "HCEP";
        }
        if (comm.contains("HCEE")) {
            return "HCEE";
        }
        if (comm.contains("HMEP")) {
            return "HMEP";
        }
        if (comm.contains("HMEE")) {
            return "HMEE";
        }

        // Vérification explicite des rôles précis en base (sans équivalence générique)
        boolean hasExactHcep = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("HCEP"));
        boolean hasExactHcee = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("HCEE"));
        boolean hasExactHmep = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("HMEP"));
        boolean hasExactHmee = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("HMEE"));
        boolean hasExactCeee = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("CEEE"));
        boolean hasExactCeep = user.getRoles() != null && user.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().toUpperCase().contains("CEEP"));

        if (hasExactHcep && !hasExactHcee) return "HCEP";
        if (hasExactHcee && !hasExactHcep) return "HCEE";
        if (hasExactHmep && !hasExactHmee) return "HMEP";
        if (hasExactHmee && !hasExactHmep) return "HMEE";
        if (hasExactCeee) return "CEEE";
        if (hasExactCeep) return "CEEP";

        // Déduction pour les rôles polyvalents ou génériques HC / HM / CE :
        if (RoleUtils.userHasRolePattern(user, "HC")) {
            return !hcepSigned ? "HCEP" : "HCEE";
        }
        if (RoleUtils.userHasRolePattern(user, "HM")) {
            return !hmepSigned ? "HMEP" : "HMEE";
        }
        if (RoleUtils.userHasRolePattern(user, "CE")) {
            return "CEEP";
        }

        return "UNKNOWN";
    }

    private boolean isRoleSigned(List<Visa> visas, String roleCode) {
        if (visas == null || visas.isEmpty()) return false;
        return visas.stream().anyMatch(v -> {
            // Seuls les visas validés/signés avec une signature effective sont considérés comme signés
            if (v.getStatut() == null
                    || v.getStatut() == StatutVisa.EN_ATTENTE
                    || v.getStatut() == StatutVisa.REFUS
                    || v.getStatut() == StatutVisa.REFUSE) {
                return false;
            }
            if (v.getSignaturePath() == null || v.getSignaturePath().isBlank()) {
                return false;
            }
            String comment = v.getCommentaire() != null ? v.getCommentaire().toUpperCase() : "";
            if ("HCEE".equalsIgnoreCase(roleCode)) {
                return comment.contains("HCEE");
            }
            if ("HCEP".equalsIgnoreCase(roleCode)) {
                return comment.contains("HCEP");
            }
            if ("HMEE".equalsIgnoreCase(roleCode)) {
                return comment.contains("HMEE");
            }
            if ("HMEP".equalsIgnoreCase(roleCode)) {
                return comment.contains("HMEP");
            }
            if ("CEEE".equalsIgnoreCase(roleCode)) {
                return comment.contains("CEEE") || comment.contains("G1VISACEEE");
            }
            if ("CEEP".equalsIgnoreCase(roleCode)) {
                return comment.contains("CEEP") || comment.contains("G1VISACEEP");
            }
            if (comment.contains(roleCode.toUpperCase())) return true;
            Utilisateur u = v.getUtilisateur();
            if (u != null && u.getRoles() != null) {
                return u.getRoles().stream().anyMatch(r -> r.getNom() != null && r.getNom().equalsIgnoreCase(roleCode));
            }
            return false;
        });
    }
}
