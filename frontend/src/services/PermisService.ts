import { apiClient } from './apiClient';

export interface PermisResponse {
  id: string;
  numero: string;
  type: string;
  dateEmission: string;
  dateExpiration: string;
  statutVerification: string;
  estObligatoire: boolean;
  commentaire: string;
  fichierJointId: string;
  fichierJointNom: string;
  analyseIAId: string;
}

export interface AnalyseIAResponse {
  id: string;
  dateAnalyse: string;
  ocrText: string;
  jsonExtraction: string;
  tauxConfiance: number;
  resultat: string;
  commentaireIA: string;
  tempsExecution: number;
  modeleUtilise: string;
  versionModele: string;
  permisId: string;
}

export interface UploadPermisResponse {
  message: string;
  permisId: string;
  analyseIA: AnalyseIAResponse;
}

export interface PermisRequest {
  numero: string;
  type: string;
  dateEmission: string;
  dateExpiration: string;
  estObligatoire: boolean;
  commentaire?: string;
  autorisationTravailId: string;
}

export const PermisService = {
  getAllPermis: async (): Promise<PermisResponse[]> => {
    const response = await apiClient.get<PermisResponse[]>('/permis');
    return response.data;
  },

  getPermisById: async (id: string): Promise<PermisResponse> => {
    const response = await apiClient.get<PermisResponse>(`/permis/${id}`);
    return response.data;
  },

  getPermisByAT: async (atId: string): Promise<PermisResponse[]> => {
    const response = await apiClient.get<PermisResponse[]>(`/permis/at/${atId}`);
    return response.data;
  },

  createPermis: async (request: PermisRequest): Promise<PermisResponse> => {
    const response = await apiClient.post<PermisResponse>('/permis', request);
    return response.data;
  },

  updatePermis: async (id: string, request: PermisRequest): Promise<PermisResponse> => {
    const response = await apiClient.put<PermisResponse>(`/permis/${id}`, request);
    return response.data;
  },

  deletePermis: async (id: string): Promise<void> => {
    await apiClient.delete(`/permis/${id}`);
  },

  uploadFichier: async (id: string, file: File): Promise<UploadPermisResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<UploadPermisResponse>(`/permis/${id}/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  downloadFichier: async (id: string): Promise<Blob> => {
    const response = await apiClient.get(`/permis/${id}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  reanalyserPermis: async (id: string): Promise<UploadPermisResponse> => {
    const response = await apiClient.put<UploadPermisResponse>(`/permis/${id}/reanalyser`);
    return response.data;
  },

  getAnalyse: async (id: string): Promise<AnalyseIAResponse | null> => {
    const response = await apiClient.get<AnalyseIAResponse | "">(`/permis/${id}/analyse`);
    return response.data || null;
  }
};
