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
  MenuItem,
  FormControl,
  Select,
  InputLabel,
  FormHelperText,
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface InstallationFormData {
  nomInstallation: string;
  codeInstallation: string;
  atelier: string;
  localisation: string;
  serviceId: string;
}

const DEFAULT_FORM: InstallationFormData = {
  nomInstallation: '',
  codeInstallation: '',
  atelier: '',
  localisation: '',
  serviceId: '',
};

export default function InstallationFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [form, setForm] = useState<InstallationFormData>(DEFAULT_FORM);
  const [services, setServices] = useState<Array<{ id: string; nomService: string; codeService: string }>>([]);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        // Load services for dropdown
        const servicesList = await AdminService.listServices();
        setServices(servicesList.map((s: any) => ({ id: s.id, nomService: s.nomService, codeService: s.codeService })));

        if (isEdit) {
          const data = await AdminService.getInstallation(id!);
          setForm({
            nomInstallation: data.nomInstallation,
            codeInstallation: data.codeInstallation,
            atelier: data.atelier || '',
            localisation: data.localisation || '',
            serviceId: data.service?.id || '',
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
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
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
          {/* Code + Nom */}
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              label="Code de l'installation"
              value={form.codeInstallation}
              onChange={handleChange('codeInstallation')}
              required
              fullWidth
              autoFocus
              helperText="Identifiant unique (ex: INST-001)"
            />
            <TextField
              label="Nom de l'installation"
              value={form.nomInstallation}
              onChange={handleChange('nomInstallation')}
              required
              fullWidth
              helperText="Libellé complet de l'installation"
            />
          </Box>

          {/* Atelier + Localisation */}
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              label="Atelier"
              value={form.atelier}
              onChange={handleChange('atelier')}
              fullWidth
              helperText="Nom de l'atelier (optionnel)"
            />
            <TextField
              label="Localisation"
              value={form.localisation}
              onChange={handleChange('localisation')}
              fullWidth
              helperText="Emplacement géographique (optionnel)"
            />
          </Box>

          {/* Service parent */}
          <FormControl fullWidth>
            <InputLabel id="service-label">Service rattaché</InputLabel>
            <Select
              labelId="service-label"
              value={form.serviceId}
              onChange={(e) => {
                setForm((prev) => ({ ...prev, serviceId: e.target.value }));
              }}
              label="Service rattaché"
            >
              <MenuItem value="">
                <em>Aucun service</em>
              </MenuItem>
              {services.map(service => (
                <MenuItem key={service.id} value={service.id}>
                  {service.nomService} ({service.codeService})
                </MenuItem>
              ))}
            </Select>
            <FormHelperText>Une installation appartient à un service OCP</FormHelperText>
          </FormControl>

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