package com.ocp.at.security;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Service;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.Zone;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de résolution contextuelle de la position P/E d'un utilisateur sur une AT.
 *
 * <h2>Principe métier (Standard S-HSE-SEC-31)</h2>
 * <p>Les codes P (Propriétaire) et E (Exécutant) ne sont pas des propriétés fixes de l'utilisateur.
 * Ils désignent une position <em>relative au territoire de l'AT</em> :</p>
 * <ul>
 *   <li><b>P</b> = le service dont la zone est {@code zoneProprietaire} de l'AT (hôte de l'intervention)</li>
 *   <li><b>E</b> = le service dont la zone est {@code zoneExecutante} de l'AT (intervenant externe)</li>
 * </ul>
 *
 * <p>Un même utilisateur peut être côté P sur une AT et côté E sur une autre.
 * C'est le champ {@code Utilisateur.service} qui permet cette résolution.</p>
 *
 * <h2>Conditions préalables</h2>
 * <ul>
 *   <li>L'utilisateur doit être rattaché à un service ({@code utilisateur.service != null})</li>
 *   <li>L'AT doit avoir ses zones définies ({@code zoneProprietaire}, {@code zoneExecutante})</li>
 * </ul>
 *
 * @see com.ocp.at.entity.Utilisateur#getService()
 * @see com.ocp.at.entity.AutorisationTravail#getZoneProprietaire()
 * @see com.ocp.at.entity.AutorisationTravail#getZoneExecutante()
 */
@Component("atContext")
@RequiredArgsConstructor
@Slf4j
public class ATContextService {

    /**
     * Position contextuelle d'un utilisateur sur une AT donnée.
     * Déterminée à l'exécution en comparant le service de l'utilisateur aux zones de l'AT.
     */
    public enum PositionAT {
        /** L'utilisateur appartient au service hébergeant l'intervention (zone propriétaire). */
        PROPRIETAIRE,
        /** L'utilisateur appartient au service intervenant (zone exécutante). */
        EXECUTANT,
        /** L'utilisateur n'est ni P ni E sur cette AT (accès en lecture seule uniquement). */
        AUCUNE
    }

    private final AutorisationTravailRepository atRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Résout la position (P, E ou Aucune) de l'utilisateur courant sur l'AT identifiée par {@code atId}.
     *
     * @param atId identifiant de l'AT
     * @return {@link PositionAT} de l'utilisateur courant
     */
    @Transactional(readOnly = true)
    public PositionAT resoudrePosition(String atId) {
        Utilisateur user = getCurrentUser();
        if (user.getService() == null) {
            log.warn("[ATContext] Utilisateur {} sans service rattaché — position AUCUNE sur AT {}",
                    user.getEmail(), atId);
            return PositionAT.AUCUNE;
        }

        AutorisationTravail at = atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("AT non trouvée : " + atId));

        String userServiceId = user.getService().getId();

        // Résolution côté Propriétaire
        if (at.getZoneProprietaire() != null) {
            Zone zoneP = at.getZoneProprietaire();
            // La zone propriétaire correspond au service de l'utilisateur via zone.id
            // Note : Zone n'a pas de lien direct vers Service — la comparaison se fait via
            // le service de l'utilisateur et la zone assignée à ce service.
            // Ici on compare zone.id == utilisateur.service.zone.id
            if (user.getService().getZone() != null &&
                    user.getService().getZone().getId().equals(zoneP.getId())) {
                return PositionAT.PROPRIETAIRE;
            }
        }

        // Résolution côté Exécutant
        if (at.getZoneExecutante() != null) {
            Zone zoneE = at.getZoneExecutante();
            if (user.getService().getZone() != null &&
                    user.getService().getZone().getId().equals(zoneE.getId())) {
                return PositionAT.EXECUTANT;
            }
        }

        log.debug("[ATContext] Utilisateur {} (service={}) n'est ni P ni E sur AT {}",
                user.getEmail(), userServiceId, atId);
        return PositionAT.AUCUNE;
    }

    // =========================================================================
    // Méthodes de commodité
    // =========================================================================

    /** @return true si l'utilisateur courant est côté Propriétaire sur cette AT. */
    @Transactional(readOnly = true)
    public boolean estProprietaire(String atId) {
        return resoudrePosition(atId) == PositionAT.PROPRIETAIRE;
    }

    /** @return true si l'utilisateur courant est côté Exécutant sur cette AT. */
    @Transactional(readOnly = true)
    public boolean estExecutant(String atId) {
        return resoudrePosition(atId) == PositionAT.EXECUTANT;
    }

    /**
     * Vérifie que l'utilisateur a un service rattaché.
     * À appeler avant toute action de workflow sur une AT.
     *
     * @throws com.ocp.at.exception.BusinessException si l'utilisateur n'a pas de service
     */
    public void verifierServiceRattache() {
        Utilisateur user = getCurrentUser();
        if (user.getService() == null) {
            throw new com.ocp.at.exception.BusinessException(
                "Votre compte n'est rattaché à aucun service. " +
                "Contactez un administrateur pour définir votre service d'appartenance.");
        }
    }

    // =========================================================================
    // Méthode privée
    // =========================================================================

    private Utilisateur getCurrentUser() {
        return SecurityUtils.getCurrentUtilisateurId()
                .flatMap(utilisateurRepository::findByEmail)
                .orElseThrow(() -> new com.ocp.at.exception.BusinessException("Utilisateur non authentifié"));
    }


    /**
     * Chefs d'équipe du service intervenant (zone exécutante) → acteurs CEEE sur cette AT.
     * À afficher dans le formulaire et à notifier à la soumission.
     */
    @Transactional(readOnly = true)
    public java.util.List<Utilisateur> findChefsEquipeExecutants(String atId) {
        AutorisationTravail at = atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("AT non trouvée : " + atId));
        if (at.getZoneExecutante() == null) {
            return java.util.Collections.emptyList();
        }
        return utilisateurRepository.findChefsEquipeByZoneId(at.getZoneExecutante().getId());
    }

    /**
     * Chefs d'équipe du service propriétaire (zone propriétaire) → acteurs CEEP sur cette AT.
     */
    @Transactional(readOnly = true)
    public java.util.List<Utilisateur> findChefsEquipeProprietaires(String atId) {
        AutorisationTravail at = atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("AT non trouvée : " + atId));
        if (at.getZoneProprietaire() == null) {
            return java.util.Collections.emptyList();
        }
        return utilisateurRepository.findChefsEquipeByZoneId(at.getZoneProprietaire().getId());
    }

    /** Chefs d'équipe rattachés à un service (pour le formulaire : sélection service intervenant). */
    @Transactional(readOnly = true)
    public java.util.List<Utilisateur> findChefsEquipeByService(String serviceId) {
        if (serviceId == null || serviceId.isBlank()) {
            return java.util.Collections.emptyList();
        }
        return utilisateurRepository.findChefsEquipeByServiceId(serviceId);
    }
}
