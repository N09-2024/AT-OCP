import { apiClient } from './apiClient';

export interface DashboardData {
  kpis: {
    autorisationsEnCours: number;
    visasEnAttente: number;
    permisActifs: number;
    receptionsEnAttente: number;
    totalArchives: number;
  };
  monthlyStats: Array<{
    mois: string;
    total: number;
  }>;
  statusDistribution: Record<string, number>;
  recentAutorisations: Array<{
    id: string;
    titre: string;
    installation: string;
    statut: string;
    echeance: string;
  }>;
}

export const DashboardService = {
  getStats: async (): Promise<DashboardData> => {
    const response = await apiClient.get<DashboardData>('/dashboard/stats');
    return response.data;
  },
};
