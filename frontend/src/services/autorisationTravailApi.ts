import { apiClient } from './apiClient';
import type { AutorisationTravail, HistoriqueAT, Visa } from '../types';

export interface AutoSaveRequest {
  objet?: string;
  descriptionTravaux?: string;
  dateDebut?: string;
  dateFin?: string;
  heureDebut?: string;
  heureFin?: string;
  servicesIntervenants?: string;
  entreprisesIntervenantes?: string;
  mesuresSecuriteExecutant?: string;
  risquesIds?: string[];
  mesuresIds?: string[];
  episIds?: string[];
  moyensAccesIds?: string[];
  permisIds?: string[];
}

export interface RefusRequest {
  commentaire: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const autorisationTravailApi = {
  // Liste paginée des ATs
  findAll: async (page = 0, size = 20): Promise<PageResponse<AutorisationTravail>> => {
    const response = await apiClient.get<PageResponse<AutorisationTravail>>(`/autorisations-travail?page=${page}&size=${size}`);
    return response.data;
  },

  // Obtenir par ID
  findById: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.get<AutorisationTravail>(`/autorisations-travail/${id}`);
    return response.data;
  },

  // Création à partir d'un document source (DI, OT, BT)
  createFromDocument: async (type: 'DI' | 'OT' | 'BT', documentId: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/documents/${type}/${documentId}/creer-at`);
    return response.data;
  },

  // Création directe sans document obligatoire
  createDirect: async (): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>('/autorisations-travail');
    return response.data;
  },

  // Sauvegarde automatique (AutoSave)
  autoSave: async (id: string, data: AutoSaveRequest): Promise<AutorisationTravail> => {
    const response = await apiClient.put<AutorisationTravail>(`/autorisations-travail/${id}/autosave`, data);
    return response.data;
  },

  // Verrous d'édition
  prendreVerrou: async (id: string): Promise<void> => {
    await apiClient.put(`/autorisations-travail/${id}/prendre-verrou`);
  },

  libererVerrou: async (id: string): Promise<void> => {
    await apiClient.put(`/autorisations-travail/${id}/liberer-verrou`);
  },

  transfererVerrou: async (id: string, nouvelUtilisateurId: string): Promise<void> => {
    await apiClient.put(`/autorisations-travail/${id}/transferer-verrou`, { nouvelUtilisateurId });
  },

  // Workflow transitions
  soumettre: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/submit`);
    return response.data;
  },

  valider: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/validate`);
    return response.data;
  },

  refuser: async (id: string, commentaire: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/reject`, { commentaire });
    return response.data;
  },

  renouveler: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/renew`);
    return response.data;
  },


  // --- Workflow S-HSE-SEC-31 ---
  marquerVisite: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/visite`);
    return response.data;
  },

  rediger: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/rediger`);
    return response.data;
  },

  demarrerIntervention: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/demarrer-intervention`);
    return response.data;
  },

  reconduire: async (id: string, depasse24h = false): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/reconduire?depasse24h=${depasse24h}`);
    return response.data;
  },

  declarerFin: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/declarer-fin`);
    return response.data;
  },

  signalerIncident: async (id: string, motif?: string): Promise<AutorisationTravail> => {
    const q = motif ? `?motif=${encodeURIComponent(motif)}` : '';
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/incident${q}`);
    return response.data;
  },

  receptionStandard: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/reception-standard`);
    return response.data;
  },

  cloturer: async (id: string): Promise<AutorisationTravail> => {
    const response = await apiClient.post<AutorisationTravail>(`/autorisations-travail/${id}/close`);
    return response.data;
  },

  // Historique & Visas & Export PDF
  getHistorique: async (id: string): Promise<HistoriqueAT[]> => {
    const response = await apiClient.get<HistoriqueAT[]>(`/autorisations-travail/${id}/historique`);
    return response.data;
  },

  getVisas: async (id: string): Promise<Visa[]> => {
    const response = await apiClient.get<Visa[]>(`/autorisations-travail/${id}/visas`);
    return response.data;
  },

  exportPdf: async (id: string): Promise<Blob> => {
    const response = await apiClient.get(`/autorisations-travail/${id}/export-pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },
};
