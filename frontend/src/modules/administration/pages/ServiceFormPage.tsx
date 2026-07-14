import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  CircularProgress,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { apiClient } from '../../../services/apiClient';

interface Zone {
  id: string;
  nomZone: string;
  codeZone: string;
}

export default function ServiceFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [nomService, setNomService] = useState('');
  const [codeService, setCodeService] = useState('');
  const [descriptionService, setDescriptionService] = useState('');
  const [zoneId, setZoneId] = useState('');
  const [zones, setZones] = useState<Zone[]>([]);

  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const zonesRes = await apiClient.get('/zones?size=500');
        setZones(zonesRes.data.content ?? zonesRes.data ?? []);

        if (isEdit) {
          const res = await apiClient.get(`/services/${id}`);
          const data = res.data;
          setNomService(data.nomService ?? '');
          setCodeService(data.codeService ?? '');
          setDescriptionService(data.descriptionService ?? '');
          setZoneId(data.zone?.id ?? '');
        }
      } catch (err) {
        console.error('Erreur chargement service', err);
        setError('Impossible de charger les données');
      } finally {
        setFetchLoading(false);
      }
    };
    loadData();
  }, [id, isEdit]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nomService.trim() || !codeService.trim() || !zoneId) {
      setError('Veuillez remplir tous les champs obligatoires');
      return;
    }
    setError('');
    setSuccess('');
    setLoading(true);

    const payload = {
      nomService: nomService.trim(),
      codeService: codeService.trim(),
      descriptionService: descriptionService.trim(),
      zoneId,
    };

    try {
      if (isEdit) {
        await apiClient.put(`/services/${id}`, payload);
        setSuccess('Service modifié avec succès');
      } else {
        await apiClient.post('/services', payload);
        setSuccess('Service créé avec succès');
        setTimeout(() => navigate('/administration/services'), 1200);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || "Erreur lors de l'enregistrement");
    } finally {
      setLoading(false);
    }
  };

  if (fetchLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', width: '100%' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <Button
          onClick={() => navigate('/administration/services')}
          startIcon={<ArrowLeftIcon width={18} />}
          sx={{ textTransform: 'none', fontWeight: 500, color: 'text.secondary' }}
        >
          Retour
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          {isEdit ? 'Modifier le service OCP' : 'Nouveau service OCP'}
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 3, borderRadius: 2 }}>
          {success}
        </Alert>
      )}

      <Paper elevation={0} sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', p: { xs: 3, md: 5 } }}>
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              label="Code du service"
              value={codeService}
              onChange={(e) => setCodeService(e.target.value)}
              required
              fullWidth
              autoFocus
            />
            <TextField
              label="Nom du service"
              value={nomService}
              onChange={(e) => setNomService(e.target.value)}
              required
              fullWidth
            />
          </Box>

          <FormControl fullWidth required>
            <InputLabel>Zone d'affectation</InputLabel>
            <Select
              value={zoneId}
              label="Zone d'affectation"
              onChange={(e) => setZoneId(e.target.value)}
            >
              <MenuItem value="" disabled>Sélectionner une zone</MenuItem>
              {zones.map(z => (
                <MenuItem key={z.id} value={z.id}>
                  {z.nomZone} ({z.codeZone})
                </MenuItem>
              ))}
            </Select>
            <FormHelperText>Un service appartient à une seule zone</FormHelperText>
          </FormControl>

          <TextField
            label="Description"
            value={descriptionService}
            onChange={(e) => setDescriptionService(e.target.value)}
            fullWidth
            multiline
            rows={3}
          />

          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
            <Button
              type="submit"
              variant="contained"
              color="success"
              disabled={loading}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              {loading ? (
                <CircularProgress size={20} color="inherit" />
              ) : isEdit ? (
                'Enregistrer'
              ) : (
                'Créer le service'
              )}
            </Button>
            <Button
              variant="outlined"
              onClick={() => navigate('/administration/services')}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 500 }}
            >
              Annuler
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}
