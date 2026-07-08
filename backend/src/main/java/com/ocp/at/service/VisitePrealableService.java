package com.ocp.at.service;

import com.ocp.at.dto.request.VisitePrealableRequest;
import com.ocp.at.dto.response.PhotoResponse;
import com.ocp.at.dto.response.VisitePrealableResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface VisitePrealableService {

    /**
     * Crée une visite préalable liée à un document source (DI, OT ou BT).
     * Règle : un document ne peut avoir qu'une seule visite.
     */
    VisitePrealableResponse create(VisitePrealableRequest request);

    /**
     * Modifie une visite existante (impossible si effectuee = true).
     */
    VisitePrealableResponse update(String id, VisitePrealableRequest request);

    /**
     * Finalise la visite (effectuee = true) si toutes les conditions sont remplies :
     * GPS renseigné, commentaire non vide, au moins une photo.
     */
    VisitePrealableResponse finaliser(String id);

    /** Supprime/annule une visite (impossible si une analyse est attachée). */
    void delete(String id);

    /** Consulte une visite par son ID. */
    VisitePrealableResponse findById(String id);

    /** Liste toutes les visites avec pagination. */
    Page<VisitePrealableResponse> findAll(Pageable pageable);

    /**
     * Ajoute une photo à la visite.
     * Le fichier est stocké via le StorageService ; seul le chemin est persisté en DB.
     */
    PhotoResponse addPhoto(String visiteId, MultipartFile file, String legende);

    /**
     * Supprime une photo de la visite.
     * Impossible si la visite est finalisée.
     */
    void deletePhoto(String visiteId, String photoId);
}
