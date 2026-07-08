package com.ocp.at.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class OCRService {

    /**
     * Mock OCR extraction of text from a file.
     * Future integration: PaddleOCR / Tesseract
     */
    public String extractText(File file) {
        // Mock implementation
        return "PERMIS DE TRAVAIL OCP\n" +
               "NUMERO: PRM-2026-001\n" +
               "TYPE: TRAVAIL_HAUTEUR\n" +
               "ENTREPRISE: OCP SA\n" +
               "DATE EMISSION: 2026-07-01\n" +
               "DATE EXPIRATION: 2026-12-31\n" +
               "SIGNATURE: OK";
    }

    /**
     * Mock OCR extraction of metadata from a file.
     */
    public Map<String, String> extractMetadata(File file) {
        // Mock implementation
        Map<String, String> meta = new HashMap<>();
        meta.put("author", "OCP");
        meta.put("creationDate", "2026-07-01");
        meta.put("pageCount", "1");
        return meta;
    }
}
