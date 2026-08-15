import { apiClient } from './apiClient';
import type { Archive } from '../types';
import type { PageResponse } from './autorisationTravailApi';

export const archiveApi = {
  // Obtenir la liste paginée des archives
  getAll: async (page = 0, size = 10): Promise<PageResponse<Archive>> => {
    const response = await apiClient.get<PageResponse<Archive>>(`/archives?page=${page}&size=${size}`);
    return response.data;
  },

  // Obtenir par ID
  getById: async (id: string): Promise<Archive> => {
    const response = await apiClient.get<Archive>(`/archives/${id}`);
    return response.data;
  },

  // Obtenir la dernière archive d'une AT
  getByAtId: async (atId: string): Promise<Archive> => {
    const response = await apiClient.get<Archive>(`/archives/at/${atId}`);
    return response.data;
  },

  // Archiver officiellement une AT clôturée (génère PDF, QR Code, SHA-256)
  archiverAT: async (atId: string): Promise<Archive> => {
    const response = await apiClient.post<Archive>(`/archives/archive/${atId}`);
    return response.data;
  },

  // Exporter temporairement le PDF
  exportAT: async (atId: string): Promise<{ pdfUrl: string; filename: string }> => {
    const response = await apiClient.post(`/archives/export/${atId}`);
    return response.data;
  },

  // Télécharger le fichier PDF d'une archive
  downloadArchive: async (id: string): Promise<Blob> => {
    const response = await apiClient.get(`/archives/${id}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Vérifier l'intégrité SHA-256
  verifyArchive: async (id: string): Promise<boolean> => {
    const response = await apiClient.get<boolean>(`/archives/${id}/verify`);
    return response.data;
  },
};
