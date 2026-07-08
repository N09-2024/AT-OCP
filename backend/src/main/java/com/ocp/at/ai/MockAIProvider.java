package com.ocp.at.ai;

import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.FichierJoint;
import com.ocp.at.entity.Permis;
import com.ocp.at.service.OCRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;

@Component("mockAIProvider")
@Primary
public class MockAIProvider implements IAProvider {

    private final OCRService ocrService;

    @Autowired
    public MockAIProvider(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public AnalyseIA analyserPermis(FichierJoint fichier, Permis permis) {
        // Simulation d'une extraction et analyse
        long startTime = System.currentTimeMillis();
        
        // On récupère le texte via l'OCR Mock (en réalité, il lirait le fichier via le path)
        File dummyFile = new File(fichier.getPath());
        String text = ocrService.extractText(dummyFile);
        
        // Simulation d'un JSON de sortie
        String jsonResult = "{\n" +
                "  \"numero\": \"" + (permis.getNumero() != null ? permis.getNumero() : "PRM-MOCK-001") + "\",\n" +
                "  \"type\": \"" + permis.getType().name() + "\",\n" +
                "  \"entreprise\": \"OCP SA\",\n" +
                "  \"dateEmission\": \"2026-07-01\",\n" +
                "  \"dateExpiration\": \"2026-12-31\",\n" +
                "  \"signaturesPresentes\": true\n" +
                "}";

        long endTime = System.currentTimeMillis();

        return AnalyseIA.builder()
                .dateAnalyse(LocalDateTime.now())
                .ocrText(text)
                .jsonExtraction(jsonResult)
                .tauxConfiance(0.95)
                .tempsExecution(endTime - startTime + 500) // simulation de 500ms d'attente
                .modeleUtilise("MockModel-v1")
                .versionModele("1.0")
                .resultat("SUCCES")
                .commentaireIA("Analyse simulée réussie avec un haut taux de confiance.")
                .permis(permis)
                .build();
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }
}
