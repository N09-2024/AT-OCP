import { Box, Typography, Paper, Button, Chip } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import {
  CloudArrowUpIcon,
  CameraIcon,
  MagnifyingGlassCircleIcon,
} from '@heroicons/react/24/outline';

export default function ResponsableEntrepriseDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const actions = [
    {
      title: 'Importer un permis',
      description: 'Téléversez un fichier PDF ou image d\'un permis de travail pour analyse automatique par l\'IA',
      icon: <CloudArrowUpIcon width={32} color="#2E624A" />,
      color: '#2E624A',
      bgcolor: '#2E624A18',
      path: '/permis/importer',
      label: 'Importer',
    },
    {
      title: 'Photographier un permis',
      description: 'Prenez une photo directement avec votre appareil photo pour un traitement immédiat',
      icon: <CameraIcon width={32} color="#3C7A5C" />,
      color: '#3C7A5C',
      bgcolor: '#3C7A5C18',
      path: '/permis/photographier',
      label: 'Photographier',
    },
    {
      title: 'Consulter les résultats',
      description: 'Visualisez les résultats d\'analyse IA — conformité, informations extraites, taux de confiance',
      icon: <MagnifyingGlassCircleIcon width={32} color="#3C7A5C" />,
      color: '#3C7A5C',
      bgcolor: '#3C7A5C18',
      path: '/permis',
      label: 'Voir les résultats',
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
          <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary' }}>
            Bonjour, {user?.prenom} 👋
          </Typography>
          <Chip
            label="Responsable Entreprise"
            size="small"
            sx={{ bgcolor: '#2E624A18', color: '#2E624A', fontWeight: 700 }}
          />
        </Box>
        <Typography variant="body1" color="text.secondary">
          Gérez vos permis de travail et consultez les analyses IA
        </Typography>
      </Box>

      {/* Bandeau IA */}
      <Paper
        sx={{
          p: 4,
          mb: 4,
          borderRadius: 3,
          background: 'linear-gradient(135deg, #2E624A 0%, #2E624A 100%)',
          color: 'white',
          boxShadow: '0 8px 24px rgba(46,98,74,0.3)',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            🤖 Analyse IA des permis de travail
          </Typography>
        </Box>
        <Typography variant="body2" sx={{ opacity: 0.9, mb: 1 }}>
          Le système analyse automatiquement vos permis par OCR et intelligence artificielle pour extraire
          les informations clés et vérifier leur conformité réglementaire.
        </Typography>
        <Typography variant="caption" sx={{ opacity: 0.75 }}>
          • Extraction automatique des données • Vérification de conformité • Taux de confiance en temps réel
        </Typography>
      </Paper>

      {/* 3 actions principales */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 3 }}>
        {actions.map((action) => (
          <Paper
            key={action.title}
            sx={{
              p: 3,
              borderRadius: 3,
              border: '1px solid',
              borderColor: 'divider',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              textAlign: 'center',
              gap: 2,
              cursor: 'pointer',
              transition: 'all 0.2s',
              '&:hover': {
                borderColor: action.color,
                boxShadow: `0 8px 24px ${action.color}20`,
                transform: 'translateY(-4px)',
              },
            }}
            onClick={() => navigate(action.path)}
          >
            <Box
              sx={{
                width: 64,
                height: 64,
                borderRadius: '50%',
                bgcolor: action.bgcolor,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              {action.icon}
            </Box>
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 0.5 }}>
                {action.title}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ fontSize: 13 }}>
                {action.description}
              </Typography>
            </Box>
            <Button
              variant="outlined"
              size="small"
              sx={{
                borderColor: action.color,
                color: action.color,
                fontWeight: 600,
                borderRadius: 2,
                '&:hover': { bgcolor: action.bgcolor },
                mt: 'auto',
              }}
              onClick={(e) => { e.stopPropagation(); navigate(action.path); }}
            >
              {action.label}
            </Button>
          </Paper>
        ))}
      </Box>
    </Box>
  );
}
