import axios from 'axios';
import { useAuthStore } from '../store/authStore';

// En dev, le proxy Vite redirige /api -> http://localhost:8080/api
// En prod, nginx fait le reverse proxy
const BASE_URL = '/api';

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Intercepteur requête : ajoute le token JWT
apiClient.interceptors.request.use(
  (config) => {
    // Try to get token from localStorage first (works before store hydration)
    let token = localStorage.getItem('accessToken');
    // Fallback to store (for consistency after login)
    if (!token) {
      token = useAuthStore.getState().token;
    }
    if (token) {
      console.log('[apiClient] Adding token to request:', token);
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      console.log('[apiClient] No token found');
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Intercepteur réponse : déconnexion automatique si 401
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = '/auth/login';
    }
    return Promise.reject(error);
  }
);
