import { apiClient } from './apiClient';

export interface ReadinessCheckItem {
  code: string;
  label: string;
  passed: boolean;
  blocking: boolean;
  message?: string;
  details?: string;
}

export interface ReadinessCheckResponse {
  atId: string;
  atNumero: string;
  pret: boolean;
  bloqueurs: string[];
  checks: ReadinessCheckItem[];
}

export interface StartInterventionRequest {
  confirmationCeee?: boolean;
}

export interface EndInterventionRequest {
    travauxRealises: string;          // required - @NotBlank on backend
    travauxNonRealises?: string;
    anomalies?: string;
    observations?: string;
    zoneNettoyee?: boolean;
    materielRetire?: boolean;
    outilsRetires?: boolean;
    protectionsRetablies?: boolean;
    personnelEvacue?: boolean;
}

export const interventionApi = {
  /** Étape 4 - Readiness check (13 contrôles) avant démarrage */
  getReadiness: async (atId: string): Promise<ReadinessCheckResponse> => {
    const { data } = await apiClient.get<ReadinessCheckResponse>(
      `/autorisations-travail/${atId}/intervention/readiness`
    );
    return data;
  },

  /** Étape 4 - Démarrer l'intervention (CEEE) */
  start: async (atId: string, payload?: StartInterventionRequest): Promise<unknown> => {
    const { data } = await apiClient.post(
      `/autorisations-travail/${atId}/intervention/start`,
      payload ?? {}
    );
    return data;
  },

  /** Étape 6 - Déclarer la fin des travaux (CEEE) */
  end: async (atId: string, payload: EndInterventionRequest): Promise<unknown> => {
    const { data } = await apiClient.post(
      `/autorisations-travail/${atId}/intervention/end`,
      payload
    );
    return data;
  },

  /** Étape 8 - Contrôle de complétude avant archivage */
  getArchiveReadiness: async (atId: string): Promise<unknown> => {
    const { data } = await apiClient.get(
      `/autorisations-travail/${atId}/archive/readiness`
    );
    return data;
  },
};
