package com.ocp.at.service;

import com.ocp.at.dto.request.EssaiRequest;
import com.ocp.at.dto.request.ReceptionTravauxRequest;
import com.ocp.at.dto.response.EssaiResponse;
import com.ocp.at.dto.response.ReceptionTravauxResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReceptionTravauxService {

    /**
     * Crée une réception de travaux pour une AT validée.
     * Règle métier : l'AT doit être en statut VALIDEE.
     * Règle métier : une seule réception par AT.
     */
    ReceptionTravauxResponse create(ReceptionTravauxRequest request);

    /**
     * Met à jour une réception non encore validée.
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
     * Supprime une réception non validée.
     */
    void delete(String id);

    /**
     * Ajoute un essai à une réception.
     */
    EssaiResponse ajouterEssai(String receptionId, EssaiRequest request);

    /**
     * Modifie un essai existant.
     */
    EssaiResponse modifierEssai(String receptionId, String essaiId, EssaiRequest request);

    /**
     * Supprime un essai.
     */
    void supprimerEssai(String receptionId, String essaiId);

    /**
     * Valide la réception.
     * Règles :
     * - travauxConformes doit être true
     * - installationRemiseEnEtat doit être true
     * - essaisEffectues doit être true
     * - Tous les essais doivent être conformes (conforme = true)
     */
    ReceptionTravauxResponse validerReception(String id);
}
