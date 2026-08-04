import { useAuthStore } from '../store/authStore';
import type { AutorisationTravail, RoleNom } from '../types';

/**
 * Hook personnalisé déterminant les actions permises à l'utilisateur courant sur une AT.
 *
 * Conforme au Standard OCP S-HSE-SEC-31 v1.0 :
 * - Résout la position P (Propriétaire) vs E (Exécutant) de l'utilisateur sur l'AT.
 * - Vérifie les permissions RBAC de l'utilisateur.
 * - Garantit qu'un HMEE est en fail-closed (lecture seule).
 */
export function useATActions(at: AutorisationTravail | null) {
  const user = useAuthStore((s) => s.user);
  const hasPermission = useAuthStore((s) => s.hasPermission);

  if (!at || !user) {
    return {
      position: 'AUCUNE' as const,
      peutClassifier: false,
      peutCreerVisite: false,
      peutValiderVisite: false,
      peutRedigerAT: false,
      peutValiderAT: false,
      peutDemarrerIntervention: false,
      peutReconduire: false,
      peutDeclarerFin: false,
      peutReceptionner: false,
      peutArchiver: false,
      estLectureSeule: true,
    };
  }

  const userRoles = (user.roles || []).map((r) => (r.nom || '').toUpperCase());
  const isAdmin = userRoles.includes('ADMIN');
  const isCE = userRoles.includes('CE') || userRoles.includes('CEEP') || userRoles.includes('CEEE');
  const isHM = userRoles.includes('HM') || userRoles.includes('HMEP') || userRoles.includes('HMEE');
  const isHC = userRoles.includes('HC') || userRoles.includes('HCEP') || userRoles.includes('HCEE');

  // Position P/E contextuelle issue de la réponse du backend ou calculée localement
  let position = at.positionUtilisateurCourant || 'AUCUNE';
  if (position === 'AUCUNE' && user.service?.zone) {
    const userZoneId = user.service.zone.id;
    if (at.zoneProprietaire?.id === userZoneId) {
      position = 'PROPRIETAIRE';
    } else if (at.zoneExecutante?.id === userZoneId) {
      position = 'EXECUTANT';
    }
  }

  // HMEE (Haute Maîtrise en position Exécutante) : fail-closed intentionnel (lecture seule)
  const isHmee = (isHM || userRoles.includes('HMEE')) && position === 'EXECUTANT' && !isAdmin;

  if (isHmee) {
    return {
      position,
      peutClassifier: false,
      peutCreerVisite: false,
      peutValiderVisite: false,
      peutRedigerAT: false,
      peutValiderAT: false,
      peutDemarrerIntervention: false,
      peutReconduire: false,
      peutDeclarerFin: false,
      peutReceptionner: false,
      peutArchiver: false,
      estLectureSeule: true,
    };
  }

  return {
    position,

    // Étape 0 : Classification (HC / ADMIN)
    peutClassifier: (isHC || isAdmin) && hasPermission('CLASSIFY_INTERVENTION'),

    // Étape 2 : Visite chantier (CE pour création, HM/HC/ADMIN pour validation)
    peutCreerVisite: (isCE || isAdmin) && hasPermission('CREATE_VISITE'),
    peutValiderVisite: (isHM || isHC || isAdmin) && hasPermission('VALIDATE_VISITE'),

    // Étape 3 : Rédaction & Validation AT
    peutRedigerAT: (isCE || isAdmin) && hasPermission('EDIT_AT'),
    peutValiderAT: (isHC || isAdmin) && hasPermission('VALIDATE_AT'),

    // Étape 4 : Démarrage travaux (CE en position E, HM/HC garants)
    peutDemarrerIntervention: (isCE || isHM || isHC || isAdmin) && hasPermission('START_INTERVENTION'),

    // Étape 5b : Reconduction
    peutReconduire: (isCE || isHC || isAdmin) && hasPermission('RENEW_AT'),

    // Étape 6 : Déclaration fin (CE en position E)
    peutDeclarerFin: (isCE || isAdmin) && (position === 'EXECUTANT' || isAdmin) && hasPermission('DECLARE_FIN_TRAVAUX'),

    // Étape 7 : Réception (CE position P / E)
    peutReceptionner: (isCE || isAdmin) && hasPermission('RECEIVE_AT'),

    // Étape 8 : Archivage (HC / ADMIN)
    peutArchiver: (isHC || isAdmin) && hasPermission('ARCHIVE_AT'),

    estLectureSeule: false,
  };
}
