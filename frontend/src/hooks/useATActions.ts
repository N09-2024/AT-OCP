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

  const role = user.roles?.[0]?.nom as RoleNom | undefined;
  const isHmee = role === 'HMEE';

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

  // HMEE : fail-closed intentionnel (lecture seule) tant que le rôle n'est pas clarifié par OCP
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

    // Étape 0 : Classification (HCEP)
    peutClassifier: hasPermission('CLASSIFY_INTERVENTION'),

    // Étape 2 : Visite chantier (CEEP E, HCEE/HMEP garants)
    peutCreerVisite: hasPermission('CREATE_VISITE') && (position === 'PROPRIETAIRE' || role === 'ADMIN'),
    peutValiderVisite: hasPermission('VALIDATE_VISITE'),

    // Étape 3 : Rédaction AT (CEEP E, HCEE G)
    peutRedigerAT: hasPermission('EDIT_AT') && (position === 'PROPRIETAIRE' || role === 'ADMIN'),
    peutValiderAT: hasPermission('VALIDATE_AT'),

    // Étape 4 : Démarrage travaux (CEEE E)
    peutDemarrerIntervention: hasPermission('START_INTERVENTION') && (position === 'EXECUTANT' || role === 'ADMIN'),

    // Étape 5b : Reconduction (CEEP E)
    peutReconduire: hasPermission('RENEW_AT') && (position === 'PROPRIETAIRE' || role === 'ADMIN'),

    // Étape 6 : Déclaration fin (CEEE E)
    peutDeclarerFin: hasPermission('DECLARE_FIN_TRAVAUX') && (position === 'EXECUTANT' || role === 'ADMIN'),

    // Étape 7 : Réception (CEEP E)
    peutReceptionner: hasPermission('RECEIVE_AT') && (position === 'PROPRIETAIRE' || role === 'ADMIN'),

    // Étape 8 : Archivage (HCEE E)
    peutArchiver: hasPermission('ARCHIVE_AT'),

    estLectureSeule: false,
  };
}
