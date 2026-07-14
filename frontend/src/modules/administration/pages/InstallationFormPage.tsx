import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  CircularProgress,
  FormControlLabel,
  Switch,
  Alert,
  MenuItem,
  FormControl,
  Select,
  InputLabel,
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface InstallationFormData {
  libelle: string;
  description: string;
  zoneId: string;
  active: boolean;
}

const DEFAULT_FORM: InstallationFormData = {
  libelle: '',
  description: '',
  zoneId: '',
  active: true,
};

export default function InstallationFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [form, setForm] = useState<InstallationFormData>(DEFAULT_FORM);
  const [zones, setZones] = useState<Array<{ id: string; libelle: string }>>([]);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        // Load zones for dropdown
        const zonesRes = await AdminService.listZones();
        const zonesList = Array.isArray(zonesRes) ? zonesRes : zonesRes.content;
        setZones(zonesList.map((z: any) => ({ id: z.id, libelle: z.libelle })));

        if (isEdit) {
          const data = await AdminService.getInstallation(id!);
          setForm({
            libelle: data.libelle,
            description: data.description || '',
            zoneId: data.zoneId || '',
            active: data.active,
          });
        }
      } catch (err) {
        console.error('Erreur chargement données installation', err);
        setError('Impossible de charger les données');
      } finally {
        setFetchLoading(false);
      }
    };
    loadData();
  }, [id, isEdit]);

  const handleChange = (field: keyof InstallationFormData) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const value = e.target.type === 'checkbox' ? (e.target as HTMLInputElement).checked : e.target.value;
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      if (isEdit) {
        await AdminService.updateInstallation(id!, form);
        setSuccess('Installation mise à jour avec succès');
      } else {
        await AdminService.createInstallation(form);
        setSuccess('Installation créée avec succès');
        setTimeout(() => navigate('/administration/installations'), 1500);
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
          onClick={() => navigate('/administration/installations')}
          startIcon={<ArrowLeftIcon width={18} />}
          sx={{ textTransform: 'none', fontWeight: 500, color: 'text.secondary' }}
        >
          Retour
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          {isEdit ? "Modifier l'installation" : 'Nouvelle installation'}
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
          <TextField
            label="Libellé"
            value={form.libelle}
            onChange={handleChange('libelle')}
            required
            fullWidth
          />
          <TextField
            label="Description"
            value={form.description}
            onChange={handleChange('description')}
            fullWidth
            multiline
            rows={4}
          />
          <FormControl fullWidth>
            <InputLabel id="zone-label">Zone</InputLabel>
            <Select
              labelId="zone-label"
              value={form.zoneId}
              onChange={(e) => {
                setForm((prev) => ({ ...prev, zoneId: e.target.value }));
              }}
              label="Zone"
            >
              {zones.map(zone => (
                <MenuItem key={zone.id} value={zone.id}>
                  {zone.libelle}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControlLabel
            control={<Switch checked={form.active} onChange={handleChange('active')} />}
            label="Installation active"
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
              onClick={() => navigate('/administration/installations')}
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