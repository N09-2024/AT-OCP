import { apiClient } from './apiClient';

export interface NotificationItem {
  id: string;
  titre: string;
  message: string;
  dateCreation: string;
  dateLecture: string | null;
  lu: boolean;
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

  countUnread: async (): Promise<number> => {
    // Fetch the first 100 notifications to count unread ones client-side
    const all = await apiClient.get<Page<NotificationItem>>('/notifications', {
      params: { page: 0, size: 100 },
    });
    return all.data.content.filter((n) => !n.lu).length;
  },
};
