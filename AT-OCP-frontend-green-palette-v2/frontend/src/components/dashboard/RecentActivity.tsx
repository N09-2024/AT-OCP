import { Card, Box, Typography, CircularProgress } from '@mui/material';
import { DocumentCheckIcon, ClipboardDocumentIcon, ExclamationCircleIcon, BellIcon } from '@heroicons/react/24/outline';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { NotificationService, type NotificationItem } from '../../services/NotificationService';
import { formatDistanceToNow } from 'date-fns';
import { fr } from 'date-fns/locale';

export default function RecentActivity() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    NotificationService.getMyNotifications(0, 5)
      .then((page) => setNotifications(page.content))
      .catch((err) => console.error("Error loading notifications", err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <Card sx={{ p: 3, display: 'flex', flexDirection: 'column', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
        <CircularProgress size={30} color="success" />
      </Card>
    );
  }

  return (
    <Card sx={{ p: 3, display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 3 }}>
        Activités récentes
      </Typography>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, flexGrow: 1 }}>
        {notifications.length === 0 ? (
          <Typography color="text.secondary">Aucune activité récente.</Typography>
        ) : (
          notifications.map((activity, idx) => (
            <Box
              key={activity.id}
              sx={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: 2,
                pb: idx < notifications.length - 1 ? 2 : 0,
                borderBottom: idx < notifications.length - 1 ? '1px solid' : 'none',
                borderColor: 'divider',
              }}
            >
              <Box
                sx={{
                  p: 1,
                  borderRadius: 2,
                  bgcolor: '#E2F0E8',
                  color: '#3C7A5C',
                  display: 'flex',
                  flexShrink: 0,
                }}
              >
                <BellIcon width={20} />
              </Box>
              <Box>
                <Typography variant="body2" color="text.primary">
                  {activity.titre} - {activity.message}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {formatDistanceToNow(new Date(activity.dateCreation), { addSuffix: true, locale: fr })}
                </Typography>
              </Box>
            </Box>
          ))
        )}
      </Box>

      <Box 
        component={Link}
        to="/notifications"
        sx={{ mt: 3, color: 'primary.main', textDecoration: 'none', fontWeight: 500, fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: 0.5, '&:hover': { textDecoration: 'underline' } }}
      >
        Voir toutes les activités →
      </Box>
    </Card>
  );
}

