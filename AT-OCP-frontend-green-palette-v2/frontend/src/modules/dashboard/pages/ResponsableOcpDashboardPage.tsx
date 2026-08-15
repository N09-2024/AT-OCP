import { Box, Typography, Paper, Button, Chip } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import {
  ClipboardDocumentListIcon,
  CheckBadgeIcon,
  XCircleIcon,
  ArchiveBoxIcon,
  ShieldCheckIcon,
} from '@heroicons/react/24/outline';

const actionCards = [
  {
    title: 'À signer',
    description: 'Autorisations en attente de votre signature',
    icon: <CheckBadgeIcon width={28} color="#3C7A5C" />,
    color: '#3C7A5C',
    bgcolor: '#3C7A5C18',
    path: '/autorisations?filtre=a-signer',
  },
  {
    title: 'À valider / Rejeter',
    description: 'Vérifier et statuer sur les AT soumises',
    icon: <ClipboardDocumentListIcon width={28} color="#3C7A5C" />,
    color: '#3C7A5C',
    bgcolor: '#3C7A5C18',
    path: '/autorisations?filtre=a-valider',
  },
  {
    title: 'Réceptionner travaux',
    description: 'Confirmer la fin des travaux sur site',
    icon: <ArchiveBoxIcon width={28} color="#2E624A" />,
    color: '#2E624A',
    bgcolor: '#2E624A18',
    path: '/receptions',
  },
  {
    title: 'Rejeter une AT',
    description: 'AT nécessitant des corrections',
    icon: <XCircleIcon width={28} color="#9A3D2F" />,
    color: '#9A3D2F',
    bgcolor: '#9A3D2F18',
    path: '/autorisations?filtre=rejetes',
  },
];

export default function ResponsableOcpDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
          <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary' }}>
            Bonjour, {user?.prenom} 👋
          </Typography>
          <Chip
            label="Responsable OCP"
            size="small"
            sx={{ bgcolor: '#3C7A5C18', color: '#3C7A5C', fontWeight: 700 }}
          />
        </Box>
        <Typography variant="body1" color="text.secondary">
          Consultez et traitez les autorisations de travail en attente
        </Typography>
      </Box>

      {/* Bandeau principal */}
      <Paper
        sx={{
          p: 4,
          mb: 3,
          borderRadius: 3,
          background: 'linear-gradient(135deg, #3C7A5C 0%, #2E624A 100%)',
          color: 'white',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 8px 24px rgba(60,122,92,0.3)',
        }}
      >
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>
            Autorisations en attente de traitement
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.85 }}>
            Signer · Vérifier · Valider · Rejeter · Réceptionner les travaux
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<ClipboardDocumentListIcon width={20} />}
          onClick={() => navigate('/autorisations')}
          sx={{
            bgcolor: 'white',
            color: '#3C7A5C',
            fontWeight: 700,
            borderRadius: 2,
            px: 3,
            whiteSpace: 'nowrap',
            flexShrink: 0,
            ml: 3,
            '&:hover': { bgcolor: 'rgba(255,255,255,0.9)' },
          }}
        >
          Voir toutes les AT
        </Button>
      </Paper>

      {/* Action cards */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 3, mb: 3 }}>
        {actionCards.map((card) => (
          <Paper
            key={card.title}
            sx={{
              p: 3,
              borderRadius: 3,
              border: '1px solid',
              borderColor: 'divider',
              cursor: 'pointer',
              transition: 'all 0.2s',
              '&:hover': {
                borderColor: card.color,
                boxShadow: `0 4px 12px ${card.color}20`,
                transform: 'translateY(-2px)',
              },
            }}
            onClick={() => navigate(card.path)}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ p: 1.5, bgcolor: card.bgcolor, borderRadius: 2 }}>
                {card.icon}
              </Box>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>{card.title}</Typography>
                <Typography variant="body2" color="text.secondary">{card.description}</Typography>
              </Box>
            </Box>
          </Paper>
        ))}
      </Box>

      {/* Permis */}
      <Paper
        sx={{
          p: 3,
          borderRadius: 3,
          border: '1px solid',
          borderColor: 'divider',
          cursor: 'pointer',
          transition: 'all 0.2s',
          '&:hover': { borderColor: '#3C7A5C', boxShadow: '0 4px 12px rgba(60,122,92,0.1)' },
        }}
        onClick={() => navigate('/permis')}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ p: 1.5, bgcolor: '#3C7A5C18', borderRadius: 2 }}>
            <ShieldCheckIcon width={28} color="#3C7A5C" />
          </Box>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>Consulter les permis</Typography>
            <Typography variant="body2" color="text.secondary">
              Voir les permis importés et les résultats d'analyse IA
            </Typography>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}
