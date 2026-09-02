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
import AutorenewIcon from '@mui/icons-material/Autorenew';
import ArchiveIcon from '@mui/icons-material/Archive';
import ShieldIcon from '@mui/icons-material/Shield';
import { useAuthStore } from '../../store/authStore';

export const MaitriseDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const cardsProprietaire = [
    {
      title: '🔄 Décisions de Reconduction (HMEP)',
      subtitle: 'Approuver ou refuser les demandes de prolongation soumises par les CEEE - Responsable OCP',
      action: 'Traiter les reconductions',
      path: '/reconductions',
      icon: <AutorenewIcon sx={{ fontSize: 32, color: '#E65100' }} />,
      color: '#E65100',
      highlight: true,
    },
    {
      title: 'Signature Haute Maîtrise (HMEP)',
      subtitle: 'Viser et signer les autorisations de travail en tant que Haute Maîtrise Propriétaire (après signature HCEP/HCEE)',
      action: 'Signer AT (HMEP)',
      path: '/autorisations',
      icon: <ShieldIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
      highlight: false,
    },
    {
      title: 'Archivage AT (§8.6 - HMEP)',
      subtitle: 'Archiver les AT et permis récapitulés après réception complète',
      action: 'Exécuter l\'archivage',
      path: '/autorisations?statut=RECEPTIONEES',
      icon: <ArchiveIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
    },
    {
      title: 'Garant Visite Propriétaire (HMEP)',
      subtitle: 'Superviser et garantir la conformité HSE des AT propriétaires',
      action: 'Consulter AT',
      path: '/autorisations?statut=EN_VISITE_REDACTION',
      icon: <ShieldIcon sx={{ fontSize: 32, color: '#1F4D3E' }} />,
      color: '#1F4D3E',
    },
  ];

  const cardsExecutant = [
    {
      title: 'Signature Haute Maîtrise (HMEE)',
      subtitle: 'Viser et signer les autorisations de travail en tant que Haute Maîtrise Exécutante (après signature HCEP/HCEE)',
      action: 'Signer AT (HMEE)',
      path: '/autorisations',
      icon: <ShieldIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
      highlight: true,
    },
    {
      title: 'Garant Démarrage Exécutant (HMEE)',
      subtitle: 'Superviser le démarrage des chantiers exécutants',
      action: 'Vérifier chantiers',
      path: '/autorisations?statut=AT_VALIDEE',
      icon: <ShieldIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
    },
  ];

  const renderSection = (title: string, color: string, cards: typeof cardsProprietaire) => (
    <Box sx={{ mb: 4 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 2 }}>
        <Box sx={{ width: 4, height: 24, bgcolor: color, borderRadius: 1 }} />
        <Typography variant="h6" sx={{ fontWeight: 700, color: '#0E2A21' }}>
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
                boxShadow: c.highlight ? '0 10px 25px -5px rgba(31,77,62,0.18)' : '0 1px 3px rgba(0,0,0,0.08)',
                border: c.highlight ? `2px solid ${c.color}` : '1px solid #D6E3DC',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-3px)' },
              }}
            >
              <CardContent sx={{ flexGrow: 1, p: 2.5 }}>
                <Box sx={{ mb: 1.5 }}>{c.icon}</Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#0E2A21', mb: 0.5 }}>
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
                      ? { bgcolor: c.color, '&:hover': { bgcolor: '#2E624A' } }
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
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
            Espace Haute Maîtrise (HM)
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 0.5, alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {user?.prenom} {user?.nom} - {user?.service?.nomService || 'Service HM'}
            </Typography>
            <Chip size="small" label="Niveau 3 - Haute Maîtrise" color="secondary" variant="outlined" sx={{ fontWeight: 700 }} />
          </Stack>
        </Box>
      </Stack>

      <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
        <strong>Standard OCP S-HSE-SEC-31 (§5, §7 & §8.6) :</strong> HMEP (Propriétaire) <strong>exécute l'archivage (§8.6)</strong> et agit comme garant sur les visites. HMEE (Exécutant) agit comme garant sur le démarrage.
      </Alert>

      {renderSection('Position Propriétaire (HMEP - Exécute Archivage §8.6)', '#3C7A5C', cardsProprietaire)}
      <Divider sx={{ my: 3 }} />
      {renderSection('Position Exécutante (HMEE - Garant Démarrage)', '#3C7A5C', cardsExecutant)}
    </Box>
  );
};
