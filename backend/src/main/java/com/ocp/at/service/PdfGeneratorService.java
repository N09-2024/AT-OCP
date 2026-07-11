package com.ocp.at.service;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.ReceptionTravaux;

import java.io.ByteArrayOutputStream;

public interface PdfGeneratorService {

    byte[] generateATPdf(AutorisationTravail autorisationTravail);

    byte[] generateReceptionPdf(ReceptionTravaux receptionTravaux);

    byte[] generatePermisPdf(AutorisationTravail autorisationTravail);

    byte[] generateCompleteDossierPdf(AutorisationTravail autorisationTravail);

    String calculateSHA256(byte[] data);
}
