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
        Utilisateur currentUser = utilisateurRepository.findByEmail(currentUserId)
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
        Utilisateur currentUser = utilisateurRepository.findByEmail(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        if (!visa.getUtilisateur().getId().equals(currentUser.getId())) {
            throw new BusinessException("Vous n'êtes pas autorisé à signer ce visa");
        }

        if (visa.getStatut() != StatutVisa.EN_ATTENTE) {
            throw new BusinessException("Ce visa n'est pas en attente de signature");
        }

        if (signature == null || signature.isEmpty()) {
            throw new BusinessException("La signature est obligatoire");
        }


        AutorisationTravail at = visa.getAutorisationTravail();
        // La vérification de réception CEEE ne s'applique qu'aux signataires ayant le rôle CEEE
        if (RoleUtils.userHasRolePattern(currentUser, "CEEE")
                && at.getDateReceptionCeee() == null) {
            throw new BusinessException("Le CEEE doit d'abord accuser réception de l'AT avant de pouvoir la signer.");
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

        // Read once — MultipartFile stream cannot be consumed twice
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

        auditService.logAction("SIGN_VISA", "SUCCES", visa.getUtilisateur(), req.getRemoteAddr(),
                req.getHeader("User-Agent"));

        return visaMapper.toResponse(visa);
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
}
