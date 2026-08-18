package com.ocp.at.service;

import com.ocp.at.dto.response.PermisDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service de gestion des documents de permis soumis pour validation IA (section E du F-HSE-SEC-31-04).
 */
public interface PermisDocumentService {

    /**
     * Synchronise les PermisDocument avec les permis cochés dans formPermisIds de l AT.
     * Crée les manquants (EN_ATTENTE_UPLOAD), supprime les devenus non cochés.
     */
    List<PermisDocumentResponse> initialiserPermisRequis(String atId);

    /**
     * Sauvegarde le fichier uploadé et déclenche l analyse IA asynchrone.
     */
    PermisDocumentResponse uploadPermisDocument(String atId, String typePermis, MultipartFile file);

    /**
     * Analyse IA asynchrone via Gemini Vision.
     * Met à jour statut, champs extraits, motif rejet.
     */
    void analyserPermisParIA(String permisDocumentId);

    /**
     * Retourne true si tous les documents de permis de l AT sont VALIDE,
     * ou si l AT n a aucun permis coché (pas de blocage).
     */
    boolean tousPermisValides(String atId);

    /**
     * Répercute le statut de tous les PermisDocument d'une AT (résultat agent IA)
     * sur les entités Permis correspondantes (Permis.statutVerification), qui sont
     * la valeur réellement contrôlée avant soumission. Idempotent - safe à appeler
     * à chaque soumission pour rattraper d'éventuels documents validés avant
     * l'introduction de la synchro automatique.
     */
    void resynchroniserStatutsPermis(String atId);

    /** Liste tous les PermisDocument d une AT. */
    List<PermisDocumentResponse> getPermisDocuments(String atId);

    /** Re-déclenche l analyse IA sur un document déjà uploadé. */
    PermisDocumentResponse relancerAnalyse(String permisDocumentId);
}