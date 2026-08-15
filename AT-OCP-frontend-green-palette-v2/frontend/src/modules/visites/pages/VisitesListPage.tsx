import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Chip,
  CircularProgress,
} from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import { apiClient } from '../../../services/apiClient';

export default function VisitesListPage() {
  const [loading, setLoading] = useState(true);
  const [visites, setVisites] = useState<any[]>([]);

  useEffect(() => {
    apiClient
      .get('/visites-prealables')
      .then((res) => setVisites(Array.isArray(res.data) ? res.data : res.data?.content || []))
      .catch(() => setVisites([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21', mb: 1 }}>
        Visites Préalables de Chantier
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
        Historique des contrôles préalables terrain avec localisation GPS et clichés photographiques
      </Typography>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
          <CircularProgress color="success" />
        </Box>
      ) : visites.length === 0 ? (
        <Paper sx={{ p: 6, textAlign: 'center' }}>
          <Typography color="text.secondary">Aucune visite préalable enregistrée.</Typography>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {visites.map((v) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={v.id}>
              <Paper sx={{ p: 3, borderRadius: 3, border: '1px solid #D6E3DC' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Chip
                    icon={<LocationOnIcon />}
                    label={v.latitude ? `${v.latitude.toFixed(4)}, ${v.longitude.toFixed(4)}` : 'GPS Capturé'}
                    color="success"
                    size="small"
                  />
                  <Typography variant="caption" color="text.secondary">
                    {v.dateVisite ? new Date(v.dateVisite).toLocaleDateString('fr-FR') : ''}
                  </Typography>
                </Box>

                <Typography variant="body2" sx={{ fontWeight: 600, mb: 1 }}>
                  Visiteur : {v.visiteurNomComplet || 'Agent OCP'}
                </Typography>

                {v.commentaire && (
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    component="p"
                    sx={{ fontStyle: 'italic', mb: 2 }}
                  >
                    « {v.commentaire} »
                  </Typography>
                )}

                {v.photos && v.photos.length > 0 && (
                  <Box sx={{ display: 'flex', gap: 1, overflowX: 'auto', pt: 1 }}>
                    {v.photos.map((p: any) => (
                      <Box
                        key={p.id}
                        component="img"
                        src={p.urlPhoto || '/placeholder.png'}
                        alt="Photo chantier"
                        sx={{ width: 60, height: 60, borderRadius: 1, objectFit: 'cover', border: '1px solid #D6E3DC' }}
                      />
                    ))}
                  </Box>
                )}
              </Paper>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
}
