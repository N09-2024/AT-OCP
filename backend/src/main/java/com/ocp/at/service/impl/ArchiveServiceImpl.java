package com.ocp.at.service.impl;

import com.ocp.at.dto.request.ArchiveSearchRequest;
import com.ocp.at.dto.response.ArchiveResponse;
import com.ocp.at.dto.response.ArchiveSearchResponse;
import com.ocp.at.dto.response.PdfExportResponse;
import com.ocp.at.entity.*;
import com.ocp.at.entity.enums.ArchiveStatus;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.mapper.ArchiveMapper;
import com.ocp.at.repository.*;
import com.ocp.at.security.SecurityUtils;
import com.ocp.at.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation du service d'archivage des Autorisations de Travail.
 * Module 10 : Export PDF, Archivage, Audit Final.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArchiveServiceImpl implements ArchiveService {

    private final AutorisationTravailRepository atRepository;
    private final ArchiveRepository archiveRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StorageService storageService;
    private final PdfGeneratorService pdfGeneratorService;
    private final QrCodeService qrCodeService;
    private final HashService hashService;
    private final ArchiveMapper archiveMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final HistoriqueATRepository historiqueATRepository;

    // =========================================================================
    // ARCHIVAGE
    // =========================================================================

    @Override
    @Transactional
    public ArchiveResponse archiverAT(String atId) {
        log.info("Archivage de l'AT : {}", atId);
        AutorisationTravail at = getEntityById(atId);

        StatutAT st = at.getStatut();
        StatutAT stW = at.getStatutWorkflow();
        boolean isArchivable = st == StatutAT.CLOTUREE || st == StatutAT.TRAVAUX_RECEPTIONES || st == StatutAT.ARCHIVEE
                || stW == StatutAT.TRAVAUX_RECEPTIONES || stW == StatutAT.RECEPTIONEES
                || stW == StatutAT.CLOTUREE || stW == StatutAT.ARCHIVEE;

        if (!isArchivable) {
            throw new BusinessException("Seule une Autorisation de Travail clôturée ou réceptionnée peut être archivée. Statut actuel : " + at.getStatut());
        }

        // Déterminer la version suivante
        Optional<ArchiveAT> latestOpt = archiveRepository.findTopByAutorisationTravailIdOrderByVersionDesc(atId);
        int nextVersion = 1;
        if (latestOpt.isPresent()) {
            ArchiveAT latest = latestOpt.get();
            latest.setArchiveStatus(ArchiveStatus.SUPERSEDED);
            archiveRepository.save(latest);
            nextVersion = latest.getVersion() + 1;
            log.info("Ancienne archive marquée SUPERSEDED, nouvelle version : {}", nextVersion);
        }

        // Génération du PDF complet
        byte[] pdfBytes = pdfGeneratorService.generateCompleteDossierPdf(at);
        String hash = hashService.calculateSHA256(pdfBytes);

        // Vérifier l'unicité du hash
        if (archiveRepository.existsByHashSHA256(hash)) {
            throw new BusinessException("Un document identique existe déjà dans les archives (même hash SHA-256).");
        }

        // Stockage du PDF
        String year = String.valueOf(LocalDateTime.now().getYear());
        String pdfPath = String.format("archives/%s/%s/v%d/%s.pdf", year, at.getNumero(), nextVersion, at.getNumero());
        try {
            storageService.storeFile(pdfPath, pdfBytes, "application/pdf");
        } catch (IOException e) {
            throw new BusinessException("Erreur lors du stockage du fichier PDF archivé: " + e.getMessage());
        }

        // Génération du QR Code
        String tempId = UUID.randomUUID().toString();
        String qrCodePath;
        try {
            qrCodePath = qrCodeService.generateQrCodeForArchive(tempId, at.getNumero(), String.valueOf(nextVersion), hash);
        } catch (Exception e) {
            log.warn("Impossible de générer le QR Code, archivage continue sans QR", e);
            qrCodePath = null;
        }

        // Création de l'entité ArchiveAT
        ArchiveAT archive = ArchiveAT.builder()
                .numeroArchive(String.format("%s-V%03d", at.getNumero(), nextVersion))
                .version(nextVersion)
                .dateArchivage(LocalDateTime.now())
                .archivePar(getCurrentUser())
                .createdAt(LocalDateTime.now())
                .autorisationTravail(at)
                .pathPdf(pdfPath)
                .hashSHA256(hash)
                .taille((long) pdfBytes.length)
                .mimeType("application/pdf")
                .qrCodePath(qrCodePath)
                .archiveStatus(ArchiveStatus.ACTIVE)
                .commentaire("Archivage automatique v" + nextVersion + " - AT " + at.getNumero())
                .build();

        ArchiveAT saved = archiveRepository.save(archive);

        // Mise à jour du QR Code avec l'ID réel
        try {
            String updatedQrPath = qrCodeService.generateQrCodeForArchive(
                    saved.getId(), at.getNumero(), String.valueOf(nextVersion), hash);
            saved.setQrCodePath(updatedQrPath);
            archiveRepository.save(saved);
        } catch (Exception e) {
            log.warn("Impossible de mettre à jour le QR Code après sauvegarde", e);
        }

        // Mettre à jour le statut AT
        at.setStatut(StatutAT.ARCHIVEE);
        at.setStatutWorkflow(StatutAT.ARCHIVEE);
        atRepository.save(at);

        // Historique AT
        creerHistoriqueAT(at, TypeActionAT.EXPORT_PDF, StatutAT.ARCHIVEE,
                "Archivage v" + nextVersion + " - Hash: " + hash.substring(0, 16) + "...");

        // Audit
        Utilisateur currentUser = getCurrentUser();
        auditService.logAction("ARCHIVAGE_AT_" + at.getNumero(), "SUCCES", currentUser, null, "SYSTEM");

        // Notification
        notificationService.createNotification(currentUser,
                "AT Archivée - " + at.getNumero(),
                "L'AT " + at.getNumero() + " a été archivée avec succès (version " + nextVersion + ").",
                "SUCCESS",
                "/archives/" + saved.getId());

        log.info("AT {} archivée avec succès - Archive ID: {}", at.getNumero(), saved.getId());
        return archiveMapper.toResponse(saved);
    }

    // =========================================================================
    // EXPORT PDF (sans archivage)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public PdfExportResponse exportAT(String atId) {
        log.info("Export PDF de l'AT : {}", atId);
        AutorisationTravail at = getEntityById(atId);

        if (at.getStatut() != StatutAT.CLOTUREE) {
            throw new BusinessException("Seule une Autorisation de Travail clôturée peut être exportée. Statut actuel : " + at.getStatut());
        }

        byte[] pdfBytes = pdfGeneratorService.generateCompleteDossierPdf(at);
        String hash = hashService.calculateSHA256(pdfBytes);
        String nomFichier = at.getNumero() + "_" + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

        // Stockage temporaire
        String pdfPath = "exports/" + nomFichier;
        try {
            storageService.storeFile(pdfPath, pdfBytes, "application/pdf");
        } catch (IOException e) {
            log.warn("Impossible de stocker le PDF d'export temporaire", e);
        }

        // Audit export
        Utilisateur currentUser = getCurrentUser();
        auditService.logAction("EXPORT_PDF_AT_" + at.getNumero(), "SUCCES", currentUser, null, "SYSTEM");

        return PdfExportResponse.builder()
                .nomFichier(nomFichier)
                .hash(hash)
                .dateGeneration(LocalDateTime.now())
                .taille((long) pdfBytes.length)
                .downloadUrl("/api/archives/export/" + atId + "/download")
                .build();
    }

    // =========================================================================
    // CONSULTATION
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ArchiveResponse getById(String id) {
        ArchiveAT archive = archiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archive non trouvée avec l'ID : " + id));
        return archiveMapper.toResponse(archive);
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveResponse getByAutorisationTravailId(String atId) {
        ArchiveAT archive = archiveRepository.findTopByAutorisationTravailIdOrderByVersionDesc(atId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune archive trouvée pour l'AT ID : " + atId));
        return archiveMapper.toResponse(archive);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArchiveResponse> getAll(Pageable pageable) {
        return archiveRepository.findAllByDeletedFalse(pageable).map(archiveMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArchiveSearchResponse> search(ArchiveSearchRequest req, Pageable pageable) {
        String keyword = req.getNumeroAT() != null ? req.getNumeroAT()
                : (req.getNumeroArchive() != null ? req.getNumeroArchive() : "");
        Page<ArchiveAT> archives = archiveRepository.search(keyword, pageable);
        return archives.map(a -> ArchiveSearchResponse.builder()
                .id(a.getId())
                .numeroAT(a.getAutorisationTravail() != null ? a.getAutorisationTravail().getNumero() : null)
                .numeroArchive(a.getNumeroArchive())
                .version(a.getVersion())
                .dateArchivage(a.getDateArchivage())
                .archiveParNom(a.getArchivePar() != null ?
                        a.getArchivePar().getNom() + " " + a.getArchivePar().getPrenom() : null)
                .archiveStatus(a.getArchiveStatus() != null ? a.getArchiveStatus().name() : null)
                .hashDocument(a.getHashSHA256())
                .taillePdf(a.getTaille())
                .build());
    }

    // =========================================================================
    // TÉLÉCHARGEMENT
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadArchive(String id) {
        ArchiveAT archive = archiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archive non trouvée avec l'ID : " + id));
        try {
            return storageService.loadFile(archive.getPathPdf());
        } catch (IOException e) {
            throw new BusinessException("Erreur lors du téléchargement du fichier PDF archivé: " + e.getMessage());
        }
    }

    // =========================================================================
    // VÉRIFICATION D'INTÉGRITÉ
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean verifyArchive(String id) {
        ArchiveAT archive = archiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archive non trouvée avec l'ID : " + id));
        try {
            byte[] pdfBytes = storageService.loadFile(archive.getPathPdf());
            String calculatedHash = hashService.calculateSHA256(pdfBytes);
            boolean valid = calculatedHash.equalsIgnoreCase(archive.getHashSHA256());
            log.info("Vérification archive {} : {}", id, valid ? "VALIDE" : "INVALIDE");
            return valid;
        } catch (IOException e) {
            throw new BusinessException("Erreur lors de la vérification de l'archive: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public com.ocp.at.dto.response.ArchiveReadinessResponse getArchiveReadiness(String atId) {
        AutorisationTravail at = getEntityById(atId);
        List<String> blockingReasons = new ArrayList<>();
        List<com.ocp.at.dto.response.ReadinessCheckItem> checklist = new ArrayList<>();

        // 1. Statut clôturé / réceptionné
        StatutAT st = at.getStatut();
        StatutAT stW = at.getStatutWorkflow();
        boolean isCloturee = st == StatutAT.CLOTUREE || st == StatutAT.TRAVAUX_RECEPTIONES
                || stW == StatutAT.TRAVAUX_RECEPTIONES || stW == StatutAT.RECEPTIONEES || stW == StatutAT.ARCHIVEE;

        checklist.add(com.ocp.at.dto.response.ReadinessCheckItem.builder()
                .code("AT_CLOSED")
                .label("Autorisation clôturée / réceptionnée")
                .passed(isCloturee)
                .blocking(true)
                .message(isCloturee ? "L'AT est clôturée suite à la réception des travaux" : "L'AT n'est pas encore clôturée (statut actuel : " + (stW != null ? stW : st) + ")")
                .build());
        if (!isCloturee) {
            blockingReasons.add("L'autorisation de travail doit être préalablement clôturée pour être archivée.");
        }

        // 2. Visas obligatoires complets
        List<com.ocp.at.entity.Visa> visas = at.getVisas();
        boolean hasVisas = visas != null && !visas.isEmpty();
        checklist.add(com.ocp.at.dto.response.ReadinessCheckItem.builder()
                .code("VISAS_COMPLETE")
                .label("Visas et signatures obligatoires")
                .passed(hasVisas)
                .blocking(true)
                .message(hasVisas ? "Ensemble des visas et signatures réglementaires présents" : "Des visas obligatoires sont manquants")
                .details((visas != null ? visas.size() : 0) + " visa(s) rattaché(s)")
                .build());
        if (!hasVisas) {
            blockingReasons.add("Le dossier ne comporte pas les visas et signatures obligatoires.");
        }

        // 3. Réception des travaux complète
        com.ocp.at.entity.ReceptionTravaux reception = at.getReceptionTravaux();
        boolean hasReception = reception != null && (Boolean.TRUE.equals(reception.getTravauxConformes())
                || reception.getResultatReception() == com.ocp.at.entity.enums.ResultatReception.CONFORME
                || reception.getResultatReception() == com.ocp.at.entity.enums.ResultatReception.CONFORME_AVEC_RESERVES);
        checklist.add(com.ocp.at.dto.response.ReadinessCheckItem.builder()
                .code("RECEPTION_COMPLETE")
                .label("Réception conjointe des travaux")
                .passed(hasReception)
                .blocking(true)
                .message(hasReception ? "Réception conjointe validée et conforme" : "Réception conjointe non validée ou absente")
                .build());
        if (!hasReception) {
            blockingReasons.add("La réception des travaux n'a pas été validée conjointement.");
        }

        // 4. Permis complémentaires conformes
        List<com.ocp.at.entity.Permis> permis = at.getPermis();
        boolean permisOk = permis == null || permis.stream().noneMatch(p -> p.getStatutVerification() == com.ocp.at.entity.enums.StatutPermis.INVALIDE);
        checklist.add(com.ocp.at.dto.response.ReadinessCheckItem.builder()
                .code("PERMITS_CONFORM")
                .label("Conformité des permis associés")
                .passed(permisOk)
                .blocking(true)
                .message(permisOk ? "Tous les permis associés sont conformes" : "Certains permis associés sont invalides")
                .build());
        if (!permisOk) {
            blockingReasons.add("Certains permis rattachés sont invalides.");
        }

        boolean ready = blockingReasons.isEmpty();

        return com.ocp.at.dto.response.ArchiveReadinessResponse.builder()
                .readyForArchive(ready)
                .blockingReasons(blockingReasons)
                .checklist(checklist)
                .atNumero(at.getNumero())
                .statut((stW != null ? stW : st).name())
                .hasCloture(isCloturee)
                .hasVisasComplets(hasVisas)
                .hasReceptionConjointe(hasReception)
                .hasPermisConformes(permisOk)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public com.ocp.at.dto.response.VerificationQrResponse verifyQrCode(String numero) {
        if (numero == null || numero.isBlank()) {
            return com.ocp.at.dto.response.VerificationQrResponse.builder()
                    .valide(false)
                    .message("Numéro d'AT ou d'archive manquant")
                    .build();
        }

        ArchiveAT archive = archiveRepository.findByNumeroArchive(numero)
                .orElseGet(() -> archiveRepository.findTopByAutorisationTravailNumeroOrderByVersionDesc(numero)
                        .orElse(null));

        if (archive == null) {
            return com.ocp.at.dto.response.VerificationQrResponse.builder()
                    .valide(false)
                    .numeroAT(numero)
                    .message("Aucun dossier archivé officiel correspondant à la référence " + numero)
                    .build();
        }

        AutorisationTravail at = archive.getAutorisationTravail();
        String installation = at != null && at.getDemandeIntervention() != null && at.getDemandeIntervention().getEquipement() != null
                ? at.getDemandeIntervention().getEquipement().getNomEquipement() : "Installation OCP";
        String zone = at != null && at.getZoneProprietaire() != null ? at.getZoneProprietaire().getNomZone() : "Zone OCP";
        String archiviste = archive.getArchivePar() != null
                ? archive.getArchivePar().getPrenom() + " " + archive.getArchivePar().getNom() : "Archiviste OCP";

        return com.ocp.at.dto.response.VerificationQrResponse.builder()
                .valide(true)
                .numeroAT(at != null ? at.getNumero() : numero)
                .numeroArchive(archive.getNumeroArchive())
                .statut(archive.getArchiveStatus().name())
                .dateArchivage(archive.getDateArchivage())
                .archiveParNomComplet(archiviste)
                .hashSHA256(archive.getHashSHA256())
                .installation(installation)
                .zone(zone)
                .message("Dossier officiel authentifié et conforme aux référentiels OCP S-HSE-SEC-31.")
                .build();
    }

    // =========================================================================
    // MÉTHODES PRIVÉES
    // =========================================================================

    private AutorisationTravail getEntityById(String id) {
        return atRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autorisation de Travail non trouvée avec l'ID : " + id));
    }

    private String getCurrentUserId() {
        return SecurityUtils.getCurrentUtilisateurId()
                .orElse("SYSTEM");
    }

    private Utilisateur getCurrentUser() {
        String userId = getCurrentUserId();
        if ("SYSTEM".equals(userId)) return null;
        return utilisateurRepository.findByEmail(userId).orElse(null);
    }

    private void creerHistoriqueAT(AutorisationTravail at, TypeActionAT action, StatutAT nouveauStatut, String commentaire) {
        try {
            HistoriqueAT historique = HistoriqueAT.builder()
                    .autorisationTravail(at)
                    .action(action)
                    .ancienStatut(at.getStatut())
                    .nouveauStatut(nouveauStatut)
                    .commentaire(commentaire)
                    .dateAction(LocalDateTime.now())
                    .utilisateur(getCurrentUser())
                    .build();
            historiqueATRepository.save(historique);
        } catch (Exception e) {
            log.warn("Impossible de créer l'historique AT", e);
        }
    }
}