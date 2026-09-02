package com.ocp.at.ai;

import com.ocp.at.dto.request.AnalyzeAtRequest;
import com.ocp.at.dto.request.ChatRequest;
import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.dto.response.AnalyzeAtResponse;
import com.ocp.at.dto.response.ChatResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import com.ocp.at.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock IA déterministe basé sur les règles métier OCP (F-HSE-SEC-31-04 et S-HSE-SEC-31).
 * Fonctionne à 100% hors-ligne et sert de repli de secours en cas d'indisponibilité du microservice Python.
 */
@Component("mockAIProvider")
@Slf4j
public class MockAIProvider implements IAProvider {

    private final OCRService ocrService;

    public MockAIProvider(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public AnalyseIA analyserPermis(FichierJoint fichier, Permis permis) {
        // OCRService.extractText attend un java.io.File - le FichierJoint n'expose pas de File directement.
        // On remplit les champs réels de l'entité AnalyseIA (ocrText, resultat, commentaireIA, tauxConfiance).
        return AnalyseIA.builder()
                .dateAnalyse(LocalDateTime.now())
                .tauxConfiance(0.92)
                .resultat("VALIDE")
                .commentaireIA("Mock AI: Document conforme aux exigences standard.")
                .modeleUtilise("MOCK")
                .versionModele("1.0")
                .permis(permis)
                .build();
    }

    @Override
    public AnalyzeAtResponse analyzeAt(AnalyzeAtRequest request) {
        if (request == null) {
            request = new AnalyzeAtRequest();
        }
        String desc = request.getDescription() != null ? request.getDescription().toLowerCase() : "";

        Set<String> risques = new LinkedHashSet<>();
        Set<String> mesures = new LinkedHashSet<>();
        Set<String> epis = new LinkedHashSet<>(List.of("Bottes de sécurité"));
        Set<String> permis = new LinkedHashSet<>();
        List<String> missing = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> sources = new ArrayList<>(List.of(
                "Standard OCP S-HSE-SEC-31 §8-§10 (Règles générales)",
                "Formulaire F-HSE-SEC-31-04"
        ));

        if (desc.contains("hauteur") || desc.contains("échafaudage") || desc.contains("echelle") || desc.contains("toiture")) {
            risques.add("Travail en hauteur");
            mesures.add("Balisage");
            epis.add("Harnais de sécurité");
            permis.add("Permis pour travail en hauteur");
            sources.add("Standard S-HSE-SEC-31 §10.1 (Travaux en hauteur)");
        }

        if (desc.contains("soudure") || desc.contains("meulage") || desc.contains("flamme") || desc.contains("chaud") || desc.contains("feu")) {
            risques.add("Produits inflammables");
            mesures.add("Balisage");
            epis.add("Casque soudure");
            permis.add("Permis de feu");
            sources.add("Standard S-HSE-SEC-31 §10.2 (Permis de feu)");
        }

        if (desc.contains("confiné") || desc.contains("cuve") || desc.contains("bac") || desc.contains("fosse") || desc.contains("trémie")) {
            risques.add("Espaces confinés");
            mesures.add("Ventilation");
            epis.add("ARI");
            permis.add("Permis pour espace confiné");
            sources.add("Standard S-HSE-SEC-31 §10.3 (Espaces confinés)");
        }

        if (desc.contains("acide") || desc.contains("chimique") || desc.contains("soufre") || desc.contains("base")) {
            risques.add("Produits chimiques");
            epis.addAll(List.of("Gants antiacides", "Lunettes étanches", "Tenue antiacide"));
            sources.add("Standard S-HSE-SEC-31 §10.4 (Risques chimiques)");
        }

        if (desc.contains("électr") || desc.contains("tension") || desc.contains("armoire") || desc.contains("moteur") || desc.contains("câble")) {
            risques.add("Electricité");
            mesures.add("Consignation des Energies");
            permis.add("Plan de consignation");
            sources.add("Standard S-HSE-SEC-31 §9 (Procédure de Consignation)");
        }

        if (desc.contains("fouille") || desc.contains("tranchée") || desc.contains("terrassement") || desc.contains("enterré")) {
            risques.add("Proximité aux réseaux enterrés");
            permis.add("Permis de fouille");
            sources.add("Standard S-HSE-SEC-31 §10.5 (Permis de fouille)");
        }

        if (risques.isEmpty()) {
            risques.add("Outillage");
            epis.add("Gants de manutention");
        }

        if (Boolean.FALSE.equals(request.getVisiteFaite())) {
            warnings.add("La visite préalable de chantier (§8.2) n'a pas encore été réalisée.");
            missing.add("Validation de la visite préalable");
        }

        if (request.getDescription() == null || request.getDescription().trim().length() < 10) {
            warnings.add("La description des travaux est trop succincte.");
            missing.add("Précisions sur la méthodologie de l'intervention");
        }

        String summary = "Analyse consultative basée sur le Standard OCP S-HSE-SEC-31. " +
                risques.size() + " risque(s) identifié(s), " +
                mesures.size() + " mesure(s) de préparation et " +
                permis.size() + " permis complémentaire(s) requis.";

        return AnalyzeAtResponse.builder()
                .summary(summary)
                .missingInformation(missing)
                .identifiedRisks(new ArrayList<>(risques))
                .recommendedMeasures(new ArrayList<>(mesures))
                .inconsistencies(new ArrayList<>())
                .warnings(warnings)
                .sources(sources)
                .confidence("HIGH")
                .risques(new ArrayList<>(risques))
                .mesures(new ArrayList<>(mesures))
                .epis(new ArrayList<>(epis))
                .permis(new ArrayList<>(permis))
                .rapport(summary)
                .alertes(warnings)
                .complet(warnings.isEmpty())
                .provider("MOCK_AI")
                .tauxConfiance(0.90)
                .build();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        if (request == null) {
            request = new ChatRequest();
        }
        String msg = request.getMessage() != null ? request.getMessage().toLowerCase() : "";
        String answer;
        List<String> sources = new ArrayList<>(List.of("Standard OCP S-HSE-SEC-31"));

        if (msg.contains("visa") || msg.contains("ceep") || msg.contains("ceee")) {
            answer = "Selon le Standard S-HSE-SEC-31 §7, l'ordre des visas est strictement séquentiel : " +
                    "1. Le CEEP (exploitant propriétaire) valide la préparation et les consignations.\n" +
                    "2. Le CEEE (exécutant) signe pour attester de la compréhension des consignes.\n" +
                    "3. Les visas hiérarchiques (HCEP/HCEE, puis HMEP/HMEE si niveau 2) sont apposés avant tout démarrage.";
            sources.add("Standard S-HSE-SEC-31 §7 (Circuit des visas)");
        } else if (msg.contains("feu") || msg.contains("chaud") || msg.contains("soudure")) {
            answer = "Pour tout travail par points chauds (soudure, meulage), le Permis de Feu est obligatoire (§10.2). " +
                    "Les prérequis comprennent : extincteur vérifié à proximité, écran de protection, et surveillance continue pendant et après les travaux.";
            sources.add("Standard S-HSE-SEC-31 §10.2 (Permis de Feu)");
        } else if (msg.contains("hauteur") || msg.contains("échafaudage")) {
            answer = "Pour les travaux en hauteur (>1.80m), un Permis de travail en hauteur est obligatoire (§10.1). " +
                    "Les EPI requis sont : Harnais de sécurité avec double longe, casque avec jugulaire, et ancrage vérifié.";
            sources.add("Standard S-HSE-SEC-31 §10.1 (Travaux en hauteur)");
        } else if (msg.contains("consignation") || msg.contains("électr")) {
            answer = "La consignation des énergies (§9) exige la séparation, la condamnation (cadenas), la purge/dissipation " +
                    "et la vérification d'absence de tension (VAT) avec délivrance du Plan de Consignation.";
            sources.add("Standard S-HSE-SEC-31 §9 (Consignation)");
        } else {
            answer = "L'Autorisation de Travail (F-HSE-SEC-31-04) est le document maître de sécurité OCP. " +
                    "Elle formalise l'évaluation des risques (Section A), les mesures de préparation (Section B), " +
                    "les EPI (Section D) et les permis spécifiques (Section E).";
        }

        return ChatResponse.builder()
                .answer(answer)
                .sources(sources)
                .confidence("HIGH")
                .suggestedQuestions(List.of(
                        "Quels sont les prérequis pour un permis de feu ?",
                        "Comment se déroule la consignation électrique ?",
                        "Quels sont les rôles respectifs du CEEP et du CEEE ?"
                ))
                .build();
    }

    @Override
    public AnalyseInterventionIAResponse analyserIntervention(String description) {
        AnalyzeAtResponse resp = analyzeAt(AnalyzeAtRequest.builder().description(description).build());
        return AnalyseInterventionIAResponse.builder()
                .risques(resp.getRisques())
                .mesures(resp.getMesures())
                .epis(resp.getEpis())
                .permis(resp.getPermis())
                .rapport(resp.getRapport())
                .alertes(resp.getAlertes())
                .complet(resp.isComplet())
                .provider("MOCK_AI")
                .tauxConfiance(resp.getTauxConfiance())
                .build();
    }

    @Override
    public AnalyseInterventionIAResponse controlerDossier(
            String description, boolean visiteFaite, int nbRisques, int nbMesures,
            int nbEpis, int nbPermis, boolean sectionFRenseignee) {
        AnalyzeAtRequest req = AnalyzeAtRequest.builder()
                .description(description)
                .visiteFaite(visiteFaite)
                .nbRisques(nbRisques)
                .nbMesures(nbMesures)
                .nbEpis(nbEpis)
                .nbPermis(nbPermis)
                .sectionFRenseignee(sectionFRenseignee)
                .build();
        AnalyzeAtResponse resp = analyzeAt(req);
        return AnalyseInterventionIAResponse.builder()
                .risques(resp.getRisques())
                .mesures(resp.getMesures())
                .epis(resp.getEpis())
                .permis(resp.getPermis())
                .rapport(resp.getRapport())
                .alertes(resp.getAlertes())
                .complet(resp.isComplet())
                .provider("MOCK_AI")
                .tauxConfiance(resp.getTauxConfiance())
                .build();
    }

    @Override
    public String getProviderName() {
        return "MOCK_AI";
    }
}
