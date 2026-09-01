import { useState, useEffect, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import { NotificationService, type NotificationItem } from '../services/NotificationService';
import { useAuthStore } from '../store/authStore';

interface UseNotificationsReturn {
  notifications: NotificationItem[];
  unreadCount: number;
  loading: boolean;
  refresh: () => void;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
}

export function useNotifications(): UseNotificationsReturn {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const stompClientRef = useRef<Client | null>(null);
  const user = useAuthStore((state) => state.user);
  const token = useAuthStore((state) => state.token);

  const fetchNotifications = useCallback(async () => {
    try {
      const page = await NotificationService.getMyNotifications(0, 100);
      setNotifications(page.content);
      setUnreadCount(page.content.filter((n) => !n.lu).length);
    } catch (err) {
      console.error('Erreur chargement notifications', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const refresh = useCallback(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const markAsRead = useCallback(async (id: string) => {
    await NotificationService.markAsRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, lu: true, dateLecture: new Date().toISOString() } : n))
    );
    setUnreadCount((prev) => Math.max(0, prev - 1));
  }, []);

  const markAllAsRead = useCallback(async () => {
    await NotificationService.markAllAsRead();
    setNotifications((prev) =>
      prev.map((n) => ({ ...n, lu: true, dateLecture: n.dateLecture ?? new Date().toISOString() }))
    );
    setUnreadCount(0);
  }, []);

  // Initial load & refresh on auth change
  useEffect(() => {
    if (token) {
      fetchNotifications();
    }
  }, [token, fetchNotifications]);

  // Polling every 15s to guarantee fresh notifications without relying solely on WS
  useEffect(() => {
    if (!token) return;
    const interval = setInterval(() => {
      NotificationService.countUnread()
        .then((count) => setUnreadCount(count))
        .catch(() => {});
    }, 15000);
    return () => clearInterval(interval);
  }, [token]);

  // Native WebSocket STOMP connection for instant real-time notifications
  useEffect(() => {
    if (!token || !user?.id) return;

    const userId = user.id;
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const brokerURL = `${protocol}//${window.location.host}/ws`;

    const client = new Client({
      brokerURL,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        // Subscribe to personal notification topic
        client.subscribe(`/topic/notifications/${userId}`, (message) => {
          try {
            const newNotif: NotificationItem = JSON.parse(message.body);
            setNotifications((prev) => [newNotif, ...prev.filter((n) => n.id !== newNotif.id)]);
            setUnreadCount((prev) => prev + 1);
          } catch {
            // ignore parse errors
          }
        });

        // Subscribe to unread count updates
        client.subscribe(`/topic/notifications/${userId}/count`, (message) => {
          try {
            const data = JSON.parse(message.body);
            if (typeof data.count === 'number') {
              setUnreadCount(data.count);
            }
          } catch {
            // ignore parse errors
          }
        });
      },
      onDisconnect: () => {
        // Silent disconnect — polling will handle continuity
      },
      onStompError: () => {
        // Silent error — polling fallback is active
      },
    });

    try {
      client.activate();
      stompClientRef.current = client;
    } catch {
      // WebSocket unavailable — polling covers it
    }

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        stompClientRef.current = null;
      }
    };
  }, [token, user?.id]);

  return { notifications, unreadCount, loading, refresh, markAsRead, markAllAsRead };
}
