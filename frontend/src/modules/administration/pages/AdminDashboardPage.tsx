import { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  CircularProgress,
  Card,
  CardContent,
  Chip,
} from '@mui/material';
import {
  UsersIcon,
  ShieldCheckIcon,
  ExclamationTriangleIcon,
  ClockIcon,
} from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';
import type { AdminStats } from '../../../services/AdminService';

export default function AdminDashboardPage() {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    AdminService.getAdminStats()
      .then(setStats)
      .catch((err) => console.error('Erreur chargement stats admin', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  const STAT_CARDS = [
    {
      title: 'Utilisateurs',
      value: stats?.totalUsers ?? 0,
      subtitle: `${stats?.activeUsers ?? 0} actifs`,
      icon: <UsersIcon width={28} />,
      color: '#3B82F6',
    },
    {
      title: 'Rôles',
      value: stats?.totalRoles ?? 0,
      subtitle: 'Configurés',
      icon: <ShieldCheckIcon width={28} />,
      color: '#10B981',
    },
    {
      title: 'Actions en attente',
      value: stats?.pendingActions ?? 0,
      subtitle: 'Nécessitent attention',
      icon: <ExclamationTriangleIcon width={28} />,
      color: '#F59E0B',
    },
    {
      title: 'Connexions récentes',
      value: stats?.recentLogins ?? 0,
      subtitle: '24 dernières heures',
      icon: <ClockIcon width={28} />,
      color: '#8B5CF6',
    },
  ];

  return (
    <Box>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          Administration
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Vue d'ensemble du système et gestion des accès
        </Typography>
      </Box>

      {/* Stat cards */}
      <Box sx={{ display: 'flex', gap: 3, mb: 4, flexWrap: 'wrap' }}>
        {STAT_CARDS.map((card) => (
          <Card
            key={card.title}
            sx={{
              flex: '1 1 200px',
              minWidth: 180,
              borderRadius: 3,
              boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
            }}
          >
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                <Box
                  sx={{
                    width: 48,
                    height: 48,
                    borderRadius: 2,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    bgcolor: `${card.color}15`,
                    color: card.color,
                  }}
                >
                  {card.icon}
                </Box>
              </Box>
              <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 0.5 }}>
                {card.value}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {card.title}
              </Typography>
              <Chip
                label={card.subtitle}
                size="small"
                sx={{
                  mt: 1,
                  bgcolor: `${card.color}10`,
                  color: card.color,
                  fontSize: 11,
                  fontWeight: 500,
                }}
              />
            </CardContent>
          </Card>
        ))}
      </Box>

      {/* Quick actions info */}
      <Card sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
            Actions rapides
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <Chip
              label="Gérer les utilisateurs"
              component="a"
              href="/administration/utilisateurs"
              clickable
              variant="outlined"
              color="primary"
              sx={{ borderRadius: 2, fontWeight: 500 }}
            />
            <Chip
              label="Gérer les rôles"
              component="a"
              href="/administration/roles"
              clickable
              variant="outlined"
              color="primary"
              sx={{ borderRadius: 2, fontWeight: 500 }}
            />
            <Chip
              label="Paramètres système"
              component="a"
              href="/administration/parametres"
              clickable
              variant="outlined"
              color="primary"
              sx={{ borderRadius: 2, fontWeight: 500 }}
            />
            <Chip
              label="Journal d'activité"
              component="a"
              href="/administration/audit"
              clickable
              variant="outlined"
              color="primary"
              sx={{ borderRadius: 2, fontWeight: 500 }}
            />
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}