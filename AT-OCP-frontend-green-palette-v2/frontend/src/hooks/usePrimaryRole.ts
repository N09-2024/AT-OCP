import { useAuthStore } from '../store/authStore';

export type AppRole = 'CE' | 'HM' | 'HC' | 'ADMIN' | 'RESPONSABLE_EXTERIEUR';

const PRIORITY: AppRole[] = ['ADMIN', 'HC', 'HM', 'CE', 'RESPONSABLE_EXTERIEUR'];

/**
 * Rôle applicatif principal pour redirection post-login et menus.
 * Priorité : ADMIN > HC > HM > CE > RESPONSABLE_EXTERIEUR
 */
export function usePrimaryRole(): AppRole {
  const user = useAuthStore((s) => s.user);
  const roles = (user?.roles || []).map((r: { nom: string }) => (r.nom || '').toUpperCase());

  for (const candidate of PRIORITY) {
    if (roles.includes(candidate)) return candidate;
  }

  // Rétrocompat anciens noms (avant V28)
  if (roles.some((r) => ['CEEP', 'CEEE', 'DEMANDEUR'].includes(r))) return 'CE';
  if (roles.some((r) => ['HMEP', 'HMEE'].includes(r))) return 'HM';
  if (roles.some((r) => ['HCEP', 'HCEE', 'RESPONSABLE_OCP'].includes(r))) return 'HC';
  if (roles.includes('RESPONSABLE_ENTREPRISE')) return 'RESPONSABLE_EXTERIEUR';

  return 'CE';
}

export function dashboardPathForRole(role: AppRole): string {
  switch (role) {
    case 'ADMIN':
      return '/dashboard/admin';
    case 'HC':
      return '/dashboard/hc';
    case 'HM':
      return '/dashboard/hm';
    case 'RESPONSABLE_EXTERIEUR':
      return '/dashboard/externe';
    case 'CE':
    default:
      return '/dashboard/ce';
  }
}
