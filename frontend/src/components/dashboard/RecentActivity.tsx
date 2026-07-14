import { Card, Box, Typography } from '@mui/material';
import { DocumentCheckIcon, ClipboardDocumentIcon, ExclamationCircleIcon } from '@heroicons/react/24/outline';
import type { ReactNode } from 'react';

interface Activity {
  id: number;
  text: string;
  time: string;
  bgColor: string;
  iconColor: string;
  icon: ReactNode;
}

const activities: Activity[] = [
  {
    id: 1,
    text: 'AT-2026-1258 a été validée par Ahmed El Amrani',
    time: 'Il y a 25 min',
    bgColor: '#EAF7EF',
    iconColor: '#16A34A',
    icon: <DocumentCheckIcon width={20} />,
  },
  {
    id: 2,
    text: 'Visa requis pour AT-2026-1255',
    time: 'Il y a 1 heure',
    bgColor: '#FEF3C7',
    iconColor: '#D97706',
    icon: <ClipboardDocumentIcon width={20} />,
  },
  {
    id: 3,
    text: 'Permis de feu PFE-2026-045 expire demain',
    time: 'Il y a 2 heures',
    bgColor: '#FEE2E2',
    iconColor: '#DC2626',
    icon: <ExclamationCircleIcon width={20} />,
  },
];

export default function RecentActivity() {
  return (
    <Card sx={{ p: 3, display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 3 }}>
        Activités récentes
      </Typography>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {activities.map((activity, idx) => (
          <Box
            key={activity.id}
            sx={{
              display: 'flex',
              alignItems: 'flex-start',
              gap: 2,
              pb: idx < activities.length - 1 ? 2 : 0,
              borderBottom: idx < activities.length - 1 ? '1px solid' : 'none',
              borderColor: 'divider',
            }}
          >
            <Box
              sx={{
                p: 1,
                borderRadius: 2,
                bgcolor: activity.bgColor,
                color: activity.iconColor,
                display: 'flex',
                flexShrink: 0,
              }}
            >
              {activity.icon}
            </Box>
            <Box>
              <Typography variant="body2" color="text.primary">
                {activity.text}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {activity.time}
              </Typography>
            </Box>
          </Box>
        ))}
      </Box>

      <Box sx={{ mt: 3, color: 'primary.main', cursor: 'pointer', fontWeight: 500, fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: 0.5 }}>
        Voir toutes les activités →
      </Box>
    </Card>
  );
}
