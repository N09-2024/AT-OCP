package com.ocp.at.service.impl;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.service.PdfGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

    @Override
    public byte[] generateATPdf(AutorisationTravail autorisationTravail) {
        log.info("Génération du PDF pour l'AT {}", autorisationTravail.getNumero());
        
        // Mock simple de la génération d'un PDF
        // Dans une version finale, on utiliserait iText, Apache PDFBox ou JasperReports
        
        String mockPdfContent = String.format(
            "%%PDF-1.4\n" +
            "1 0 obj\n" +
            "<< /Type /Catalog /Pages 2 0 R >>\n" +
            "endobj\n" +
            "2 0 obj\n" +
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
            "endobj\n" +
            "3 0 obj\n" +
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >>\n" +
            "endobj\n" +
            "4 0 obj\n" +
            "<< /Length 0 >>\n" +
            "stream\n" +
            "BT\n" +
            "/F1 12 Tf\n" +
            "72 712 Td\n" +
            "(AUTORISATION DE TRAVAIL OCP: %s) Tj\n" +
            "0 -15 Td\n" +
            "(Statut: %s - Version: %s) Tj\n" +
            "0 -15 Td\n" +
            "(Objet: %s) Tj\n" +
            "ET\n" +
            "endstream\n" +
            "endobj\n" +
            "xref\n" +
            "0 5\n" +
            "0000000000 65535 f \n" +
            "0000000009 00000 n \n" +
            "0000000058 00000 n \n" +
            "0000000115 00000 n \n" +
            "0000000204 00000 n \n" +
            "trailer\n" +
            "<< /Size 5 /Root 1 0 R >>\n" +
            "startxref\n" +
            "300\n" +
            "%%EOF",
            autorisationTravail.getNumero(),
            autorisationTravail.getStatut(),
            autorisationTravail.getVersion(),
            autorisationTravail.getObjet()
        );

        // PREPARATION MODULE 10 : Le vrai générateur PDF (Jasper/PDFBox) devra lire 
        // l'image de chaque visa validé :
        /*
        if (autorisationTravail.getVisas() != null) {
            for (Visa visa : autorisationTravail.getVisas()) {
                if (visa.getSignaturePath() != null) {
                    // Charger l'image depuis : storageService.loadSignature(visa.getSignaturePath())
                    // Intégrer l'image PNG dans le PDF
                }
            }
        }
        */

        return mockPdfContent.getBytes(StandardCharsets.UTF_8);
    }
}
