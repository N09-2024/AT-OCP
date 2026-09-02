import { apiClient } from './apiClient';

export interface NotificationItem {
  id: string;
  titre: string;
  message: string;
  dateCreation: string;
  dateLecture: string | null;
  lu: boolean;
  type?: string;
  lien?: string;
  utilisateurId: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const NotificationService = {
  getMyNotifications: async (page = 0, size = 20): Promise<Page<NotificationItem>> => {
    const res = await apiClient.get<Page<NotificationItem>>('/notifications', {
      params: { page, size, sort: 'dateCreation,desc' },
    });
    return res.data;
  },

  markAsRead: async (id: string): Promise<void> => {
    await apiClient.put(`/notifications/${id}/read`);
  },

  markAllAsRead: async (): Promise<void> => {
    await apiClient.put('/notifications/read-all');
  },

countUnread: async (): Promise<number> => {
  try {
    const res = await apiClient.get<{ count: number }>('/notifications/count-unread');
    return res.data.count ?? 0;
  } catch {
    return 0; // silencieux - ne jamais crasher la Topbar
  }

  },
};
