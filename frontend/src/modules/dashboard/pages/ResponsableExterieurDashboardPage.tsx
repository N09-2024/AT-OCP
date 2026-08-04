import { useNavigate } from 'react-router-dom';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Alert, Stack } from '@mui/material';
import AssignmentIcon from '@mui/icons-material/Assignment';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import DescriptionIcon from '@mui/icons-material/Description';
import { useAuthStore } from '../../../store/authStore';

/**
 * Dashboard RESPONSABLE_EXTERIEUR
 * BT + permis uniquement — pas d'accès au workflow AT principal.
 */
export default function ResponsableExterieurDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" sx={{ fontWeight: 800, mb: 1 }}>
        Espace Entreprise Extérieure
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        {user?.prenom} {user?.nom} — Responsable Entreprise Extérieure
      </Typography>

      <Alert severity="warning" sx={{ mb: 3 }}>
        Accès limité aux <strong>Bons de Travaux (BT)</strong> et aux <strong>permis</strong> associés.
        Le workflow AT (demande, visite, rédaction, validation) est réservé aux rôles OCP (CE / HM / HC).
      </Alert>

      <Grid container spacing={2.5}>
        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderRadius: 3, border: '1px solid #e2e8f0', borderTop: '4px solid #d97706' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <AssignmentIcon sx={{ color: '#d97706' }} />
                <Typography fontWeight={800}>Mes Bons de Travaux</Typography>
              </Stack>
              <Typography variant="body2" color="text.secondary">
                Créer et suivre les BT
              </Typography>
            </CardContent>
            <CardActions>
              <Button onClick={() => navigate('/documents')}>Ouvrir</Button>
            </CardActions>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderRadius: 3, border: '1px solid #e2e8f0', borderTop: '4px solid #0284c7' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <UploadFileIcon color="primary" />
                <Typography fontWeight={800}>Permis à uploader</Typography>
              </Stack>
              <Typography variant="body2" color="text.secondary">
                Joindre les permis liés aux BT
              </Typography>
            </CardContent>
            <CardActions>
              <Button onClick={() => navigate('/permis')}>Gérer</Button>
            </CardActions>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card sx={{ borderRadius: 3, border: '1px solid #e2e8f0', borderTop: '4px solid #64748b' }}>
            <CardContent>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <DescriptionIcon />
                <Typography fontWeight={800}>AT liées à mes BT</Typography>
              </Stack>
              <Typography variant="body2" color="text.secondary">
                Consultation seule
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
