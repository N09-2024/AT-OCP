package com.ocp.at.service;

import com.ocp.at.dto.request.ArchiveRequest;
import com.ocp.at.dto.request.ArchiveSearchRequest;
import com.ocp.at.dto.response.ArchiveResponse;
import com.ocp.at.dto.response.ArchiveSearchResponse;
import com.ocp.at.dto.response.PdfExportResponse;
import org.springframework.data.domain.Page;

/**
 * Service pour la gestion des archives d'autorisations de travail.
 */
public interface ArchiveService {

    /**
     * Exporte une AT clôturée en PDF et retourne les informations du PDF généré.
     *
     * @param atId l'ID de l'autorisation de travail
     * @return la réponse contenant les détails du PDF exporté
     */
    PdfExportResponse exportAT(String atId);

    /**
     * Archive une AT clôturée (génère le PDF, calcule le hash, génère le QR Code, stocke le tout).
     *
     * @param atId l'ID de l'autorisation de travail
     * @return la réponse contenant les détails de l'archivage
     */
    ArchiveResponse archiverAT(String atId);

    /**
     * Récupère une archive par son ID.
     *
     * @param id l'ID de l'archive
     * @return la réponse contenant les détails de l'archive
     */
    ArchiveResponse getById(String id);

    /**
     * Récupère la dernière version de l'archive associée à une AT.
     *
     * @param atId l'ID de l'autorisation de travail
     * @return la réponse contenant les détails de l'archive la plus récente
     */
    ArchiveResponse getByAutorisationTravailId(String atId);

    /**
     * Récupère toutes les archives avec pagination.
     *
     * @param pageable la pagination
     * @return une page d'archives
     */
    Page<ArchiveResponse> getAll(org.springframework.data.domain.Pageable pageable);

    /**
     * Recherche des archives selon des critères.
     *
     * @param searchRequest les critères de recherche
     * @param pageable la pagination
     * @return une page d'archives correspondant à la recherche
     */
    Page<ArchiveSearchResponse> search(ArchiveSearchRequest searchRequest, org.springframework.data.domain.Pageable pageable);

    /**
     * Télécharge le fichier PDF d'une archive.
     *
     * @param id l'ID de l'archive
     * @return les bytes du fichier PDF
     */
    byte[] downloadArchive(String id);

    /**
     * Vérifie l'intégrité d'une archive en comparant le hash stocké avec le hash calculé du fichier PDF.
     *
     * @param id l'ID de l'archive
     * @return vrai si le hash correspond, faux sinon
     */
    boolean verifyArchive(String id);
}