package com.ocp.at.service;

import com.ocp.at.entity.AutorisationTravail;

public interface PdfGeneratorService {
    byte[] generateATPdf(AutorisationTravail autorisationTravail);
}
