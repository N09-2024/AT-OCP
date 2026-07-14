import { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  Divider,
  CircularProgress,
  Alert,
} from '@mui/material';
import { AdminService } from '../../../services/AdminService';
import type { SystemSettings } from '../../../services/AdminService';

export default function SettingsPage() {
  const [settings, setSettings] = useState<SystemSettings>({
    maintenanceMode: false,
    sessionTimeoutMinutes: 60,
    maxLoginAttempts: 5,
    inscriptionOuverte: false,
    emailNotifications: true,
    retentionDays: 365,
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    AdminService.getSettings()
      .then(setSettings)
      .catch((err) => console.error('Erreur chargement paramètres', err))
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (field: keyof SystemSettings) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : Number(e.target.value);
    setSettings((prev) => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    setError('');
    setSuccess('');
    setSaving(true);
    try {
      await AdminService.updateSettings(settings);
      setSuccess('Paramètres enregistrés avec succès');
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Erreur lors de l'enregistrement";
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          Paramètres
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Configuration générale du système
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

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', p: 4, maxWidth: 640 }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Sécurité
          </Typography>
          <TextField
            label="Délai d'expiration de session (minutes)"
            type="number"
            value={settings.sessionTimeoutMinutes}
            onChange={handleChange('sessionTimeoutMinutes')}
            fullWidth
            size="small"
            slotProps={{ htmlInput: { min: 5, max: 480 } }}
          />
          <TextField
            label="Tentatives de connexion max"
            type="number"
            value={settings.maxLoginAttempts}
            onChange={handleChange('maxLoginAttempts')}
            fullWidth
            size="small"
            slotProps={{ htmlInput: { min: 1, max: 20 } }}
          />

          <Divider />

          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Fonctionnalités
          </Typography>
          <FormControlLabel
            control={
              <Switch checked={settings.maintenanceMode} onChange={handleChange('maintenanceMode')} />
            }
            label="Mode maintenance"
          />
          <FormControlLabel
            control={
              <Switch checked={settings.inscriptionOuverte} onChange={handleChange('inscriptionOuverte')} />
            }
            label="Inscription ouverte"
          />
          <FormControlLabel
            control={
              <Switch checked={settings.emailNotifications} onChange={handleChange('emailNotifications')} />
            }
            label="Notifications par email"
          />

          <Divider />

          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Archivage
          </Typography>
          <TextField
            label="Durée de conservation (jours)"
            type="number"
            value={settings.retentionDays}
            onChange={handleChange('retentionDays')}
            fullWidth
            size="small"
            slotProps={{ htmlInput: { min: 30, max: 3650 } }}
          />

          <Box sx={{ mt: 2 }}>
            <Button
              variant="contained"
              color="success"
              onClick={handleSave}
              disabled={saving}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              {saving ? <CircularProgress size={20} color="inherit" /> : 'Enregistrer les paramètres'}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}