package com.ocp.at.service;

import com.ocp.at.dto.request.AutoSaveRequest;
import com.ocp.at.dto.request.RefusRequest;
import com.ocp.at.dto.request.TransferLockRequest;
import com.ocp.at.dto.response.AutorisationTravailResponse;
import com.ocp.at.dto.response.HistoriqueATResponse;
import com.ocp.at.dto.response.VisaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AutorisationTravailService {

    AutorisationTravailResponse createFromDocument(String documentId, String typeDocument);

    Page<AutorisationTravailResponse> findAll(Pageable pageable);

    AutorisationTravailResponse findById(String id);

    AutorisationTravailResponse autoSave(String id, AutoSaveRequest request);

    void prendreVerrou(String id);

    void libererVerrou(String id);

    void transfererVerrou(String id, TransferLockRequest request);

    AutorisationTravailResponse soumettreAT(String id);

    AutorisationTravailResponse validerAT(String id);

    AutorisationTravailResponse refuserAT(String id, RefusRequest request);

    AutorisationTravailResponse renouvelerAT(String id);

    AutorisationTravailResponse annulerAT(String id);

    AutorisationTravailResponse cloturerAT(String id);

    List<HistoriqueATResponse> getHistorique(String id);

    List<VisaResponse> getVisas(String id);
}
