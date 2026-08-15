package com.ocp.at.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocp.at.dto.response.PermisDocumentResponse;
import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.PermisDocument;
import com.ocp.at.entity.enums.StatutPermisDocument;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.PermisDocumentRepository;
import com.ocp.at.service.PermisDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation du service de validation IA des permis de travail.
 * Utilise Google Gemini 1.5 Flash (vision) pour analyser les documents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermisDocumentServiceImpl implements PermisDocumentService {

    private final PermisDocumentRepository permisDocumentRepository;
    private final AutorisationTravailRepository atRepository;
    private final com.ocp.at.repository.PermisRepository permisRepository;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${app.storage.location:uploads}")
    private String storageRoot;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    // ------------------------------------------------------------------ //
    //  initialiserPermisRequis
    // ------------------------------------------------------------------ //

    @Override
    @Transactional
    public List<PermisDocumentResponse> initialiserPermisRequis(String atId) {
        AutorisationTravail at = getAT(atId);

        // Extraire les types cochés depuis formPermisIds (JSON array of strings ou UUIDs)
        List<String> typesCochesList = extraireTypesCochesDuFormulaire(at);

        if (typesCochesList.isEmpty()) {
            // Aucun permis coché : supprimer tous les documents existants
            List<PermisDocument> existants = permisDocumentRepository.findByAutorisationTravailId(atId);
            permisDocumentRepository.deleteAll(existants);
            return List.of();
        }

        // Supprimer ceux devenus non cochés
        permisDocumentRepository.deleteByAtIdAndTypeNotIn(atId, typesCochesList);

        // Créer les manquants
        for (String type : typesCochesList) {
            boolean existe = permisDocumentRepository
                    .findByAutorisationTravailIdAndTypePermisAttendu(atId, type)
                    .isPresent();
            if (!existe) {
                PermisDocument pd = PermisDocument.builder()
                        .autorisationTravail(at)
                        .typePermisAttendu(type)
                        .statut(StatutPermisDocument.EN_ATTENTE_UPLOAD)
                        .build();
                permisDocumentRepository.save(pd);
            }
        }

        return getPermisDocuments(atId);
    }

    // ------------------------------------------------------------------ //
    //  uploadPermisDocument
    // ------------------------------------------------------------------ //

    @Override
    @Transactional
    public PermisDocumentResponse uploadPermisDocument(String atId, String typePermis, MultipartFile file) {
        getAT(atId); // vérif existence

        if (file == null || file.isEmpty()) {
            throw new BusinessException("Fichier vide ou absent");
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new BusinessException("Format non supporté. Acceptés : JPG, PNG, PDF");
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            throw new BusinessException("Fichier trop volumineux (max 10 Mo)");
        }

        // Trouver ou créer le PermisDocument
        PermisDocument pd = permisDocumentRepository
                .findByAutorisationTravailIdAndTypePermisAttendu(atId, typePermis)
                .orElseGet(() -> {
                    AutorisationTravail at = getAT(atId);
                    return PermisDocument.builder()
                            .autorisationTravail(at)
                            .typePermisAttendu(typePermis)
                            .statut(StatutPermisDocument.EN_ATTENTE_UPLOAD)
                            .build();
                });

        // Sauvegarder le fichier
        try {
            Path dir = Paths.get(storageRoot, "permis", atId);
            Files.createDirectories(dir);
            String fileName = typePermis.replaceAll("[^a-zA-Z0-9]", "_") + "_"
                    + System.currentTimeMillis() + getExtension(file.getOriginalFilename());
            Path dest = dir.resolve(fileName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            pd.setFilePath(dest.toString());
            pd.setFileOriginalName(file.getOriginalFilename());
            pd.setFileContentType(contentType);
            pd.setStatut(StatutPermisDocument.EN_ATTENTE_ANALYSE);
            pd.setDateUpload(LocalDateTime.now());
            // Réinitialiser les champs IA
            pd.setMotifRejet(null);
            pd.setTypeExtrait(null);
            pd.setDateDebutExtrait(null);
            pd.setDateFinExtrait(null);
            pd.setResponsablesExtraits(null);
            pd.setScoreConfiance(null);
            pd.setCommentaireIA(null);
            pd.setDateAnalyse(null);

        } catch (IOException e) {
            throw new BusinessException("Erreur lors de la sauvegarde du fichier : " + e.getMessage());
        }

        PermisDocument saved = permisDocumentRepository.save(pd);

        // Déclencher l analyse IA de façon asynchrone
        analyserPermisParIA(saved.getId());

        return toResponse(saved);
    }

    // ------------------------------------------------------------------ //
    //  analyserPermisParIA — @Async
    // ------------------------------------------------------------------ //

    @Override
    @Async
    @Transactional
    public void analyserPermisParIA(String permisDocumentId) {
        PermisDocument pd = permisDocumentRepository.findById(permisDocumentId)
                .orElse(null);
        if (pd == null) {
            log.warn("PermisDocument {} introuvable pour analyse IA", permisDocumentId);
            return;
        }
        if (pd.getFilePath() == null) {
            log.warn("PermisDocument {} sans filePath — analyse abandonnée", permisDocumentId);
            pd.setStatut(StatutPermisDocument.REJETE);
            pd.setMotifRejet("Aucun fichier associé");
            permisDocumentRepository.save(pd);
            syncPermisStatutVerification(pd);
            return;
        }

        try {
            // 1. Charger et convertir en image base64
            String base64Image;
            String imageMimeType;

            File file = new File(pd.getFilePath());
            if (!file.exists()) {
                log.error("Fichier permis introuvable : {}", pd.getFilePath());
                pd.setStatut(StatutPermisDocument.REJETE);
                pd.setMotifRejet("Fichier introuvable sur le serveur");
                permisDocumentRepository.save(pd);
                syncPermisStatutVerification(pd);
                return;
            }

            String ct = pd.getFileContentType() != null ? pd.getFileContentType() : "";
            if (ct.equals("application/pdf")) {
                // Convertir page 1 du PDF en PNG
                try (PDDocument doc = Loader.loadPDF(file)) {
                    PDFRenderer renderer = new PDFRenderer(doc);
                    BufferedImage img = renderer.renderImageWithDPI(0, 150);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", baos);
                    base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                    imageMimeType = "image/png";
                }
            } else {
                // Lire l image directement
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
                imageMimeType = ct.isEmpty() ? "image/jpeg" : ct;
            }

            // 2. Construire le prompt
            AutorisationTravail at = pd.getAutorisationTravail();
            String dateIntervention = at.getDateDebut() != null ? at.getDateDebut().toString() : "non précisée";
            String zone = at.getZoneProprietaire() != null ? at.getZoneProprietaire().getNomZone() : "non précisée";
            String entreprise = at.getEntreprisesIntervenantes() != null ? at.getEntreprisesIntervenantes() : "non précisée";

            String userPrompt = "Analyse ce document de permis de travail.\n\n"
                    + "Contexte de l'AT :\n"
                    + "- Type de permis attendu : " + pd.getTypePermisAttendu() + "\n"
                    + "- Date intervention : " + dateIntervention + "\n"
                    + "- Zone : " + zone + "\n"
                    + "- Entreprise : " + entreprise + "\n\n"
                    + "Retourne ce JSON exact sans texte avant ni après, sans markdown :\n"
                    + "{\n"
                    + "  \"typeExtrait\": \"type identifié dans le document\",\n"
                    + "  \"dateDebutExtrait\": \"dd/MM/yyyy ou null\",\n"
                    + "  \"dateFinExtrait\": \"dd/MM/yyyy ou null\",\n"
                    + "  \"responsablesExtraits\": \"noms et roles des signataires\",\n"
                    + "  \"estValide\": true ou false,\n"
                    + "  \"scoreConfiance\": 0.0 à 1.0,\n"
                    + "  \"motifRejet\": \"explication si estValide=false, sinon null\",\n"
                    + "  \"commentaire\": \"résumé 1-2 phrases\"\n"
                    + "}\n\n"
                    + "Critères — estValide = false si l'un échoue :\n"
                    + "1. Le type du document correspond au type attendu : " + pd.getTypePermisAttendu() + "\n"
                    + "2. Le document est lisible et non tronqué\n"
                    + "3. Au moins une signature ou visa visible\n"
                    + "4. Les dates couvrent la date d'intervention : " + dateIntervention + "\n"
                    + "5. Le document n'est pas un formulaire vierge\n";

            // 3. Appel Gemini API (ou mock si clé absente)
            String jsonResponse;
            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                log.warn("GEMINI_API_KEY non configurée — fallback mock pour PermisDocument {}", permisDocumentId);
                jsonResponse = buildMockResponse(pd.getTypePermisAttendu());
            } else {
                jsonResponse = callGeminiVision(base64Image, imageMimeType, userPrompt);
            }

            // 4. Parser et persister le résultat
            parseEtPersister(pd, jsonResponse);

        } catch (Exception e) {
            log.error("Erreur analyse IA PermisDocument {} : {}", permisDocumentId, e.getMessage(), e);
            pd.setStatut(StatutPermisDocument.REJETE);
            pd.setMotifRejet("Erreur lors de l analyse : " + e.getMessage());
            pd.setDateAnalyse(LocalDateTime.now());
            permisDocumentRepository.save(pd);
            syncPermisStatutVerification(pd);
        }
    }

    // ------------------------------------------------------------------ //
    //  tousPermisValides
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public boolean tousPermisValides(String atId) {
        List<PermisDocument> docs = permisDocumentRepository.findByAutorisationTravailId(atId);
        if (docs.isEmpty()) return true; // pas de permis requis
        return docs.stream().allMatch(d -> d.getStatut() == StatutPermisDocument.VALIDE);
    }

    @Override
    @Transactional
    public void resynchroniserStatutsPermis(String atId) {
        List<PermisDocument> docs = permisDocumentRepository.findByAutorisationTravailId(atId);
        for (PermisDocument pd : docs) {
            syncPermisStatutVerification(pd);
        }
    }

    // ------------------------------------------------------------------ //
    //  getPermisDocuments / relancerAnalyse
    // ------------------------------------------------------------------ //

    @Override
    @Transactional(readOnly = true)
    public List<PermisDocumentResponse> getPermisDocuments(String atId) {
        return permisDocumentRepository.findByAutorisationTravailId(atId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PermisDocumentResponse relancerAnalyse(String permisDocumentId) {
        PermisDocument pd = permisDocumentRepository.findById(permisDocumentId)
                .orElseThrow(() -> new ResourceNotFoundException("PermisDocument introuvable : " + permisDocumentId));
        if (pd.getFilePath() == null) {
            throw new BusinessException("Aucun fichier uploadé — impossible de relancer l analyse");
        }
        pd.setStatut(StatutPermisDocument.EN_ATTENTE_ANALYSE);
        pd.setMotifRejet(null);
        pd.setDateAnalyse(null);
        PermisDocument saved = permisDocumentRepository.save(pd);
        analyserPermisParIA(saved.getId());
        return toResponse(saved);
    }

    // ================================================================== //
    //  Méthodes privées
    // ================================================================== //

    private String callGeminiVision(String base64Image, String mimeType, String userPrompt) {
        RestTemplate restTemplate = new RestTemplate();
        String model = (geminiModel != null && !geminiModel.isBlank())
                ? geminiModel.trim().replace("models/", "")
                : "gemini-2.5-flash";
        String url = String.format(GEMINI_API_URL, model, geminiApiKey);

        Map<String, Object> imagePart = Map.of(
            "inlineData", Map.of("mimeType", mimeType, "data", base64Image)
        );
        Map<String, Object> textPart = Map.of("text", userPrompt);
        Map<String, Object> systemInstruction = Map.of(
            "parts", List.of(Map.of("text",
                "Tu es un agent expert en sécurité industrielle OCP, spécialisé dans la validation " +
                "des permis de travail selon le standard S-HSE-SEC-31. " +
                "Retourne UNIQUEMENT un objet JSON valide, sans texte avant ni après, sans markdown."))
        );
        Map<String, Object> contents = Map.of(
            "parts", List.of(imagePart, textPart)
        );
        Map<String, Object> body = Map.of(
            "system_instruction", systemInstruction,
            "contents", List.of(contents),
            "generationConfig", Map.of(
                "temperature", 0.1,
                "maxOutputTokens", 2048,
                "responseMimeType", "application/json"
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Gemini API error: " + response.getStatusCode());
        }

        // Extraire le texte de la réponse Gemini
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode candidate = root.path("candidates").get(0);
            String finishReason = candidate.path("finishReason").asText("");
            if ("MAX_TOKENS".equals(finishReason)) {
                log.warn("Réponse Gemini tronquée (MAX_TOKENS) — envisager d'augmenter maxOutputTokens. Body brut : {}",
                        response.getBody());
            }
            String text = candidate.path("content").path("parts").get(0).path("text").asText();
            log.debug("Réponse brute Gemini : {}", text);
            return text;
        } catch (Exception e) {
            log.error("Corps de réponse Gemini brut (échec extraction) : {}", response.getBody());
            throw new RuntimeException("Impossible de parser la réponse Gemini : " + e.getMessage());
        }
    }

    private void parseEtPersister(PermisDocument pd, String jsonText) {
        try {
            // Nettoyer le JSON (enlever les balises markdown si présentes)
            String cleaned = jsonText.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
            }
            // Filet de sécurité : si Gemini ajoute du texte avant/après malgré la consigne,
            // on isole le premier bloc JSON valide { ... }
            int firstBrace = cleaned.indexOf('{');
            int lastBrace = cleaned.lastIndexOf('}');
            if (firstBrace > 0 || (lastBrace >= 0 && lastBrace < cleaned.length() - 1)) {
                if (firstBrace >= 0 && lastBrace > firstBrace) {
                    cleaned = cleaned.substring(firstBrace, lastBrace + 1);
                }
            }

            JsonNode json = objectMapper.readTree(cleaned);

            pd.setTypeExtrait(json.path("typeExtrait").asText(null));
            pd.setDateDebutExtrait(nullIfEmpty(json.path("dateDebutExtrait").asText(null)));
            pd.setDateFinExtrait(nullIfEmpty(json.path("dateFinExtrait").asText(null)));
            pd.setResponsablesExtraits(json.path("responsablesExtraits").asText(null));
            pd.setScoreConfiance(json.path("scoreConfiance").asDouble(0.5));
            pd.setCommentaireIA(json.path("commentaire").asText(null));
            pd.setDateAnalyse(LocalDateTime.now());

            boolean estValide = json.path("estValide").asBoolean(false);
            if (estValide) {
                pd.setStatut(StatutPermisDocument.VALIDE);
                pd.setMotifRejet(null);
            } else {
                pd.setStatut(StatutPermisDocument.REJETE);
                pd.setMotifRejet(json.path("motifRejet").asText("Document non conforme"));
            }

        } catch (Exception e) {
            log.error("Erreur parsing réponse IA : {}. Texte brut reçu : {}", e.getMessage(), jsonText);
            pd.setStatut(StatutPermisDocument.REJETE);
            pd.setMotifRejet("Réponse IA non parsable — veuillez re-soumettre le document");
            pd.setDateAnalyse(LocalDateTime.now());
        }
        permisDocumentRepository.save(pd);
        syncPermisStatutVerification(pd);
    }

    // ------------------------------------------------------------------ //
    //  syncPermisStatutVerification — répercute le résultat de l'agent IA
    //  (PermisDocument.statut) sur l'entité Permis.statutVerification,
    //  qui est la valeur réellement contrôlée par la garde de soumission
    //  (AutorisationTravailServiceImpl.soumettreAT). Sans cette synchro,
    //  un document validé par l'IA (PermisDocument.VALIDE) ne débloque
    //  jamais la soumission, car Permis.statutVerification reste à
    //  A_VERIFIER indéfiniment.
    // ------------------------------------------------------------------ //
    private void syncPermisStatutVerification(PermisDocument pd) {
        try {
            AutorisationTravail at = pd.getAutorisationTravail();
            if (at == null || pd.getTypePermisAttendu() == null) return;

            java.util.List<com.ocp.at.entity.Permis> permisList =
                    permisRepository.findByAutorisationTravailId(at.getId());

            for (com.ocp.at.entity.Permis p : permisList) {
                if (p.getTypePermis() == null) continue;
                boolean sameType = pd.getTypePermisAttendu().equals(p.getTypePermis().getId())
                        || pd.getTypePermisAttendu().equalsIgnoreCase(p.getTypePermis().getNom());
                if (!sameType) continue;

                if (pd.getStatut() == StatutPermisDocument.VALIDE) {
                    p.setStatutVerification(com.ocp.at.entity.enums.StatutPermis.CONFORME);
                    p.setCommentaire(pd.getCommentaireIA());
                } else if (pd.getStatut() == StatutPermisDocument.REJETE) {
                    p.setStatutVerification(com.ocp.at.entity.enums.StatutPermis.NON_CONFORME);
                    p.setCommentaire(pd.getMotifRejet());
                } else {
                    p.setStatutVerification(com.ocp.at.entity.enums.StatutPermis.A_VERIFIER);
                }
                permisRepository.save(p);
            }
        } catch (Exception e) {
            log.warn("Sync Permis.statutVerification depuis PermisDocument {} : {}", pd.getId(), e.getMessage());
        }
    }

    private String buildMockResponse(String typeAttendu) {
        return "{\n"
                + "  \"typeExtrait\": \"" + typeAttendu + " (simulation)\",\n"
                + "  \"dateDebutExtrait\": null,\n"
                + "  \"dateFinExtrait\": null,\n"
                + "  \"responsablesExtraits\": \"Signataire simulé — Mode hors-ligne\",\n"
                + "  \"estValide\": true,\n"
                + "  \"scoreConfiance\": 0.85,\n"
                + "  \"motifRejet\": null,\n"
                + "  \"commentaire\": \"Validation simulée (GEMINI_API_KEY non configurée). À configurer en production.\"\n"
                + "}\n";
    }

    private List<String> extraireTypesCochesDuFormulaire(AutorisationTravail at) {
        // formPermisIds peut être: ["uuid1","uuid2"] ou ["PERMIS_FEU","ESPACE_CONFINE"]
        String raw = at.getFormPermisIds();
        if (raw == null || raw.isBlank() || raw.equals("[]") || raw.equals("null")) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.isArray()) {
                List<String> types = new ArrayList<>();
                node.forEach(n -> types.add(n.asText()));
                return types;
            }
        } catch (Exception e) {
            log.warn("formPermisIds non parsable : {}", raw);
        }
        return List.of();
    }

    private AutorisationTravail getAT(String atId) {
        return atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("AutorisationTravail introuvable : " + atId));
    }

    private PermisDocumentResponse toResponse(PermisDocument pd) {
        return PermisDocumentResponse.builder()
                .id(pd.getId())
                .atId(pd.getAutorisationTravail() != null ? pd.getAutorisationTravail().getId() : null)
                .typePermisAttendu(pd.getTypePermisAttendu())
                .fileOriginalName(pd.getFileOriginalName())
                .fileContentType(pd.getFileContentType())
                .statut(pd.getStatut())
                .dateUpload(pd.getDateUpload())
                .dateAnalyse(pd.getDateAnalyse())
                .typeExtrait(pd.getTypeExtrait())
                .dateDebutExtrait(pd.getDateDebutExtrait())
                .dateFinExtrait(pd.getDateFinExtrait())
                .responsablesExtraits(pd.getResponsablesExtraits())
                .motifRejet(pd.getMotifRejet())
                .scoreConfiance(pd.getScoreConfiance())
                .commentaireIA(pd.getCommentaireIA())
                .build();
    }

    private String getExtension(String filename) {
        if (filename == null) return ".bin";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx) : ".bin";
    }

    private String nullIfEmpty(String val) {
        if (val == null || val.isBlank() || val.equalsIgnoreCase("null")) return null;
        return val;
    }
}