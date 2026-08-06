import { useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Stack, Chip, Divider } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import SecurityIcon from '@mui/icons-material/Security';
import MapIcon from '@mui/icons-material/Map';
import BusinessIcon from '@mui/icons-material/Business';
import FactoryIcon from '@mui/icons-material/Factory';
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
    { title: 'Utilisateurs', subtitle: 'Comptes, validation, verrouillage', path: '/administration/utilisateurs', icon: <PeopleIcon />, color: '#00875A' },
    { title: 'Rôles & permissions', subtitle: 'CE, HM, HC, ADMIN, RESPONSABLE_EXTERIEUR', path: '/administration/roles', icon: <SecurityIcon />, color: '#0284c7' },
    { title: 'Statistiques globales', subtitle: 'KPI AT par statut / zone / délai', path: '/administration/statistiques', icon: <AssessmentIcon />, color: '#d97706' },
    { title: 'Audit système', subtitle: 'Journal détaillé des actions', path: '/administration/audit', icon: <SecurityIcon />, color: '#64748b' },
    { title: 'Toutes les AT', subtitle: 'Liste complète, tous statuts', path: '/autorisations', icon: <AssessmentIcon />, color: '#059669' },
    { title: 'Paramètres', subtitle: 'Configuration système', path: '/administration/parametres', icon: <SettingsIcon />, color: '#7c3aed' },
  ];

  const referentielCards = [
    { title: 'Zones', subtitle: 'Territoires (base logique P/E)', path: '/administration/zones', icon: <MapIcon />, color: '#00875A' },
    { title: 'Services', subtitle: 'Services rattachés aux zones', path: '/administration/services', icon: <BusinessIcon />, color: '#0284c7' },
    { title: 'Installations', subtitle: 'Installations & sites OCP', path: '/administration/installations', icon: <FactoryIcon />, color: '#d97706' },
    { title: 'Équipements', subtitle: 'Équipements mobiles / fixes', path: '/administration/equipements', icon: <BuildIcon />, color: '#7c3aed' },
    { title: 'Catalogue Risques', subtitle: 'Risques d’intervention', path: '/administration/risques', icon: <WarningAmberIcon />, color: '#e11d48' },
    { title: 'Mesures de prévention', subtitle: 'Mesures & préparations', path: '/administration/mesures-prevention', icon: <HealthAndSafetyIcon />, color: '#10b981' },
    { title: 'EPI', subtitle: 'Protections individuelles', path: '/administration/epis', icon: <ShieldIcon />, color: '#3b82f6' },
    { title: 'Moyens d’accès', subtitle: 'Accès zones de travail', path: '/administration/moyens-acces', icon: <EscalatorWarningIcon />, color: '#8b5cf6' },
    { title: 'Types de permis', subtitle: 'Feu, fouille, hauteur, etc.', path: '/administration/types-permis', icon: <AssignmentCheckIcon />, color: '#ec4899' },
    { title: 'Entreprises externes', subtitle: 'Sous-traitants OCP', path: '/administration/entreprises', icon: <ApartmentIcon />, color: '#f59e0b' },
    { title: 'Habilitations', subtitle: 'Liste F-HSE-SEC-31-02', path: '/habilitations', icon: <BadgeIcon />, color: '#059669' },
    { title: 'Registre Niveau 1', subtitle: 'Sans AT (F-HSE-SEC-31-01)', path: '/habilitations', icon: <FormatListNumberedIcon />, color: '#475569' },
  ];

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Stack direction="row" spacing={1.5} sx={{ mb: 0.5, alignItems: 'center' }}>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f172a' }}>
            Administration Système
          </Typography>
          <Chip label="Bypass guards métier" color="error" size="small" sx={{ fontWeight: 700, fontSize: 11 }} />
        </Stack>
        <Typography variant="body2" color="text.secondary">
          {user?.prenom} {user?.nom} — Administrateur (Accès total sur les espaces et référentiels)
        </Typography>
      </Box>

      {/* Section 1: PILOTAGE */}
      <Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b', mb: 2 }}>
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
                border: '1px solid #e2e8f0',
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
      <Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b', mb: 2 }}>
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
                border: '1px solid #e2e8f0',
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
