import { apiClient } from './apiClient';
import type { DemandeIntervention, OrdreTravail, BonTravail } from '../types';
import type { PageResponse } from './autorisationTravailApi';

export const documentInterventionApi = {
  getDemandesIntervention: async (page = 0, size = 50): Promise<PageResponse<DemandeIntervention>> => {
    const response = await apiClient.get<PageResponse<DemandeIntervention>>(`/demandes-intervention?page=${page}&size=${size}`);
    return response.data;
  },

  getDemandeInterventionById: async (id: string): Promise<DemandeIntervention> => {
    const response = await apiClient.get<DemandeIntervention>(`/demandes-intervention/${id}`);
    return response.data;
  },

  getOrdresTravail: async (page = 0, size = 50): Promise<PageResponse<OrdreTravail>> => {
    const response = await apiClient.get<PageResponse<OrdreTravail>>(`/ordres-travail?page=${page}&size=${size}`);
    return response.data;
  },

  getOrdreTravailById: async (id: string): Promise<OrdreTravail> => {
    const response = await apiClient.get<OrdreTravail>(`/ordres-travail/${id}`);
    return response.data;
  },

  getBonsTravail: async (page = 0, size = 50): Promise<PageResponse<BonTravail>> => {
    const response = await apiClient.get<PageResponse<BonTravail>>(`/bons-travail?page=${page}&size=${size}`);
    return response.data;
  },

  getBonTravailById: async (id: string): Promise<BonTravail> => {
    const response = await apiClient.get<BonTravail>(`/bons-travail/${id}`);
    return response.data;
  },
};
