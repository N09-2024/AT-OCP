import { useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Stack, Chip, Divider } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import SecurityIcon from '@mui/icons-material/Security';
import MapIcon from '@mui/icons-material/Map';
import BusinessIcon from '@mui/icons-material/Business';
import BuildIcon from '@mui/icons-material/Build';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import HealthAndSafetyIcon from '@mui/icons-material/HealthAndSafety';
import ShieldIcon from '@mui/icons-material/Shield';
import EscalatorWarningIcon from '@mui/icons-material/EscalatorWarning';
import AssignmentCheckIcon from '@mui/icons-material/AssignmentTurnedIn';
import ApartmentIcon from '@mui/icons-material/Apartment';
import BadgeIcon from '@mui/icons-material/Badge';
import FormatListNumberedIcon from '@mui/icons-material/FormatListNumbered';
import { useAuthStore } from '../../../store/authStore';

export default function AdminDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const pilotageCards = [
    { title: 'Utilisateurs', subtitle: 'Comptes, validation, verrouillage', path: '/administration/utilisateurs', icon: <PeopleIcon />, color: '#1F4D3E' },
    { title: 'Rôles & permissions', subtitle: 'CE, HM, HC, ADMIN, RESPONSABLE_EXTERIEUR', path: '/administration/roles', icon: <SecurityIcon />, color: '#3C7A5C' },
    { title: 'Statistiques globales', subtitle: 'KPI AT par statut / zone / délai', path: '/administration/statistiques', icon: <AssessmentIcon />, color: '#A87532' },
    { title: 'Audit système', subtitle: 'Journal détaillé des actions', path: '/administration/audit', icon: <SecurityIcon />, color: '#5C6E67' },
    { title: 'Toutes les AT', subtitle: 'Liste complète, tous statuts', path: '/autorisations', icon: <AssessmentIcon />, color: '#3C7A5C' },
    { title: 'Paramètres', subtitle: 'Configuration système', path: '/administration/parametres', icon: <SettingsIcon />, color: '#3C7A5C' },
  ];

  const referentielCards = [
    { title: 'Zones', subtitle: 'Territoires (base logique P/E)', path: '/administration/zones', icon: <MapIcon />, color: '#1F4D3E' },
    { title: 'Services', subtitle: 'Services rattachés aux zones', path: '/administration/services', icon: <BusinessIcon />, color: '#3C7A5C' },
    { title: 'Équipements', subtitle: 'Équipements mobiles / fixes', path: '/administration/equipements', icon: <BuildIcon />, color: '#3C7A5C' },
    { title: 'Catalogue Risques', subtitle: 'Risques d’intervention', path: '/administration/risques', icon: <WarningAmberIcon />, color: '#9A3D2F' },
    { title: 'Mesures de prévention', subtitle: 'Mesures & préparations', path: '/administration/mesures-prevention', icon: <HealthAndSafetyIcon />, color: '#7FC8A9' },
    { title: 'EPI', subtitle: 'Protections individuelles', path: '/administration/epis', icon: <ShieldIcon />, color: '#1F4D3E' },
    { title: 'Moyens d’accès', subtitle: 'Accès zones de travail', path: '/administration/moyens-acces', icon: <EscalatorWarningIcon />, color: '#3C7A5C' },
    { title: 'Types de permis', subtitle: 'Feu, fouille, hauteur, etc.', path: '/administration/types-permis', icon: <AssignmentCheckIcon />, color: '#3C7A5C' },
    { title: 'Entreprises externes', subtitle: 'Sous-traitants OCP', path: '/administration/entreprises', icon: <ApartmentIcon />, color: '#A87532' },
    { title: 'Habilitations', subtitle: 'Liste F-HSE-SEC-31-02', path: '/habilitations', icon: <BadgeIcon />, color: '#3C7A5C' },
    { title: 'Registre Niveau 1', subtitle: 'Sans AT (F-HSE-SEC-31-01)', path: '/habilitations', icon: <FormatListNumberedIcon />, color: '#5C6E67' },
  ];

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Stack direction="row" spacing={1.5} sx={{ mb: 0.5, alignItems: 'center' }}>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
            Administration Système
          </Typography>
          <Chip label="Bypass guards métier" color="error" size="small" sx={{ fontWeight: 700, fontSize: 11 }} />
        </Stack>
        <Typography variant="body2" color="text.secondary">
          {user?.prenom} {user?.nom} — Administrateur (Accès total sur les espaces et référentiels)
        </Typography>
      </Box>

      {/* Section 1: PILOTAGE */}
      <Typography variant="h6" sx={{ fontWeight: 800, color: '#16241E', mb: 2 }}>
        PILOTAGE GLOBAL
      </Typography>
      <Grid container spacing={2.5} sx={{ mb: 5 }}>
        {pilotageCards.map((c) => (
          <Grid key={c.title} size={{ xs: 12, sm: 6, md: 4 }}>
            <Card
              elevation={0}
              sx={{
                height: '100%',
                borderRadius: 3,
                border: '1px solid #D6E3DC',
                borderTop: `4px solid ${c.color}`,
                transition: 'transform 0.15s ease',
                '&:hover': { transform: 'translateY(-2px)' },
              }}
            >
              <CardContent>
                <Stack direction="row" spacing={1.5} sx={{ mb: 1, alignItems: 'center' }}>
                  <Box sx={{ color: c.color }}>{c.icon}</Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>{c.title}</Typography>
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

      <Divider sx={{ my: 4 }} />

      {/* Section 2: RÉFÉRENTIELS */}
      <Typography variant="h6" sx={{ fontWeight: 800, color: '#16241E', mb: 2 }}>
        RÉFÉRENTIELS OCP & HSE
      </Typography>
      <Grid container spacing={2.5}>
        {referentielCards.map((c) => (
          <Grid key={c.title} size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
            <Card
              elevation={0}
              sx={{
                height: '100%',
                borderRadius: 3,
                border: '1px solid #D6E3DC',
                borderTop: `4px solid ${c.color}`,
                transition: 'transform 0.15s ease',
                '&:hover': { transform: 'translateY(-2px)' },
              }}
            >
              <CardContent>
                <Stack direction="row" spacing={1.5} sx={{ mb: 1, alignItems: 'center' }}>
                  <Box sx={{ color: c.color }}>{c.icon}</Box>
                  <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>{c.title}</Typography>
                </Stack>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                  {c.subtitle}
                </Typography>
              </CardContent>
              <CardActions sx={{ px: 2, pb: 1.5 }}>
                <Button size="small" onClick={() => navigate(c.path)} sx={{ fontWeight: 700, fontSize: 12 }}>
                  Gérer
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
