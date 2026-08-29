package com.ocp.at.service;

import com.ocp.at.dto.request.PhotoReceptionRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.PhotoReceptionResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReceptionTravauxService {

    /**
     * Crée une réception de travaux pour une AT validée.
     * Règle métier : l'AT doit être en statut VALIDEE.
     * Règle métier : une seule réception par AT.
     */
    ReceptionTravauxResponse create(ReceptionTravauxRequest request);

    /**
     * Met à jour une réception non encore clôturée.
     */
    ReceptionTravauxResponse update(String id, ReceptionTravauxRequest request);

    /**
     * Récupère une réception par son identifiant.
     */
    ReceptionTravauxResponse getById(String id);

    /**
     * Récupère la réception associée à une AT.
     */
    ReceptionTravauxResponse getByAutorisationTravailId(String atId);

    /**
     * Liste paginée de toutes les réceptions.
     */
    Page<ReceptionTravauxResponse> getAll(Pageable pageable);

    /**
     * Supprime une réception non clôturée.
     */
    void delete(String id);

    /**
     * Signe la réception avec la signature manuscrite du responsable.
     * Réutilise le système de signature du Module 8.
     */
    ReceptionTravauxResponse signer(String id, String signaturePath);

    /**
     * Valide la réception conjointe par le CEEP avec évaluation de conformité et visa électronique manuscrit.
     */
    ReceptionTravauxResponse validerReceptionCeep(String id, com.ocp.at.dto.request.ValidationReceptionCeepRequest request, org.springframework.web.multipart.MultipartFile signatureFile);

    /**
     * Vérifie de manière déterministe les conditions préalables à la clôture.
     */
    com.ocp.at.dto.response.ClosureReadinessResponse verifierCloture(String atId);

    /**
     * Clôture l'AT associée à la réception.
     * Vérifie :
     * - réception existante et validée conjointement
     * - travaux conformes
     * - zone nettoyée
     * - consignation retirée
     * - équipement remis en service
     * - essais réalisés
     * - signatures CEEE et CEEP présentes
     */
    ReceptionTravauxResponse cloturerAT(String id);

    /**
     * Ajoute une photo à une réception.
     */
    PhotoReceptionResponse ajouterPhoto(String receptionId, PhotoReceptionRequest request);

    /**
     * Supprime une photo.
     */
    void supprimerPhoto(String receptionId, String photoId);

    /**
     * Récupère toutes les photos d'une réception.
     */
    List<PhotoReceptionResponse> getPhotos(String receptionId);
}
