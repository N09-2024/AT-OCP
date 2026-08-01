import { apiClient } from './apiClient';

export interface AnalyseInterventionIA {
  risques: string[];
  mesures: string[];
  epis: string[];
  permis: string[];
  rapport?: string;
  alertes: string[];
  complet: boolean;
  provider?: string;
  tauxConfiance?: number;
}

export const iaApi = {
  /** Suggestions sections A/B/D/E à partir de la description */
  analyserIntervention: async (description: string): Promise<AnalyseInterventionIA> => {
    const { data } = await apiClient.post<AnalyseInterventionIA>('/ia/analyser-intervention', {
      description,
    });
    return data;
  },

  /** Contrôle avant soumission CEEP */
  controlerDossier: async (payload: {
    description?: string;
    visiteFaite?: boolean;
    nbRisques?: number;
    nbMesures?: number;
    nbEpis?: number;
    nbPermis?: number;
    sectionFRenseignee?: boolean;
  }): Promise<AnalyseInterventionIA> => {
    const { data } = await apiClient.post<AnalyseInterventionIA>('/ia/controler-dossier', payload);
    return data;
  },
};
