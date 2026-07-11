package com.ocp.at.service;

import org.springframework.stereotype.Service;

/**
 * Service de calcul et de vérification du hash SHA-256.
 */
@Service
public class HashService {

    /**
     * Calcule le hash SHA-256 des données fournies.
     *
     * @param data les données à hasher
     * @return le hash SHA-256 en hexadécimal
     */
    public String calculateSHA256(byte[] data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du calcul du SHA-256", e);
        }
    }

    /**
     * Vérifie si le hash calculé correspond au hash attendu.
     *
     * @param data les données originales
     * @param expectedHash le hash attendu en hexadécimal
     * @return true si les hash correspondent, false sinon
     */
    public boolean verifyHash(byte[] data, String expectedHash) {
        String calculatedHash = calculateSHA256(data);
        return calculatedHash.equalsIgnoreCase(expectedHash);
    }
}