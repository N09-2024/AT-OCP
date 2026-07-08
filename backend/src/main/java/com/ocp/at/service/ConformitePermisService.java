package com.ocp.at.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocp.at.entity.AnalyseIA;
import com.ocp.at.entity.Permis;
import com.ocp.at.entity.enums.StatutPermis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConformitePermisService {

    private final ObjectMapper objectMapper;

    /**
     * Evalue la conformité du permis par rapport à l'analyse IA.
     * @param permis le permis
     * @param analyseIA l'analyse IA
     * @return StatutPermis (CONFORME, NON_CONFORME, EXPIRE, INVALIDE)
     */
    public StatutPermis evaluerConformite(Permis permis, AnalyseIA analyseIA) {
        if (analyseIA == null || analyseIA.getJsonExtraction() == null) {
            return StatutPermis.A_VERIFIER;
        }

        try {
            JsonNode extractedData = objectMapper.readTree(analyseIA.getJsonExtraction());

            // 1. Vérification du numéro
            String extractedNumero = extractedData.path("numero").asText(null);
            if (extractedNumero == null || (permis.getNumero() != null && !permis.getNumero().equals(extractedNumero))) {
                log.warn("Non-conformité: Le numéro extrait ({}) ne correspond pas ou est introuvable.", extractedNumero);
                return StatutPermis.NON_CONFORME;
            }

            // 2. Vérification du type
            String extractedType = extractedData.path("type").asText(null);
            if (extractedType == null || !extractedType.equalsIgnoreCase(permis.getType().name())) {
                log.warn("Non-conformité: Le type extrait ({}) ne correspond pas au type du permis ({}).", extractedType, permis.getType().name());
                return StatutPermis.NON_CONFORME;
            }

            // 3. Vérification des dates
            String dateExpirationStr = extractedData.path("dateExpiration").asText(null);
            if (dateExpirationStr != null && !dateExpirationStr.isEmpty()) {
                LocalDate expiration = LocalDate.parse(dateExpirationStr);
                permis.setDateExpiration(expiration);
                
                if (expiration.isBefore(LocalDate.now())) {
                    log.warn("Non-conformité: Le permis est expiré ({}).", expiration);
                    return StatutPermis.EXPIRE;
                }
            }

            // Si tout est ok
            return StatutPermis.CONFORME;

        } catch (Exception e) {
            log.error("Erreur lors de l'évaluation de la conformité du permis {}: {}", permis.getId(), e.getMessage());
            return StatutPermis.INVALIDE;
        }
    }
}
