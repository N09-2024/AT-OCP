import { apiClient } from './apiClient';
import { useAuthStore } from '../store/authStore';

// -------------------------------------------------------
// Types correspondant exactement aux DTOs du backend
// -------------------------------------------------------
export interface LoginRequest {
  email: string;
  motDePasse: string;  // champ backend : motDePasse (pas password)
}

export interface RoleResponse {
  id: string;
  nom: string;
  description?: string;
}

export interface UtilisateurResponse {
  id: string;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  photo?: string;
  actif: boolean;
  compteVerrouille: boolean;
  motDePasseExpire: boolean;
  dateCreation: string;
  dateModification?: string;
  derniereConnexion?: string;
  roles: RoleResponse[];
}

export interface JwtResponse {
  accessToken: string;
  refreshToken: string;
  type: string;
  utilisateur: UtilisateurResponse;
  roles: string[];
  permissions: string[];
}

// -------------------------------------------------------
// Service d'authentification
// -------------------------------------------------------
export const AuthService = {
  /**
   * Connexion : POST /api/auth/login
   * Retourne accessToken + profil utilisateur
   */
  login: async (data: { email: string; password: string }): Promise<{ token: string; user: UtilisateurResponse; permissions: string[] }> => {
    const payload: LoginRequest = {
      email: data.email,
      motDePasse: data.password,  // mapping frontend -> backend
    };

    const response = await apiClient.post<JwtResponse>('/auth/login', payload);
    const { accessToken, utilisateur, permissions, refreshToken } = response.data;

    // Stocker le accessToken dans localStorage pour un accès immédiat
    if (accessToken) {
      localStorage.setItem('accessToken', accessToken);
      console.log('[AuthService] Access token stored in localStorage');
    }
    
    // Stocker le refreshToken pour le renouvellement silencieux
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    // Mettre à jour le store d'authentification (qui sera persistant via zustand-middleware)
    useAuthStore.getState().login(utilisateur, accessToken, permissions);
    console.log('[AuthService] Auth store updated with token:', accessToken ? 'present' : 'null');

    return {
      token: accessToken,
      user: utilisateur,
      permissions,
    };
  },

  /**
   * Récupérer le profil de l'utilisateur connecté : GET /api/auth/me
   */
  getMe: async (): Promise<UtilisateurResponse> => {
    const response = await apiClient.get<UtilisateurResponse>('/auth/me');
    return response.data;
  },

  /**
   * Déconnexion : révoque le refreshToken côté backend
   */
  logout: async (): Promise<void> => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        await apiClient.post('/auth/logout', { refreshToken });
      } catch {
        // Ignorer les erreurs de logout
      }
      localStorage.removeItem('refreshToken');
    }
  },

  /**
   * Renouveler le token JWT
   */
  refreshToken: async (): Promise<string> => {
    const refreshToken = localStorage.getItem('refreshToken');
    const response = await apiClient.post<{ accessToken: string }>('/auth/refresh-token', { refreshToken });
    return response.data.accessToken;
  },
};
