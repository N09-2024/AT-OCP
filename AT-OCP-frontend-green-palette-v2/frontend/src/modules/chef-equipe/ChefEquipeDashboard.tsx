import React, { useEffect, useState } from 'react';
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
import AddIcon from '@mui/icons-material/Add';
import DescriptionIcon from '@mui/icons-material/Description';
import SendIcon from '@mui/icons-material/Send';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useAuthStore } from '../../store/authStore';

export const ChefEquipeDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const cardsProprietaire = [
    {
      title: 'Nouvelle AT',
      subtitle: 'Créer une demande d\'intervention (DI/BT/OT)',
      action: 'Créer AT',
      path: '/autorisations/nouvelle',
      icon: <AddIcon sx={{ fontSize: 32, color: '#1F4D3E' }} />,
      color: '#1F4D3E',
      highlight: true,
    },
    {
      title: 'Mes Brouillons (P)',
      subtitle: 'AT en cours de rédaction propriétaires',
      action: 'Voir mes brouillons',
      path: '/autorisations?statut=BROUILLON',
      icon: <DescriptionIcon sx={{ fontSize: 32, color: '#A87532' }} />,
      color: '#A87532',
    },
    {
      title: 'AT Soumises (P)',
      subtitle: 'Visite & rédaction conjointe avec l\'exécutant',
      action: 'Suivre les soumissions',
      path: '/autorisations?statut=DEMANDE_CREEE',
      icon: <SendIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
    },
    {
      title: 'Réception Travaux (P)',
      subtitle: 'Réception conjointe CEEP + CEEE après fin des travaux',
      action: 'Réceptionner',
      path: '/autorisations?statut=DECLAREE_TERMINEE',
      icon: <CheckCircleIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
    },
  ];

  const cardsExecutant = [
    {
      title: 'AT à Viser & Signer (E)',
      subtitle: 'Visite préalable terrain & signature exécutante',
      action: 'Accéder aux réceptions',
      path: '/receptions/ceee',
      icon: <AssignmentTurnedInIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
      highlight: true,
    },
    {
      title: 'Démarrage Travaux (E)',
      subtitle: 'Démarrer l\'intervention (CEEE Exécute)',
      action: 'Démarrer travaux',
      path: '/autorisations?statut=AT_VALIDEE',
      icon: <PlayArrowIcon sx={{ fontSize: 32, color: '#3C7A5C' }} />,
      color: '#3C7A5C',
    },
    {
      title: 'Reconduction / Poste (E)',
      subtitle: 'Reconduire l\'AT et les permis si dépassement',
      action: 'Reconduire',
      path: '/autorisations?statut=EN_COURS',
      icon: <AutorenewIcon sx={{ fontSize: 32, color: '#A87532' }} />,
      color: '#A87532',
    },
    {
      title: 'Déclaration Fin Travaux (E)',
      subtitle: 'Déclarer la fin des travaux & clôture 2h permis feu',
      action: 'Déclarer fin',
      path: '/autorisations?statut=EN_COURS',
      icon: <CheckCircleIcon sx={{ fontSize: 32, color: '#5C6E67' }} />,
      color: '#5C6E67',
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
          <Grid key={i} size={{ xs: 12, sm: 6, md: 3 }}>
            <Card
              sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                borderRadius: 3,
                boxShadow: c.highlight ? '0 10px 25px -5px rgba(31,77,62,0.2)' : '0 1px 3px rgba(0,0,0,0.08)',
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
            Espace Chef d'Équipe (CE)
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 0.5, alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {user?.prenom} {user?.nom} — {user?.service?.nomService || 'Service CE'}
            </Typography>
            <Chip size="small" label="Niveau 1 — Chef d'Équipe" color="success" variant="outlined" sx={{ fontWeight: 700 }} />
          </Stack>
        </Box>

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/autorisations/nouvelle')}
          sx={{ bgcolor: '#1F4D3E', fontWeight: 700, borderRadius: 2, px: 3 }}
        >
          Nouvelle AT
        </Button>
      </Stack>

      <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
        <strong>Standard OCP S-HSE-SEC-31 (§5 & §7) :</strong> Vos droits (CEEP ou CEEE) sont résolus dynamiquement par AT en fonction du service propriétaire et du service exécutant d'intervention.
      </Alert>

      {renderSection('Mes AT — Position Propriétaire (CEEP)', '#1F4D3E', cardsProprietaire)}
      <Divider sx={{ my: 3 }} />
      {renderSection('Mes AT — Position Exécutant (CEEE)', '#3C7A5C', cardsExecutant)}
    </Box>
  );
};
