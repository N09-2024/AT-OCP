import { apiClient } from './apiClient';
import type { Visa } from '../types';

export interface CreateVisaRequest {
  autorisationTravailId: string;
  commentaire?: string;
  ordre?: number;
}

export const visaApi = {
  // Créer un visa en attente
  createVisa: async (request: CreateVisaRequest): Promise<Visa> => {
    const response = await apiClient.post<Visa>('/visa', request);
    return response.data;
  },

  // Signer un visa existant avec une image PNG manuscrite
  signVisa: async (visaId: string, signatureBlob: Blob, commentaire?: string): Promise<Visa> => {
    const formData = new FormData();
    formData.append('signature', signatureBlob, 'signature.png');
    if (commentaire) {
      formData.append('commentaire', commentaire);
    }
    const response = await apiClient.post<Visa>(`/visa/${visaId}/sign`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Helper combiné : créer ET signer un visa en une seule étape pour le frontend
  createAndSignVisa: async (autorisationTravailId: string, signatureBlob: Blob, commentaire?: string, ordre = 1): Promise<Visa> => {
    const createdVisa = await visaApi.createVisa({ autorisationTravailId, commentaire, ordre });
    const signedVisa = await visaApi.signVisa(createdVisa.id, signatureBlob, commentaire);
    return signedVisa;
  },

  // Récupérer la liste des visas pour une AT
  getVisasByAtId: async (atId: string): Promise<Visa[]> => {
    const response = await apiClient.get<Visa[]>(`/visa/at/${atId}`);
    return response.data;
  },

  // Obtenir l'URL de l'image de la signature manuscrite
  getSignatureImageUrl: (visaId: string): string => {
    return `/api/visa/${visaId}/signature`;
  },
};
