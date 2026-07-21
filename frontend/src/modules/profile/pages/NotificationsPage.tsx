import { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  Chip,
  IconButton,
  CircularProgress,
  Divider,
  Button,
  Alert,
  Tooltip,
} from '@mui/material';
import { BellIcon, CheckIcon, EnvelopeOpenIcon } from '@heroicons/react/24/outline';
import { NotificationService, type NotificationItem } from '../../../services/NotificationService';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [markingAll, setMarkingAll] = useState(false);

  const loadNotifications = () => {
    setLoading(true);
    NotificationService.getMyNotifications(0, 100)
      .then((page) => setNotifications(page.content))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkAsRead = async (id: string) => {
    await NotificationService.markAsRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, lu: true, dateLecture: new Date().toISOString() } : n))
    );
  };

  const handleMarkAllAsRead = async () => {
    setMarkingAll(true);
    const unread = notifications.filter((n) => !n.lu);
    await Promise.all(unread.map((n) => NotificationService.markAsRead(n.id)));
    setNotifications((prev) =>
      prev.map((n) => ({ ...n, lu: true, dateLecture: n.dateLecture ?? new Date().toISOString() }))
    );
    setMarkingAll(false);
  };

  const unreadCount = notifications.filter((n) => !n.lu).length;

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 2,
              bgcolor: '#eff6ff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <BellIcon width={22} color="#2563eb" />
          </Box>
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
              Notifications
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {unreadCount > 0
                ? `${unreadCount} notification(s) non lue(s)`
                : 'Toutes les notifications sont lues'}
            </Typography>
          </Box>
        </Box>
        {unreadCount > 0 && (
          <Button
            variant="outlined"
            size="small"
            startIcon={<CheckIcon width={16} />}
            onClick={handleMarkAllAsRead}
            disabled={markingAll}
            sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
          >
            Tout marquer comme lu
          </Button>
        )}
      </Box>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
          <CircularProgress color="primary" />
        </Box>
      ) : notifications.length === 0 ? (
        <Alert severity="info" sx={{ borderRadius: 3 }}>
          Aucune notification pour le moment.
        </Alert>
      ) : (
        <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
          <List disablePadding>
            {notifications.map((notif, index) => (
              <Box key={notif.id}>
                <ListItem
                  sx={{
                    px: 3,
                    py: 2,
                    bgcolor: notif.lu ? 'transparent' : 'primary.50',
                    borderLeft: notif.lu ? '3px solid transparent' : '3px solid',
                    borderColor: notif.lu ? 'transparent' : 'primary.main',
                    transition: 'background-color 0.2s',
                    '&:hover': { bgcolor: 'action.hover' },
                  }}
                  secondaryAction={
                    !notif.lu && (
                      <Tooltip title="Marquer comme lu">
                        <IconButton
                          size="small"
                          onClick={() => handleMarkAsRead(notif.id)}
                          sx={{ color: 'primary.main' }}
                        >
                          <EnvelopeOpenIcon width={18} />
                        </IconButton>
                      </Tooltip>
                    )
                  }
                >
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                        <Typography
                          variant="body2"
                          sx={{ fontWeight: notif.lu ? 500 : 700, color: 'text.primary' }}
                        >
                          {notif.titre}
                        </Typography>
                        {!notif.lu && (
                          <Chip
                            label="Nouveau"
                            size="small"
                            color="primary"
                            sx={{ height: 18, fontSize: 10, fontWeight: 700, borderRadius: 1 }}
                          />
                        )}
                      </Box>
                    }
                    secondary={
                      <Box>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                          {notif.message}
                        </Typography>
                        <Typography variant="caption" color="text.disabled">
                          {new Date(notif.dateCreation).toLocaleString('fr-FR')}
                          {notif.lu && notif.dateLecture
                            ? ` · Lu le ${new Date(notif.dateLecture).toLocaleString('fr-FR')}`
                            : ''}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItem>
                {index < notifications.length - 1 && <Divider />}
              </Box>
            ))}
          </List>
        </Paper>
      )}
    </Box>
  );
}
