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
  Divider,
  Alert,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditNoteIcon from '@mui/icons-material/EditNote';
import ConstructionIcon from '@mui/icons-material/Construction';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FlagIcon from '@mui/icons-material/Flag';
import FormatListBulletedIcon from '@mui/icons-material/FormatListBulleted';
import InboxIcon from '@mui/icons-material/Inbox';
import DrawIcon from '@mui/icons-material/Draw';
import { useAuthStore } from '../../../store/authStore';

/**
 * Dashboard CE - Chef d'Équipe
 * Affiche des cartes différentes selon la position P (CEEP) ou E (CEEE).
 * Un CEEE ne voit PAS les actions réservées au CEEP et vice-versa.
 */
export default function CEDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const roles = user?.roles?.map((r: any) => r.nom) || [];
  const isCeep = roles.some((r: string) => r === 'CEEP' || r === 'CE');
  const isCeee = roles.some((r: string) => r === 'CEEE');
  const isBoth = isCeep && isCeee;

  // Cartes CEEP (Position P - Propriétaire)
  const cardsCeep = [
    {
      title: 'Créer une nouvelle AT',
      subtitle: 'Rédiger une Autorisation de Travail (F-HSE-SEC-31-04)',
      color: '#1F4D3E',
      icon: <AddIcon />,
      path: '/autorisations/nouvelle',
      action: 'Créer',
    },
    {
      title: 'Mes AT en brouillon (P)',
      subtitle: 'Reprendre la rédaction de vos AT non soumises',
      color: '#A87532',
      icon: <EditNoteIcon />,
      path: '/autorisations?filtre=BROUILLON',
      action: 'Reprendre',
    },
    {
      title: 'Mes AT soumises (P)',
      subtitle: 'AT transmises au CEEE et en attente de visa',
      color: '#3C7A5C',
      icon: <ConstructionIcon />,
      path: '/autorisations?filtre=SOUMISE',
      action: 'Suivre',
    },
    {
      title: 'À réceptionner (P)',
      subtitle: 'Fin déclarée - Lancer la réception conjointe',
      color: '#3C7A5C',
      icon: <FlagIcon />,
      path: '/autorisations?filtre=FIN_TRAVAUX_DECLAREE',
      action: 'Réceptionner',
    },
    {
      title: 'Toutes mes AT (P)',
      subtitle: 'Historique complet de vos autorisations émises',
      color: '#5C6E67',
      icon: <FormatListBulletedIcon />,
      path: '/autorisations',
      action: 'Voir tout',
    },
  ];

  // Cartes CEEE (Position E - Exécutant)
  const cardsCeee = [
    {
      title: 'AT à réceptionner (E)',
      subtitle: 'Consultez et accusez réception des AT transmises à votre service',
      color: '#3C7A5C',
      icon: <InboxIcon />,
      path: '/receptions/ceee',
      action: 'Voir les AT reçues',
      highlight: true,
    },
    {
      title: 'AT à signer (E)',
      subtitle: 'Apposez votre visa sur les AT dont vous avez accusé réception',
      color: '#1F4D3E',
      icon: <DrawIcon />,
      path: '/receptions/ceee',
      action: 'Viser & Signer',
    },
    {
      title: 'Interventions actives (E)',
      subtitle: 'Interventions en cours - Déclarer fin ou incident',
      color: '#3C7A5C',
      icon: <PlayArrowIcon />,
      path: '/autorisations?filtre=VALIDEE',
      action: 'Gérer',
    },
    {
      title: 'Historique (E)',
      subtitle: 'Toutes les AT liées à votre service exécutant',
      color: '#5C6E67',
      icon: <FormatListBulletedIcon />,
      path: '/autorisations',
      action: 'Voir',
    },
  ];

  const renderCards = (cards: typeof cardsCeep, label: string, color: string) => (
    <Box sx={{ mb: 4 }}>
      <Stack direction="row" spacing={1} sx={{ mb: 2, alignItems: 'center' }}>
        <Chip label={label} size="small" sx={{ bgcolor: color, color: 'white', fontWeight: 800, fontSize: 12, px: 1 }} />
        <Typography variant="subtitle2" color="text.secondary" sx={{ fontWeight: 600 }}>
          {label === 'Position P - Propriétaire' ? "Actions réservées au CEEP" : "Actions réservées au CEEE"}
        </Typography>
      </Stack>
      <Grid container spacing={2.5}>
        {cards.map((c) => (
          <Grid key={c.title} size={{ xs: 12, sm: 6, md: 4 }}>
            <Card
              elevation={0}
              sx={{
                height: '100%',
                border: (c as any).highlight ? `2px solid ${c.color}` : '1px solid #D6E3DC',
                borderRadius: 3,
                borderTop: `4px solid ${c.color}`,
                bgcolor: (c as any).highlight ? `${c.color}08` : 'white',
              }}
            >
              <CardContent>
                <Stack direction="row" spacing={1} sx={{ mb: 1, alignItems: 'center' }}>
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
                <Button
                  size="small"
                  variant={(c as any).highlight ? 'contained' : 'text'}
                  onClick={() => navigate(c.path)}
                  sx={{
                    fontWeight: 700,
                    color: (c as any).highlight ? 'white' : c.color,
                    bgcolor: (c as any).highlight ? c.color : 'transparent',
                    '&:hover': { bgcolor: (c as any).highlight ? '#2E624A' : `${c.color}15` },
                    textTransform: 'none',
                    borderRadius: 2,
                  }}
                >
                  {c.action}
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );

  return (
    <Box sx={{ p: 3 }}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
            Espace Chef d'Équipe
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 0.5, alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {user?.prenom} {user?.nom} - {user?.service?.nomService || 'Service CE'}
            </Typography>
            {isBoth && (
              <Chip size="small" label="CEEP + CEEE" color="warning" variant="outlined" sx={{ fontWeight: 700, fontSize: 11 }} />
            )}
            {isCeep && !isBoth && (
              <Chip size="small" label="Position P - Propriétaire" color="success" variant="outlined" sx={{ fontWeight: 700, fontSize: 11 }} />
            )}
            {isCeee && !isBoth && (
              <Chip size="small" label="Position E - Exécutant" color="primary" variant="outlined" sx={{ fontWeight: 700, fontSize: 11 }} />
            )}
          </Stack>
        </Box>

        {isCeep && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/autorisations/nouvelle')}
            sx={{ bgcolor: '#1F4D3E', fontWeight: 700, borderRadius: 2, px: 3 }}
          >
            Nouvelle AT
          </Button>
        )}
      </Stack>

      {/* Alerte pour les rôles doubles */}
      {isBoth && (
        <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
          Vous avez les deux rôles (CEEP + CEEE). Les sections ci-dessous sont séparées par position pour chaque AT.
        </Alert>
      )}

      {/* Section CEEP (Position P) */}
      {isCeep && renderCards(cardsCeep, 'Position P - Propriétaire', '#1F4D3E')}

      {isBoth && <Divider sx={{ my: 2 }} />}

      {/* Section CEEE (Position E) */}
      {isCeee && renderCards(cardsCeee, 'Position E - Exécutant', '#3C7A5C')}

      {/* Cas: rôle CE générique - afficher les deux sections */}
      {!isCeep && !isCeee && renderCards([...cardsCeep, ...cardsCeee], 'Chef d\'Équipe (CE)', '#5C6E67')}
    </Box>
  );
}
