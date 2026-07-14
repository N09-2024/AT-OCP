import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UtilisateurResponse } from '../services/AuthService';

interface AuthState {
  user: UtilisateurResponse | null;
  token: string | null;
  permissions: string[];
  isAuthenticated: boolean;
  login: (user: UtilisateurResponse, token: string, permissions: string[]) => void;
  logout: () => void;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: string) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      permissions: [],
      isAuthenticated: false,

      login: (user, token, permissions) => {
        set({ user, token, permissions, isAuthenticated: true });
      },

      logout: () => {
        localStorage.removeItem('refreshToken');
        set({ user: null, token: null, permissions: [], isAuthenticated: false });
      },

      hasPermission: (permission: string) => {
        return get().permissions.includes(permission);
      },

      hasRole: (role: string) => {
        const user = get().user;
        if (!user?.roles) return false;
        return user.roles.some((r) => r.nom === role);
      },
    }),
    {
      name: 'ocp-at-auth',  // clé localStorage
      partialize: (state) => ({
        token: state.token,
        user: state.user,
        permissions: state.permissions,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
