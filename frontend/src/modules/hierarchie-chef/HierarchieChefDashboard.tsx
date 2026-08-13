import React from 'react';
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
  Alert,
  Divider,
} from '@mui/material';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import SecurityIcon from '@mui/icons-material/Security';
import HistoryIcon from '@mui/icons-material/History';
import { useAuthStore } from '../../store/authStore';

export const HierarchieChefDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const cardsExecutant = [
    {
      title: 'Garant Visite & Rédaction (HCEE)',
      subtitle: 'Garantir la visite préalable terrain et la rédaction de l\'AT',
      action: 'Vérifier & valider',
      path: '/autorisations?statut=EN_VISITE_REDACTION',
      icon: <VerifiedUserIcon sx={{ fontSize: 32, color: '#0284c7' }} />,
      color: '#0284c7',
      highlight: true,
    },
    {
      title: 'Garant Démarrage Travaux (HCEE)',
      subtitle: 'Garantir les conditions de démarrage et la conformité des permis',
      action: 'Vérifier démarrage',
      path: '/autorisations?statut=AT_VALIDEE',
      icon: <SecurityIcon sx={{ fontSize: 32, color: '#16a34a' }} />,
      color: '#16a34a',
    },
  ];

  const cardsProprietaire = [
    {
      title: 'Signature AT Hors Cadre (HCEP)',
      subtitle: 'Viser et signer les autorisations de travail en tant que Hors Cadre Propriétaire',
      action: 'Signer AT (HCEP)',
      path: '/autorisations',
      icon: <VerifiedUserIcon sx={{ fontSize: 32, color: '#7c3aed' }} />,
      color: '#7c3aed',
      highlight: true,
    },
    {
      title: 'Garant Archivage AT (HCEP)',
      subtitle: 'Garantir la clôture et l\'archivage final (§8.6 - HMEP exécute, HCEP garant)',
      action: 'Consulter archivage',
      path: '/autorisations?statut=RECEPTIONEES',
      icon: <HistoryIcon sx={{ fontSize: 32, color: '#7c3aed' }} />,
      color: '#7c3aed',
    },
  ];

  const renderSection = (title: string, color: string, cards: typeof cardsExecutant) => (
    <Box sx={{ mb: 4 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
        <Box sx={{ width: 4, height: 24, bgcolor: color, borderRadius: 1 }} />
        <Typography variant="h6" sx={{ fontWeight: 700, color: '#0f172a' }}>
          {title}
        </Typography>
      </Stack>
      <Grid container spacing={2.5}>
        {cards.map((c, i) => (
          <Grid key={i} size={{ xs: 12, sm: 6, md: 4 }}>
            <Card
              sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                borderRadius: 3,
                boxShadow: c.highlight ? '0 10px 25px -5px rgba(2,132,199,0.2)' : '0 1px 3px rgba(0,0,0,0.08)',
                border: c.highlight ? `2px solid ${c.color}` : '1px solid #e2e8f0',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-3px)' },
              }}
            >
              <CardContent sx={{ flexGrow: 1, p: 2.5 }}>
                <Box sx={{ mb: 1.5 }}>{c.icon}</Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#0f172a', mb: 0.5 }}>
                  {c.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {c.subtitle}
                </Typography>
              </CardContent>
              <CardActions sx={{ px: 2.5, pb: 2.5 }}>
                <Button
                  size="small"
                  variant={c.highlight ? 'contained' : 'outlined'}
                  onClick={() => navigate(c.path)}
                  sx={{
                    fontWeight: 700,
                    textTransform: 'none',
                    borderRadius: 2,
                    ...(c.highlight
                      ? { bgcolor: c.color, '&:hover': { bgcolor: '#0369a1' } }
                      : { color: c.color, borderColor: c.color }),
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
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f172a' }}>
            Espace Hors Cadre (HC)
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 0.5, alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {user?.prenom} {user?.nom} — {user?.service?.nomService || 'Service HC'}
            </Typography>
            <Chip size="small" label="Niveau 2 — Hors Cadre Responsable" color="primary" variant="outlined" sx={{ fontWeight: 700 }} />
          </Stack>
        </Box>
      </Stack>

      <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
        <strong>Standard OCP S-HSE-SEC-31 (§5 & §7) :</strong> Le rôle Hors Cadre agit comme <strong>Garant (G)</strong> sur la visite/rédaction et le démarrage (HCEE Exécutant), et comme <strong>Garant d'archivage (HCEP Propriétaire)</strong>.
      </Alert>

      {renderSection('Position Exécutante (HCEE — Garant Visite & Démarrage)', '#0284c7', cardsExecutant)}
      <Divider sx={{ my: 3 }} />
      {renderSection('Position Propriétaire (HCEP — Garant Archivage §8.6)', '#7c3aed', cardsProprietaire)}
    </Box>
  );
};
