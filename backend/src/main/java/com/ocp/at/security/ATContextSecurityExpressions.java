package com.ocp.at.security;

import com.ocp.at.entity.Utilisateur;
import com.ocp.at.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expressions de sécurité contextuelles appelables dans les annotations {@code @PreAuthorize}.
 *
 * <h2>Usage dans les controllers</h2>
 * <pre>{@code
 * // Étape 2 — Valider une visite (HCEE G ou HMEP G)
 * @PreAuthorize("hasAuthority('VALIDATE_VISITE')")
 *
 * // Étape 3 — Rédiger une AT (CEEP côté P uniquement)
 * @PreAuthorize("hasAuthority('EDIT_AT') and @atSec.estCeepProprietaire(#atId)")
 *
 * // Étape 4 — Démarrer une intervention (CEEE côté E uniquement)
 * @PreAuthorize("hasAuthority('START_INTERVENTION') and @atSec.estCeeeExecutant(#atId)")
 *
 * // Étape 8 — Archiver (HCEE uniquement, quel que soit P/E)
 * @PreAuthorize("hasAuthority('ARCHIVE_AT')")
 * }</pre>
 *
 * <h2>Design</h2>
 * <p>La plupart des actions de workflow sont contrôlées uniquement par les permissions
 * (déjà assignées par rôle dans DataInitializer), car un CEEP n'a RECEIVE_AT que si
 * son service est propriétaire sur une AT — la position P/E est déjà encodée dans le
 * fait d'avoir le bon rôle.</p>
 *
 * <p>Les méthodes {@code estCeepProprietaire}, {@code estCeeeExecutant}, etc. sont fournies
 * pour les cas nécessitant une vérification contextuelle explicite (double vérification
 * rôle + territoire).</p>
 *
 * @see ATContextService
 */
@Component("atSec")
@RequiredArgsConstructor
@Slf4j
public class ATContextSecurityExpressions {

    private final ATContextService atContextService;
    private final UtilisateurRepository utilisateurRepository;

    // =========================================================================
    // Vérifications de position contextuelle P/E
    // =========================================================================

    /**
     * L'utilisateur courant est-il côté Propriétaire sur cette AT ?
     * Indépendant du rôle (CEEP, HCEP, HMEP tous côté P si leur service == zoneProprietaire).
     */
    @Transactional(readOnly = true)
    public boolean estProprietaireSurAT(String atId) {
        return atContextService.estProprietaire(atId);
    }

    /**
     * L'utilisateur courant est-il côté Exécutant sur cette AT ?
     * Indépendant du rôle (CEEE, HCEE, HMEE tous côté E si leur service == zoneExecutante).
     */
    @Transactional(readOnly = true)
    public boolean estExecutantSurAT(String atId) {
        return atContextService.estExecutant(atId);
    }

    // =========================================================================
    // Combinaisons rôle + position P/E (double vérification)
    // Utiles pour les cas où la permission seule ne suffit pas.
    // =========================================================================

    /**
     * L'utilisateur a le rôle CEEP ET est côté Propriétaire sur cette AT.
     * Utilisable pour les actions de rédaction AT (§8.3 : CEEP exécute côté P).
     */
    @Transactional(readOnly = true)
    public boolean estCeepProprietaire(String atId) {
        return aRoleContenant("CEEP") && atContextService.estProprietaire(atId);
    }

    /**
     * L'utilisateur a le rôle CEEE ET est côté Exécutant sur cette AT.
     * Utilisable pour le démarrage de l'intervention (§4 : CEEE exécute côté E).
     */
    @Transactional(readOnly = true)
    public boolean estCeeeExecutant(String atId) {
        return aRoleContenant("CEEE") && atContextService.estExecutant(atId);
    }

    /**
     * L'utilisateur a le rôle HCEE ET est côté Exécutant sur cette AT.
     * Utilisable pour la validation (§8.3 : HCEE garant côté E).
     */
    @Transactional(readOnly = true)
    public boolean estHceeExecutant(String atId) {
        return aRoleContenant("HCEE") && atContextService.estExecutant(atId);
    }

    /**
     * L'utilisateur a le rôle HMEP ET est côté Propriétaire sur cette AT.
     * Utilisable pour la validation de visite (§8.2 : HMEP garant côté P).
     */
    @Transactional(readOnly = true)
    public boolean estHmepProprietaire(String atId) {
        return aRoleContenant("HMEP") && atContextService.estProprietaire(atId);
    }

    /**
     * L'utilisateur a le rôle HCEP ET est côté Propriétaire sur cette AT.
     * Utilisable pour la supervision de l'archivage (§8.6 : HCEP garant côté P).
     */
    @Transactional(readOnly = true)
    public boolean estHcepProprietaire(String atId) {
        return aRoleContenant("HCEP") && atContextService.estProprietaire(atId);
    }

    // =========================================================================
    // Méthodes de vérification des conditions de workflow
    // =========================================================================

    /**
     * L'utilisateur peut-il valider une visite sur cette AT ?
     * §8.2 : HCEE (G côté E) OU HMEP (G côté P).
     */
    @Transactional(readOnly = true)
    public boolean peutValiderVisite(String atId) {
        return estHceeExecutant(atId) || estHmepProprietaire(atId);
    }

    /**
     * L'utilisateur peut-il démarrer une intervention sur cette AT ?
     * §4 : CEEE (E côté E), HCEE et HMEP garantissent mais n'exécutent pas.
     * Pour le démarrage réel (action CEEE), utiliser {@link #estCeeeExecutant(String)}.
     *
     * Note : La validation HCEE/HMEP du démarrage passe par le mécanisme de visa.
     */
    @Transactional(readOnly = true)
    public boolean peutDemarrerIntervention(String atId) {
        return estCeeeExecutant(atId);
    }

    // =========================================================================
    // Méthode privée — vérification de rôle
    // =========================================================================

    private boolean aRoleContenant(String roleNom) {
        return SecurityUtils.getCurrentUtilisateurId()
                .flatMap(utilisateurRepository::findByEmail)
                .map(user -> user.getRoles().stream()
                        .anyMatch(r -> {
                            String nom = r.getNom();
                            if ("CEEP".equals(roleNom) || "CEEE".equals(roleNom)) {
                                return nom.equals("CE") || nom.equals(roleNom);
                            }
                            if ("HMEP".equals(roleNom) || "HMEE".equals(roleNom)) {
                                return nom.equals("HM") || nom.equals(roleNom);
                            }
                            if ("HCEP".equals(roleNom) || "HCEE".equals(roleNom)) {
                                return nom.equals("HC") || nom.equals(roleNom);
                            }
                            return nom.equals(roleNom);
                        }))
                .orElse(false);
    }
}
