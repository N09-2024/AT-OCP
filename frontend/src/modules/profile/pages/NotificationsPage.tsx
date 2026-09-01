import { useState } from 'react';
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
} from '@heroicons/react/24/outline';
import { useNotifications } from '../../../hooks/useNotifications';
import type { NotificationItem } from '../../../services/NotificationService';

export default function NotificationsPage() {
  const navigate = useNavigate();
  const { notifications, unreadCount, loading, refresh, markAsRead, markAllAsRead } = useNotifications();
  const [markingAll, setMarkingAll] = useState(false);

  const handleMarkAllAsRead = async () => {
    setMarkingAll(true);
    try {
      await markAllAsRead();
    } finally {
      setMarkingAll(false);
    }
  };

  const handleNotificationClick = async (notif: NotificationItem) => {
    if (!notif.lu) {
      await markAsRead(notif.id);
    }
    if (notif.lien) {
      let target = notif.lien;
      if (target.startsWith('/at/')) {
        target = target.replace('/at/', '/autorisations/');
      }
      navigate(target);
    }
  };

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
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            variant="outlined"
            size="small"
            onClick={refresh}
            sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
          >
            Actualiser
          </Button>
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
                            onClick={(e) => {
                              e.stopPropagation();
                              markAsRead(notif.id);
                            }}
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
