import { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  Switch,
  CircularProgress,
  Alert,
  Paper,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  useTheme,
  Grid
} from '@mui/material';
import {
  Security as SecurityIcon,
  Archive as ArchiveIcon,
  Save as SaveIcon,
  Build as BuildIcon,
  Email as EmailIcon,
  PersonAdd as PersonAddIcon,
  Timer as TimerIcon,
  VpnKey as VpnKeyIcon,
  History as HistoryIcon,
} from '@mui/icons-material';
import { AdminService } from '../../../services/AdminService';
import type { SystemSettings } from '../../../services/AdminService';

export default function SettingsPage() {
  const theme = useTheme();
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
    let value: boolean | number;
    if (e.target.type === 'checkbox') {
      value = e.target.checked;
    } else {
      value = Number(e.target.value);
    }
    setSettings((prev) => ({ ...prev, [field]: value }));
  };

  const handleSwitch = (field: keyof SystemSettings) => (
    _: React.ChangeEvent<HTMLInputElement>,
    checked: boolean
  ) => {
    setSettings((prev) => ({ ...prev, [field]: checked }));
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
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <CircularProgress color="primary" />
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 900, margin: '0 auto', pb: 4 }}>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 600, color: 'text.primary', mb: 0.5 }}>
            Paramètres Système
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gérez la configuration globale et la sécurité de la plateforme.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          startIcon={saving ? <CircularProgress size={16} color="inherit" /> : <SaveIcon fontSize="small" />}
          onClick={handleSave}
          disabled={saving}
          sx={{ textTransform: 'none', px: 3, boxShadow: 'none' }}
        >
          {saving ? 'Enregistrement...' : 'Enregistrer'}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 3 }}>{success}</Alert>}

      <Paper variant="outlined" sx={{ borderRadius: 2, mb: 4, overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, bgcolor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.01)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <SecurityIcon color="primary" />
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>
              Sécurité & Accès
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, ml: 4.5 }}>
            Contrôle des sessions et politique de mot de passe
          </Typography>
        </Box>
        <Divider />
        <Box sx={{ p: 3 }}>
          <Grid container spacing={4}>
            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                <TimerIcon fontSize="small" color="action" /> Délai d'expiration (minutes)
              </Typography>
              <TextField
                type="number"
                value={settings.sessionTimeoutMinutes}
                onChange={handleChange('sessionTimeoutMinutes')}
                fullWidth
                size="small"
                slotProps={{ htmlInput: { min: 5, max: 480 } }}
                helperText="Inactivité avant déconnexion automatique"
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                <VpnKeyIcon fontSize="small" color="action" /> Tentatives de connexion
              </Typography>
              <TextField
                type="number"
                value={settings.maxLoginAttempts}
                onChange={handleChange('maxLoginAttempts')}
                fullWidth
                size="small"
                slotProps={{ htmlInput: { min: 1, max: 20 } }}
                helperText="Nombre d'échecs avant verrouillage"
              />
            </Grid>
          </Grid>
        </Box>
      </Paper>

      <Paper variant="outlined" sx={{ borderRadius: 2, mb: 4, overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, bgcolor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.01)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <BuildIcon color="primary" />
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>
              Fonctionnalités
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, ml: 4.5 }}>
            Activation et désactivation des modules système
          </Typography>
        </Box>
        <Divider />
        <List disablePadding>
          <ListItem sx={{ py: 2, px: 3 }}>
            <ListItemIcon>
              <BuildIcon color="action" />
            </ListItemIcon>
            <ListItemText 
              primary={<Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Mode maintenance</Typography>}
              secondary="Restreint l'accès aux seuls administrateurs de la plateforme"
            />
            <ListItemSecondaryAction>
              <Switch checked={settings.maintenanceMode} onChange={handleSwitch('maintenanceMode')} color="primary" />
            </ListItemSecondaryAction>
          </ListItem>
          <Divider component="li" />
          <ListItem sx={{ py: 2, px: 3 }}>
            <ListItemIcon>
              <PersonAddIcon color="action" />
            </ListItemIcon>
            <ListItemText 
              primary={<Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Inscription ouverte</Typography>}
              secondary="Permet aux nouveaux utilisateurs de créer un compte librement"
            />
            <ListItemSecondaryAction>
              <Switch checked={settings.inscriptionOuverte} onChange={handleSwitch('inscriptionOuverte')} color="primary" />
            </ListItemSecondaryAction>
          </ListItem>
          <Divider component="li" />
          <ListItem sx={{ py: 2, px: 3 }}>
            <ListItemIcon>
              <EmailIcon color="action" />
            </ListItemIcon>
            <ListItemText 
              primary={<Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Notifications par email</Typography>}
              secondary="Envoi automatique d'emails lors d'événements importants"
            />
            <ListItemSecondaryAction>
              <Switch checked={settings.emailNotifications} onChange={handleSwitch('emailNotifications')} color="primary" />
            </ListItemSecondaryAction>
          </ListItem>
        </List>
      </Paper>

      <Paper variant="outlined" sx={{ borderRadius: 2, mb: 4, overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, bgcolor: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.02)' : 'rgba(0,0,0,0.01)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <ArchiveIcon color="primary" />
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>
              Archivage des données
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, ml: 4.5 }}>
            Gestion du cycle de vie et rétention
          </Typography>
        </Box>
        <Divider />
        <Box sx={{ p: 3 }}>
          <Grid container spacing={4}>
            <Grid item xs={12} sm={6}>
              <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                <HistoryIcon fontSize="small" color="action" /> Durée de conservation (jours)
              </Typography>
              <TextField
                type="number"
                value={settings.retentionDays}
                onChange={handleChange('retentionDays')}
                fullWidth
                size="small"
                slotProps={{ htmlInput: { min: 30, max: 3650 } }}
                helperText="Délai avant l'archivage automatique"
              />
            </Grid>
          </Grid>
        </Box>
      </Paper>
    </Box>
  );
}