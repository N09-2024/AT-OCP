import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
import {
  BellIcon,
  CheckIcon,
  EnvelopeOpenIcon,
  ArrowTopRightOnSquareIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  InformationCircleIcon,
} from '@heroicons/react/24/outline';
import { NotificationService, type NotificationItem } from '../../../services/NotificationService';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [markingAll, setMarkingAll] = useState(false);
  const navigate = useNavigate();

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

  const handleMarkAsRead = async (id: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    await NotificationService.markAsRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, lu: true, dateLecture: new Date().toISOString() } : n))
    );
  };

  const handleMarkAllAsRead = async () => {
    setMarkingAll(true);
    try {
      await NotificationService.markAllAsRead();
      setNotifications((prev) =>
        prev.map((n) => ({ ...n, lu: true, dateLecture: n.dateLecture ?? new Date().toISOString() }))
      );
    } catch (err) {
      console.error('Erreur markAllAsRead', err);
    } finally {
      setMarkingAll(false);
    }
  };

  const handleNotificationClick = async (notif: NotificationItem) => {
    if (!notif.lu) {
      await handleMarkAsRead(notif.id);
    }
    if (notif.lien) {
      // Normaliser les URLs relatives backend (ex: /at/123 -> /autorisations/123)
      let target = notif.lien;
      if (target.startsWith('/at/')) {
        target = target.replace('/at/', '/autorisations/');
      }
      navigate(target);
    }
  };

  const unreadCount = notifications.filter((n) => !n.lu).length;

  const getTypeChip = (type?: string) => {
    switch (type?.toUpperCase()) {
      case 'ACTION':
        return <Chip label="Action requise" size="small" color="warning" sx={{ height: 20, fontSize: 10, fontWeight: 700 }} />;
      case 'SUCCESS':
        return <Chip label="Succès" size="small" color="success" sx={{ height: 20, fontSize: 10, fontWeight: 700 }} />;
      case 'WARNING':
      case 'ERROR':
        return <Chip label="Important" size="small" color="error" sx={{ height: 20, fontSize: 10, fontWeight: 700 }} />;
      default:
        return <Chip label="Information" size="small" color="default" sx={{ height: 20, fontSize: 10, fontWeight: 600 }} />;
    }
  };

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
              bgcolor: '#EDF2EE',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <BellIcon width={22} color="#2E624A" />
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
                  onClick={() => handleNotificationClick(notif)}
                  sx={{
                    px: 3,
                    py: 2,
                    cursor: notif.lien ? 'pointer' : 'default',
                    bgcolor: notif.lu ? 'transparent' : 'rgba(46, 98, 74, 0.05)',
                    borderLeft: notif.lu ? '3px solid transparent' : '3px solid',
                    borderColor: notif.lu ? 'transparent' : 'primary.main',
                    transition: 'all 0.2s',
                    '&:hover': { bgcolor: notif.lu ? 'action.hover' : 'rgba(46, 98, 74, 0.1)' },
                  }}
                  secondaryAction={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {notif.lien && (
                        <Tooltip title="Ouvrir l'autorisation">
                          <IconButton
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleNotificationClick(notif);
                            }}
                            sx={{ color: 'primary.main' }}
                          >
                            <ArrowTopRightOnSquareIcon width={18} />
                          </IconButton>
                        </Tooltip>
                      )}
                      {!notif.lu && (
                        <Tooltip title="Marquer comme lu">
                          <IconButton
                            size="small"
                            onClick={(e) => handleMarkAsRead(notif.id, e)}
                            sx={{ color: '#5C6E67' }}
                          >
                            <EnvelopeOpenIcon width={18} />
                          </IconButton>
                        </Tooltip>
                      )}
                    </Box>
                  }
                >
                  <ListItemText
                    disableTypography
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5, flexWrap: 'wrap' }}>
                        <Typography
                          variant="body2"
                          sx={{ fontWeight: notif.lu ? 600 : 800, color: 'text.primary' }}
                        >
                          {notif.titre}
                        </Typography>
                        {getTypeChip(notif.type)}
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
                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{ mb: 0.5, whiteSpace: 'pre-line' }}
                        >
                          {notif.message}
                        </Typography>
                        <Typography variant="caption" color="text.disabled">
                          {new Date(notif.dateCreation).toLocaleString('fr-FR')}
                          {notif.lu && notif.dateLecture
                            ? ` • Lu le ${new Date(notif.dateLecture).toLocaleString('fr-FR')}`
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
