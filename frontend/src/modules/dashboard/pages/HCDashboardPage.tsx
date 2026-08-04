import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Stack,
} from '@mui/material';
import CategoryIcon from '@mui/icons-material/Category';
import GavelIcon from '@mui/icons-material/Gavel';
import ArchiveIcon from '@mui/icons-material/Archive';
import BadgeIcon from '@mui/icons-material/Badge';
import ListAltIcon from '@mui/icons-material/ListAlt';
import { useAuthStore } from '../../../store/authStore';

/**
 * Dashboard HC — Hors Cadre
 * Position P (HCEP) : classification, archivage (garant), habilitations
 * Position E (HCEE) : garant visite / rédaction / démarrage / visa, exécute archivage
 */
export default function HCDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const cards = [
    {
      title: 'Classifier une intervention',
      subtitle: 'Étape 0 — Niveau 1 ou Niveau 2 (§6)',
      icon: <CategoryIcon />,
      color: '#00875A',
      path: '/autorisations',
      // Le dialog classification est sur AutorisationListPage
    },
    {
      title: 'AT en attente de garantie',
      subtitle: 'Position E — visite, rédaction, démarrage, visa',
      icon: <GavelIcon />,
      color: '#0284c7',
      path: '/autorisations?filtre=a-valider',
    },
    {
      title: 'AT à archiver',
      subtitle: '§8.6 — garant (P) / exécute (E)',
      icon: <ArchiveIcon />,
      color: '#7c3aed',
      path: '/archives',
    },
    {
      title: 'Agents habilités',
      subtitle: 'Liste F-HSE-SEC-31-02',
      icon: <BadgeIcon />,
      color: '#d97706',
      path: '/habilitations',
    },
    {
      title: 'Registre Niveau 1',
      subtitle: 'Interventions sans AT — F-HSE-SEC-31-01',
      icon: <ListAltIcon />,
      color: '#64748b',
      path: '/habilitations',
    },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" sx={{ fontWeight: 800, mb: 1, color: '#0f172a' }}>
        Espace Hors Cadre
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        {user?.prenom} {user?.nom} — HC (HCEP en position P · HCEE en position E)
      </Typography>

      <Grid container spacing={2.5}>
        {cards.map((c) => (
          <Grid key={c.title} size={{ xs: 12, sm: 6, md: 4 }}>
            <Card
              elevation={0}
              sx={{
                height: '100%',
                borderRadius: 3,
                border: '1px solid #e2e8f0',
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
                <Button size="small" onClick={() => navigate(c.path)} sx={{ fontWeight: 700 }}>
                  Ouvrir
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
