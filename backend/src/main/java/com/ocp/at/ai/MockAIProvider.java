package com.ocp.at.ai;

import com.ocp.at.dto.response.AnalyseInterventionIAResponse;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import com.ocp.at.service.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provider mock déterministe (PFE / offline).
 * Remplaçable par LangChainProvider / CrewAIProvider branchés sur FastAPI.
 */
@Component("mockAIProvider")
public class MockAIProvider implements IAProvider {

    private final OCRService ocrService;

    @Autowired
    public MockAIProvider(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public AnalyseIA analyserPermis(FichierJoint fichier, Permis permis) {
        long start = System.currentTimeMillis();
        File dummyFile = new File(fichier.getPath() != null ? fichier.getPath() : ".");
        String text = ocrService.extractText(dummyFile);
        String num = permis.getNumero() != null ? permis.getNumero() : "PRM-MOCK";
        String typeNom = permis.getTypePermis() != null ? permis.getTypePermis().getNom() : "";
        String json = "{\"numero\":\"" + num + "\",\"type\":\"" + typeNom + "\",\"signaturesPresentes\":true}";
        return AnalyseIA.builder()
                .dateAnalyse(LocalDateTime.now())
                .ocrText(text)
                .jsonExtraction(json)
                .tauxConfiance(0.9)
                .tempsExecution(System.currentTimeMillis() - start + 200)
                .modeleUtilise("MockModel-v1")
                .versionModele("1.0")
                .resultat("SUCCES")
                .commentaireIA("Analyse permis simulée - à remplacer par LangChain/CrewAI.")
                .permis(permis)
                .build();
    }

    @Override
    public AnalyseInterventionIAResponse analyserIntervention(String description) {
        String d = description != null ? description.toLowerCase(Locale.ROOT) : "";
        List<String> risques = new ArrayList<>();
        List<String> mesures = new ArrayList<>();
        List<String> epis = new ArrayList<>();
        List<String> permis = new ArrayList<>();

        if (d.contains("hauteur") || d.contains("échelle") || d.contains("nacelle") || d.contains("échafaud") || d.contains("5 m") || d.contains("mètre")) {
            risques.add("Travail en hauteur");
            epis.add("Harnais de sécurité");
            epis.add("Casque");
            permis.add("Permis pour travail en hauteur");
            mesures.add("Balisage");
        }
        if (d.contains("acide") || d.contains("chimique") || d.contains("sulfur") || d.contains("corrosi")) {
            risques.add("Produits chimiques");
            epis.add("Gants antiacides");
            epis.add("Lunettes étanches");
            epis.add("Tenue antiacide");
            mesures.add("Vidange de l'équipement et ses circuits");
            mesures.add("Douche / rince-œil à proximité");
        }
        if (d.contains("feu") || d.contains("soud") || d.contains("meulage") || d.contains("flamme")) {
            risques.add("Produits inflammables");
            permis.add("Permis de feu");
            epis.add("Casque soudure");
            mesures.add("Extincteur à proximité");
        }
        if (d.contains("électri") || d.contains("consign") || d.contains("vanne") || d.contains("tension")) {
            risques.add("Electricité");
            mesures.add("Consignation des énergies");
            permis.add("Plan de consignation");
        }
        if (d.contains("confin") || d.contains("cuve") || d.contains("fosse")) {
            risques.add("Espaces confinés");
            permis.add("Permis pour espace confiné");
            epis.add("ARI");
            mesures.add("Ventilation");
        }
        if (d.contains("fouille") || d.contains("tranchée") || d.contains("enterr")) {
            risques.add("Proximité aux réseaux enterrés");
            permis.add("Permis de fouille");
        }
        if (risques.isEmpty()) {
            risques.add("Co-activité");
            mesures.add("Balisage");
            epis.add("Casque");
            epis.add("Bottes de sécurité");
        }

        String rapport = "Analyse mock (LangChain/CrewAI à brancher) : "
                + risques.size() + " risque(s), "
                + permis.size() + " permis suggéré(s). "
                + "À valider par le CEEP sur le formulaire F-HSE-SEC-31-04.";

        return AnalyseInterventionIAResponse.builder()
                .risques(risques)
                .mesures(mesures)
                .epis(epis)
                .permis(permis)
                .rapport(rapport)
                .complet(true)
                .alertes(List.of())
                .provider(getProviderName())
                .tauxConfiance(0.75)
                .build();
    }

    @Override
    public AnalyseInterventionIAResponse controlerDossier(
            String description, boolean visiteFaite, int nbRisques, int nbMesures,
            int nbEpis, int nbPermis, boolean sectionFRenseignee) {
        List<String> alertes = new ArrayList<>();
        if (description == null || description.isBlank()) {
            alertes.add("Description de l'intervention vide (en-tête F-HSE-SEC-31-04)");
        }
        if (!visiteFaite) {
            alertes.add("Visite préalable chantier non enregistrée (§8.2)");
        }
        if (nbRisques == 0) {
            alertes.add("Section A : aucun risque coché");
        }
        if (nbMesures == 0) {
            alertes.add("Section B : aucune mesure de préparation");
        }
        if (nbEpis == 0) {
            alertes.add("Section D : aucun EPI spécifique");
        }
        if (!sectionFRenseignee) {
            alertes.add("Section F : mesures exécutant / mode opératoire non renseignées");
        }
        // permis : informatif seulement si risques élévés sans permis
        if (nbPermis == 0 && nbRisques > 2) {
            alertes.add("Section E : aucun permis alors que plusieurs risques sont identifiés - vérifier §11");
        }

        AnalyseInterventionIAResponse suggestions = analyserIntervention(description);
        return AnalyseInterventionIAResponse.builder()
                .risques(suggestions.getRisques())
                .mesures(suggestions.getMesures())
                .epis(suggestions.getEpis())
                .permis(suggestions.getPermis())
                .rapport("Contrôle dossier (agent Contrôleur AT) : "
                        + (alertes.isEmpty() ? "OK pour soumission humaine." : alertes.size() + " alerte(s)."))
                .alertes(alertes)
                .complet(alertes.isEmpty())
                .provider(getProviderName())
                .tauxConfiance(0.8)
                .build();
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }
}
