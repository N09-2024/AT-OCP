import { useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Alert, Stack } from '@mui/material';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { useAuthStore } from '../../../store/authStore';

/**
 * Dashboard HM — Haute Maîtrise
 * Position P (HMEP) : garant visite + démarrage
 * Position E (HMEE) : lecture seule (fail-closed)
 */
export default function HMDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" sx={{ fontWeight: 800, mb: 1, color: '#0f172a' }}>
        Espace Haute Maîtrise
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        {user?.prenom} {user?.nom} — HM (HMEP en position P · lecture seule en position E)
      </Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        En position <strong>Exécutant (HMEE)</strong>, le standard ne confirme aucune action d’écriture :
        accès en <strong>lecture seule</strong> uniquement.
      </Alert>

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderRadius: 3, border: '1px solid #e2e8f0', borderTop: '4px solid #0284c7' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <VerifiedUserIcon color="primary" />
                <Typography fontWeight={800}>Visites à garantir</Typography>
              </Stack>
              <Typography variant="body2" color="text.secondary">
                Position P — garantir la visite préalable chantier (§8.2)
              </Typography>
            </CardContent>
            <CardActions>
              <Button onClick={() => navigate('/visites')}>Garantir la visite</Button>
            </CardActions>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderRadius: 3, border: '1px solid #e2e8f0', borderTop: '4px solid #d97706' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <PlayCircleIcon sx={{ color: '#d97706' }} />
                <Typography fontWeight={800}>Démarrages à cautionner</Typography>
              </Stack>
              <Typography variant="body2" color="text.secondary">
                Position P — garantir le début d’intervention
              </Typography>
            </CardContent>
            <CardActions>
              <Button onClick={() => navigate('/autorisations?filtre=a-demarrer')}>Voir</Button>
            </CardActions>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderRadius: 3, border: '1px solid #e2e8f0', borderTop: '4px solid #64748b' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <VisibilityIcon />
                <Typography fontWeight={800}>Consultation périmètre</Typography>
              </Stack>
              <Typography variant="body2" color="text.secondary">
                Toutes les AT de mon territoire (lecture)
              </Typography>
            </CardContent>
            <CardActions>
              <Button onClick={() => navigate('/autorisations')}>Consulter</Button>
            </CardActions>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
