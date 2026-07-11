package com.ocp.at.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service de génération de QR Code.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {

    private final StorageService storageService;

    @Value("${qr.code.size:300}")
    private int qrCodeSize;

    /**
     * Génère un QR Code contenant les informations fournies et le stocke.
     *
     * @param contenu le contenu à encoder dans le QR Code
     * @return le chemin du fichier stocké
     */
    public String generateQrCode(String contenu) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(contenu, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] qrCodeBytes = pngOutputStream.toByteArray();

            // Générer un nom de fichier unique
            String fileName = "qr_" + System.currentTimeMillis() + ".png";
            String uploadDir = "qr";
            String filePath = storageService.storeFile(uploadDir + "/" + fileName, qrCodeBytes, "image/png");

            return filePath;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR Code", e);
            throw new RuntimeException("Erreur lors de la génération du QR Code", e);
        }
    }

    /**
     * Génère un QR Code pour la vérification d'une archive.
     * Le QR Code pointe vers l'URL de vérification de l'archive.
     *
     * @param archiveId l'ID de l'archive
     * @param numeroAT  le numéro de l'AT
     * @param version   la version de l'archive
     * @param hash      le hash SHA-256 de l'archive
     * @return le chemin du fichier QR Code stocké
     */
    public String generateQrCodeForArchive(String archiveId, String numeroAT, String version, String hash) {
        String verificationUrl = "/api/archives/" + archiveId + "/verify";
        String contenu = "archiveId=" + archiveId + "&numeroAT=" + numeroAT + "&version=" + version + "&hash=" + hash;
        return generateQrCode(contenu);
    }

    /**
     * Génère un QR Code et retourne les données de l'image sous forme de tableau d'octets.
     *
     * @param contenu le contenu à encoder dans le QR Code
     * @return les données de l'image PNG du QR Code
     */
    public byte[] generateQrCodeImage(String contenu) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(contenu, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR Code image", e);
            throw new RuntimeException("Erreur lors de la génération du QR Code image", e);
        }
    }
}