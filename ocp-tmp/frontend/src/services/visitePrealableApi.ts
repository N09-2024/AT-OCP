import { apiClient } from './apiClient';
import type { VisitePrealable, PhotoVisite } from '../types';

export interface CreateVisiteRequest {
  documentSourceId?: string;
  typeDocumentSource?: string;
  latitude?: number;
  longitude?: number;
  commentaire?: string;
}

export const visitePrealableApi = {
  create: async (request: CreateVisiteRequest): Promise<VisitePrealable> => {
    const response = await apiClient.post<VisitePrealable>('/visites-prealables', request);
    return response.data;
  },

  getById: async (id: string): Promise<VisitePrealable> => {
    const response = await apiClient.get<VisitePrealable>(`/visites-prealables/${id}`);
    return response.data;
  },

  getByDocument: async (type: string, id: string): Promise<VisitePrealable> => {
    const response = await apiClient.get<VisitePrealable>(`/visites-prealables/document/${type}/${id}`);
    return response.data;
  },

  ajouterPhoto: async (visiteId: string, file: File): Promise<PhotoVisite> => {
    const formData = new FormData();
    formData.append('file', file);
    // Ne PAS forcer Content-Type : le boundary doit être ajouté par le navigateur
    const response = await apiClient.post<PhotoVisite>(`/visites-prealables/${visiteId}/photos`, formData);
    return response.data;
  },
};
