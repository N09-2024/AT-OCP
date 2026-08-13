package com.ocp.at.security;

import com.ocp.at.entity.AutorisationTravail;
import com.ocp.at.entity.Role;
import com.ocp.at.entity.Utilisateur;
import com.ocp.at.entity.Zone;
import com.ocp.at.exception.BusinessException;
import com.ocp.at.exception.ResourceNotFoundException;
import com.ocp.at.repository.AutorisationTravailRepository;
import com.ocp.at.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Résolution contextuelle P/E + guards métier (Standard S-HSE-SEC-31).
 *
 * <p>Rôles applicatifs : CE, HM, HC, ADMIN, RESPONSABLE_EXTERIEUR.
 * Positions P/E calculées via {@code utilisateur.service.zone} vs zones de l'AT.</p>
 */
import com.ocp.at.entity.enums.NiveauHierarchique;
import com.ocp.at.entity.enums.PositionAT;

@Component("atContext")
@RequiredArgsConstructor
@Slf4j
public class ATContextService {

    /** Rôles applicatifs V28 */
    public static final String ROLE_CE = "CE";
    public static final String ROLE_HM = "HM";
    public static final String ROLE_HC = "HC";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_RESP_EXT = "RESPONSABLE_EXTERIEUR";

    private final AutorisationTravailRepository atRepository;
    private final UtilisateurRepository utilisateurRepository;

    // =========================================================================
    // Position P / E
    // =========================================================================

    @Transactional(readOnly = true)
    public PositionAT resolvePosition(Utilisateur user, AutorisationTravail at) {
        if (user == null || at == null || user.getService() == null || user.getService().getZone() == null) {
            return PositionAT.AUCUNE;
        }
        String userZoneId = user.getService().getZone().getId();
        if (at.getZoneProprietaire() != null && userZoneId.equals(at.getZoneProprietaire().getId())) {
            return PositionAT.PROPRIETAIRE;
        }
        if (at.getZoneExecutante() != null && userZoneId.equals(at.getZoneExecutante().getId())) {
            return PositionAT.EXECUTANT;
        }
        return PositionAT.AUCUNE;
    }

    public boolean peutAgir(Utilisateur user, AutorisationTravail at, String etape) {
        if (user == null) return false;
        NiveauHierarchique niv = user.getNiveau();
        if (niv == NiveauHierarchique.ADMIN) return true;

        PositionAT pos = resolvePosition(user, at);

        switch (etape.toUpperCase(Locale.ROOT)) {
            case "CREATION":
                return niv == NiveauHierarchique.CHEF_EQUIPE && pos == PositionAT.PROPRIETAIRE;
            case "VISITE_REDACTION":
                return niv == NiveauHierarchique.CHEF_EQUIPE && (pos == PositionAT.PROPRIETAIRE || pos == PositionAT.EXECUTANT);
            case "GARANT_VISITE":
                return (niv == NiveauHierarchique.HORS_CADRE && pos == PositionAT.EXECUTANT)
                    || (niv == NiveauHierarchique.HAUTE_MAITRISE && pos == PositionAT.PROPRIETAIRE);
            case "DEMARRAGE":
                return niv == NiveauHierarchique.CHEF_EQUIPE && pos == PositionAT.EXECUTANT;
            case "GARANT_DEMARRAGE":
                return (niv == NiveauHierarchique.HORS_CADRE && pos == PositionAT.EXECUTANT)
                    || (niv == NiveauHierarchique.HAUTE_MAITRISE && pos == PositionAT.EXECUTANT);
            case "RECONDUCTION":
                return niv == NiveauHierarchique.CHEF_EQUIPE && (pos == PositionAT.PROPRIETAIRE || pos == PositionAT.EXECUTANT);
            case "DECLARATION_FIN":
                return niv == NiveauHierarchique.CHEF_EQUIPE && pos == PositionAT.EXECUTANT;
            case "RECEPTION":
                return niv == NiveauHierarchique.CHEF_EQUIPE && (pos == PositionAT.PROPRIETAIRE || pos == PositionAT.EXECUTANT);
            case "ARCHIVAGE":
                return niv == NiveauHierarchique.HAUTE_MAITRISE && pos == PositionAT.PROPRIETAIRE;
            case "GARANT_ARCHIVAGE":
                return niv == NiveauHierarchique.HORS_CADRE && pos == PositionAT.PROPRIETAIRE;
            default:
                return false;
        }
    }

    @Transactional(readOnly = true)
    public PositionAT resoudrePosition(String atId) {
        Utilisateur user = getCurrentUser();
        AutorisationTravail at = getAt(atId);
        return resolvePosition(user, at);
    }

    @Transactional(readOnly = true)
    public boolean estProprietaire(String atId) {
        return resoudrePosition(atId) == PositionAT.PROPRIETAIRE;
    }

    @Transactional(readOnly = true)
    public boolean estExecutant(String atId) {
        return resoudrePosition(atId) == PositionAT.EXECUTANT;
    }

    // =========================================================================
    // Rôles applicatifs
    // =========================================================================

    public Set<String> rolesCourants() {
        Utilisateur user = getCurrentUser();
        if (user.getRoles() == null) {
            return Set.of();
        }
        return user.getRoles().stream()
                .map(Role::getNom)
                .map(n -> n == null ? "" : n.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean hasRole(String role) {
        return rolesCourants().contains(role.toUpperCase(Locale.ROOT));
    }

    public boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    public boolean isCE() {
        return hasRole(ROLE_CE) || isAdmin();
    }

    public boolean isHM() {
        return hasRole(ROLE_HM) || isAdmin();
    }

    public boolean isHC() {
        return hasRole(ROLE_HC) || isAdmin();
    }

    public boolean isResponsableExterieur() {
        return hasRole(ROLE_RESP_EXT);
    }

    /**
     * Rôle principal pour redirection UI (priorité ADMIN > HC > HM > CE > RESP_EXT).
     */
    public String rolePrincipal() {
        Set<String> roles = rolesCourants();
        if (roles.contains(ROLE_ADMIN)) return ROLE_ADMIN;
        if (roles.contains(ROLE_HC)) return ROLE_HC;
        if (roles.contains(ROLE_HM)) return ROLE_HM;
        if (roles.contains(ROLE_CE)) return ROLE_CE;
        if (roles.contains(ROLE_RESP_EXT)) return ROLE_RESP_EXT;
        return ROLE_CE; // défaut terrain
    }

    // =========================================================================
    // Guards d'actions workflow (à appeler dans les services)
    // =========================================================================

    public void verifierServiceRattache() {
        Utilisateur user = getCurrentUser();
        if (user.getService() == null) {
            throw new BusinessException(
                    "Votre compte n'est rattaché à aucun service. "
                            + "Contactez un administrateur pour définir votre service d'appartenance.");
        }
    }

    /** Classification Niveau 1/2 — HC (position P / HCEP) ou ADMIN */
    public void requireClassifier() {
        if (isAdmin()) return;
        if (!isHC()) {
            throw new BusinessException("Seuls les Hors Cadre (HC) peuvent classifier une intervention.");
        }
    }

    /** Créer demande / AT — CE ou ADMIN */
    public void requireCreerDemande() {
        if (isAdmin()) return;
        if (!isCE()) {
            throw new BusinessException("Seuls les Chefs d'Équipe (CE) peuvent créer une demande d'intervention.");
        }
    }

    /** Visite chantier — CE (P exécute / E participe) ou HM/HC garant, ou ADMIN */
    public void requireVisite(String atId) {
        if (isAdmin()) return;
        if (isCE() || isHM() || isHC()) return;
        throw new BusinessException("Vous n'êtes pas habilité à intervenir sur la visite chantier.");
    }

    /** Rédaction AT sur le terrain — CE position P (exécute) ou CE position E (participe) ou HC garant */
    public void requireRedaction(String atId) {
        if (isAdmin()) return;
        if (isCE() || isHC()) return;
        throw new BusinessException("Vous n'êtes pas habilité à rédiger / participer à la rédaction de l'AT.");
    }

    /** Signature / visa — CE, HM (P), HC, ADMIN */
    public void requireSigner(String atId) {
        if (isAdmin()) return;
        if (isCE() || isHC()) return;
        if (isHM() && estProprietaire(atId)) return;
        throw new BusinessException("Vous n'êtes pas habilité à signer / viser cette AT.");
    }

    /** Démarrer intervention — CE position E (exécute) ; HM/HC garant */
    public void requireDemarrer(String atId) {
        if (isAdmin()) return;
        if (isCE() && estExecutant(atId)) return;
        if (isHM() && estProprietaire(atId)) return; // garant HMEP
        if (isHC()) return; // garant HCEE
        throw new BusinessException(
                "Seul le Chef d'Équipe Exécutant (CE en position E) peut démarrer l'intervention "
                        + "(HM/HC en garant).");
    }

    /** Déclarer fin des travaux — CE position E */
    public void requireDeclarerFin(String atId) {
        if (isAdmin()) return;
        if (isCE() && estExecutant(atId)) return;
        throw new BusinessException(
                "Seul le Chef d'Équipe Exécutant (CE en position E) peut déclarer la fin des travaux.");
    }

    /** Réception — CE position P (exécute) ; CE position E participe */
    public void requireReception(String atId) {
        if (isAdmin()) return;
        if (isCE() && (estProprietaire(atId) || estExecutant(atId))) return;
        throw new BusinessException(
                "La réception est réservée aux Chefs d'Équipe (CE) du périmètre P ou E.");
    }

    /** Reconduction / visa poste — CE (P exécute / E participe) ou HC garant */
    public void requireReconduction(String atId) {
        if (isAdmin()) return;
        if (isCE() || isHC()) return;
        throw new BusinessException("Vous n'êtes pas habilité à reconduire / viser cette AT.");
    }

    /** Archivage — HC (P garant / E exécute) ou ADMIN */
    public void requireArchiver() {
        if (isAdmin()) return;
        if (!isHC()) {
            throw new BusinessException("Seuls les Hors Cadre (HC) peuvent archiver une AT.");
        }
    }

    /** HM en position E = lecture seule (fail-closed standard) */
    public void requireHmEcriture(String atId) {
        if (isAdmin()) return;
        if (isHM() && estExecutant(atId)) {
            throw new BusinessException(
                    "Haute Maîtrise en position Exécutant (HMEE) : accès en lecture seule uniquement.");
        }
    }

    // =========================================================================
    // Listes acteurs
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Utilisateur> findChefsEquipeExecutants(String atId) {
        AutorisationTravail at = getAt(atId);
        if (at.getZoneExecutante() == null) {
            return List.of();
        }
        return utilisateurRepository.findChefsEquipeByZoneId(at.getZoneExecutante().getId());
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> findChefsEquipeProprietaires(String atId) {
        AutorisationTravail at = getAt(atId);
        if (at.getZoneProprietaire() == null) {
            return List.of();
        }
        return utilisateurRepository.findChefsEquipeByZoneId(at.getZoneProprietaire().getId());
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> findChefsEquipeByService(String serviceId) {
        if (serviceId == null || serviceId.isBlank()) {
            return List.of();
        }
        return utilisateurRepository.findChefsEquipeByServiceId(serviceId);
    }

    // =========================================================================
    // Privé
    // =========================================================================

    private AutorisationTravail getAt(String atId) {
        return atRepository.findById(atId)
                .orElseThrow(() -> new ResourceNotFoundException("AT non trouvée : " + atId));
    }

    private Utilisateur getCurrentUser() {
        return SecurityUtils.getCurrentUtilisateurId()
                .flatMap(utilisateurRepository::findByEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur non authentifié"));
    }
}
