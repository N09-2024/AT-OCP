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
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface ZoneFormData {
  nomZone: string;
  codeZone: string;
  descriptionZone: string;
}

const DEFAULT_FORM: ZoneFormData = {
  nomZone: '',
  codeZone: '',
  descriptionZone: '',
};

export default function ZoneFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [form, setForm] = useState<ZoneFormData>(DEFAULT_FORM);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        if (isEdit) {
          const data = await AdminService.getZone(id!);
          setForm({
            nomZone: data.nomZone,
            codeZone: data.codeZone,
            descriptionZone: data.descriptionZone || '',
          });
        }
      } catch (err) {
        console.error('Erreur chargement zone', err);
        setError('Impossible de charger la zone');
      } finally {
        setFetchLoading(false);
      }
    };
    loadData();
  }, [id, isEdit]);

  const handleChange = (field: keyof ZoneFormData) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      if (isEdit) {
        await AdminService.updateZone(id!, form);
        setSuccess('Zone mise à jour avec succès');
      } else {
        await AdminService.createZone(form);
        setSuccess('Zone créée avec succès');
        setTimeout(() => navigate('/administration/zones'), 1500);
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Erreur lors de l'enregistrement";
      setError(msg);
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
          onClick={() => navigate('/administration/zones')}
          startIcon={<ArrowLeftIcon width={18} />}
          sx={{ textTransform: 'none', fontWeight: 500, color: 'text.secondary' }}
        >
          Retour
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          {isEdit ? "Modifier la zone" : 'Nouvelle zone'}
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
              label="Code de la zone"
              value={form.codeZone}
              onChange={handleChange('codeZone')}
              required
              fullWidth
              autoFocus
              helperText="Identifiant unique (ex: Z001)"
            />
            <TextField
              label="Nom de la zone"
              value={form.nomZone}
              onChange={handleChange('nomZone')}
              required
              fullWidth
              helperText="Libellé complet de la zone"
            />
          </Box>
          <TextField
            label="Description"
            value={form.descriptionZone}
            onChange={handleChange('descriptionZone')}
            fullWidth
            multiline
            rows={4}
          />
          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
            <Button
              type="submit"
              variant="contained"
              color="success"
              disabled={loading}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              {loading ? <CircularProgress size={20} color="inherit" /> : isEdit ? 'Enregistrer' : 'Créer'}
            </Button>
            <Button
              variant="outlined"
              onClick={() => navigate('/administration/zones')}
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