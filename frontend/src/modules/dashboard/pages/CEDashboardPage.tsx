import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Stack,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditNoteIcon from '@mui/icons-material/EditNote';
import ConstructionIcon from '@mui/icons-material/Construction';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FlagIcon from '@mui/icons-material/Flag';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import { useAuthStore } from '../../../store/authStore';

/**
 * Dashboard CE — Chef d'Équipe
 * Position P (CEEP) ou E (CEEE) selon le territoire de chaque AT.
 */
export default function CEDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const cards = [
    {
      title: 'À rédiger (P)',
      subtitle: 'AT Propriétaire à rédiger — F-HSE-SEC-31-04',
      color: '#00875A',
      icon: <EditNoteIcon />,
      path: '/autorisations?filtre=a-rediger',
      action: 'Ouvrir',
    },
    {
      title: 'À viser (E)',
      subtitle: 'Position E — signature visa CEEE',
      color: '#0284c7',
      icon: <CheckCircleIcon />,
      path: '/autorisations?filtre=a-viser',
      action: 'Viser',
    },
    {
      title: 'À démarrer (E)',
      subtitle: 'Position E — démarrer l’intervention',
      color: '#d97706',
      icon: <PlayArrowIcon />,
      path: '/autorisations?filtre=a-demarrer',
      action: 'Démarrer',
    },
    {
      title: 'En cours',
      subtitle: 'Interventions actives — Déclarer fin / incident / reconduire',
      color: '#7c3aed',
      icon: <ConstructionIcon />,
      path: '/autorisations?filtre=en-cours',
      action: 'Gérer',
    },
    {
      title: 'À réceptionner (P)',
      subtitle: 'Fin déclarée — Lancer réception conjointe',
      color: '#059669',
      icon: <FlagIcon />,
      path: '/autorisations?filtre=a-receptionner',
      action: 'Réceptionner',
    },
    {
      title: 'Mes AT (Tous statuts)',
      subtitle: 'Liste complète filtrée P + E',
      color: '#64748b',
      icon: <FormatListBulletedIcon />,
      path: '/autorisations',
      action: 'Voir tout',
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f172a' }}>
            Espace Chef d’Équipe
          </Typography>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 0.5 }}>
            <Typography variant="body2" color="text.secondary">
              {user?.prenom} {user?.nom} — {user?.service?.nomService || 'Service CE'}
            </Typography>
            <Chip
              size="small"
              label="Position P ou E selon l’AT"
              color="success"
              variant="outlined"
              sx={{ fontWeight: 700, fontSize: 11 }}
            />
          </Stack>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/autorisations/nouvelle')}
          sx={{ bgcolor: '#00875A', fontWeight: 700, borderRadius: 2, px: 3 }}
        >
          Nouvelle demande d’intervention
        </Button>
      </Stack>

      <Grid container spacing={2.5}>
        {cards.map((c) => (
          <Grid key={c.title} size={{ xs: 12, sm: 6, md: 4 }}>
            <Card
              elevation={0}
              sx={{
                height: '100%',
                border: '1px solid #e2e8f0',
                borderRadius: 3,
                borderTop: `4px solid ${c.color}`,
              }}
            >
              <CardContent>
                <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                  <Box sx={{ color: c.color }}>{c.icon}</Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>
                    {c.title}
                  </Typography>
                </Stack>
                <Typography variant="body2" color="text.secondary">
                  {c.subtitle}
                </Typography>
              </CardContent>
              <CardActions sx={{ px: 2, pb: 2 }}>
                <Button size="small" onClick={() => navigate(c.path)} sx={{ fontWeight: 700, color: c.color }}>
                  {c.action}
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
