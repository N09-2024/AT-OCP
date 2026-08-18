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

export interface AnalyzeAtPayload {
  atId?: string;
  description?: string;
  typeIntervention?: string;
  niveau?: string;
  installation?: string;
  equipement?: string;
  risques?: string[];
  mesures?: string[];
  epi?: string[];
  moyensAcces?: string[];
  visiteFaite?: boolean;
  nbRisques?: number;
  nbMesures?: number;
  nbEpis?: number;
  nbPermis?: number;
  sectionFRenseignee?: boolean;
}

export interface AnalyzeAtResult {
  summary: string;
  missingInformation: string[];
  identifiedRisks: string[];
  recommendedMeasures: string[];
  inconsistencies: string[];
  warnings: string[];
  sources: string[];
  confidence: string;

  // Champs de compatibilité
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

export interface ChatPayload {
  message: string;
  conversationId?: string;
  atContext?: Record<string, any>;
}

export interface ChatResult {
  answer: string;
  sources: string[];
  confidence: string;
  suggestedQuestions: string[];
}

export const iaApi = {
  /** Analyse complète multi-agents de l'Autorisation de Travail (CrewAI + LangChain + RAG) */
  analyzeAt: async (payload: AnalyzeAtPayload): Promise<AnalyzeAtResult> => {
    const { data } = await apiClient.post<AnalyzeAtResult>('/ai/analyze-at', payload);
    return data;
  },

  /** Assistant conversationnel RAG OCP */
  chat: async (payload: ChatPayload): Promise<ChatResult> => {
    const { data } = await apiClient.post<ChatResult>('/ai/chat', payload);
    return data;
  },

  /** Suggestions sections A/B/D/E à partir de la description (Rétrocompatible) */
  analyserIntervention: async (description: string): Promise<AnalyseInterventionIA> => {
    const { data } = await apiClient.post<AnalyseInterventionIA>('/ia/analyser-intervention', {
      description,
    });
    return data;
  },

  /** Contrôle avant soumission CEEP (Rétrocompatible) */
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
