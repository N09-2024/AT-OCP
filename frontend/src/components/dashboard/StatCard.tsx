import { Card, Box, Typography } from '@mui/material';
import type { ReactNode } from 'react';
import { ArrowRightIcon } from '@heroicons/react/24/outline';
import { Link } from 'react-router-dom';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle: string;
  icon: ReactNode;
  linkTo: string;
}

export default function StatCard({ title, value, subtitle, icon, linkTo }: StatCardProps) {
  return (
    <Card sx={{ p: 3, display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', mb: 2 }}>
        <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: 'primary.light', color: 'primary.main', display: 'flex' }}>
          {icon}
        </Box>
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ fontWeight: 'bold', textTransform: 'uppercase', letterSpacing: 0.5 }}
        >
          {title}
        </Typography>
      </Box>
      <Box sx={{ flexGrow: 1 }}>
        <Typography variant="h3" color="text.primary" sx={{ fontWeight: 'bold' }}>
          {value}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
          {subtitle}
        </Typography>
      </Box>
      <Box
        component={Link}
        to={linkTo}
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          mt: 3,
          color: 'primary.main',
          textDecoration: 'none',
          fontWeight: 500,
          fontSize: '0.875rem',
          '&:hover': { textDecoration: 'underline' },
        }}
      >
        Voir plus <ArrowRightIcon width={16} />
      </Box>
    </Card>
  );
}
