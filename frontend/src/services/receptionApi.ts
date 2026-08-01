import { apiClient } from './apiClient';
import type { ReceptionTravaux, PhotoReception } from '../types';

export interface ReceptionTravauxRequest {
  autorisationTravailId: string;
  dateReelleDebut?: string;
  dateReelleFin?: string;
  travauxConformes: boolean;
  zoneNettoyee: boolean;
  consignationRetiree: boolean;
  equipementRemisEnService: boolean;
  installationRemiseEnEtat: boolean;
  essaisEffectues: boolean;
  essaisConformes: boolean;
  travauxRealises?: string;
  commentaires?: string;
}

export const receptionApi = {
  getByAtId: async (atId: string): Promise<ReceptionTravaux> => {
    const response = await apiClient.get<ReceptionTravaux>(`/receptions/at/${atId}`);
    return response.data;
  },

  getById: async (id: string): Promise<ReceptionTravaux> => {
    const response = await apiClient.get<ReceptionTravaux>(`/receptions/${id}`);
    return response.data;
  },

  create: async (request: ReceptionTravauxRequest): Promise<ReceptionTravaux> => {
    const response = await apiClient.post<ReceptionTravaux>('/receptions', request);
    return response.data;
  },

  update: async (id: string, request: ReceptionTravauxRequest): Promise<ReceptionTravaux> => {
    const response = await apiClient.put<ReceptionTravaux>(`/receptions/${id}`, request);
    return response.data;
  },

  signer: async (id: string, signaturePath: string): Promise<ReceptionTravaux> => {
    const response = await apiClient.put<ReceptionTravaux>(`/receptions/${id}/signer`, signaturePath, {
      headers: { 'Content-Type': 'text/plain' },
    });
    return response.data;
  },

  cloturer: async (id: string): Promise<ReceptionTravaux> => {
    const response = await apiClient.put<ReceptionTravaux>(`/receptions/${id}/cloturer`);
    return response.data;
  },

  getPhotos: async (id: string): Promise<PhotoReception[]> => {
    const response = await apiClient.get<PhotoReception[]>(`/receptions/${id}/photos`);
    return response.data;
  },

  ajouterPhoto: async (id: string, file: File, description?: string): Promise<PhotoReception> => {
    const formData = new FormData();
    formData.append('file', file);
    if (description) formData.append('description', description);
    const response = await apiClient.post<PhotoReception>(`/receptions/${id}/photos`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};
