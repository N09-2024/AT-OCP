import { apiClient } from './apiClient';

export interface DemandeReconductionRequest {
  atId: string;
  nouvelleDateFin: string; // ISO datetime
  motif: string;
}

export interface DecisionReconductionRequest {
  approuve: boolean;
  commentaire?: string;
  motifRefus?: string;
}

export interface ReconductionResponse {
  id: string;
  atId: string;
  atNumero: string;
  demandeurNom: string;
  demandeurPrenom: string;
  dateDebut: string;
  dateDemande: string;
  nouvelleDateFin: string;
  motif: string;
  statut: 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  decisionParNom?: string;
  decisionParPrenom?: string;
  dateDecision?: string;
  motifRefus?: string;
  commentaire?: string;
  analyseIaJson?: string;
}

export const reconductionApi = {
  /** CEEE - Demander une reconduction */
  demander: async (payload: DemandeReconductionRequest): Promise<ReconductionResponse> => {
    const { data } = await apiClient.post<ReconductionResponse>(
      `/reconductions`,
      payload
    );
    return data;
  },

  /** HMEP - Décider d'une demande de reconduction */
  decider: async (id: string, payload: DecisionReconductionRequest): Promise<ReconductionResponse> => {
    const { data } = await apiClient.post<ReconductionResponse>(
      `/reconductions/${id}/decision`,
      payload
    );
    return data;
  },

  /** Liste des reconductions d'une AT */
  getByAtId: async (atId: string): Promise<ReconductionResponse[]> => {
    const { data } = await apiClient.get<ReconductionResponse[]>(
      `/reconductions/at/${atId}`
    );
    return data;
  },

  /** HMEP - Liste des reconductions en attente de décision */
  getPending: async (): Promise<ReconductionResponse[]> => {
    const { data } = await apiClient.get<ReconductionResponse[]>(
      `/reconductions/pending`
    );
    return data;
  },

  /** Détail d'une reconduction */
  getById: async (id: string): Promise<ReconductionResponse> => {
    const { data } = await apiClient.get<ReconductionResponse>(
      `/reconductions/${id}`
    );
    return data;
  },
};
