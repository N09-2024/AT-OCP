import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Grid,
  TextField,
  Button,
  Avatar,
  Chip,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  Divider,
} from '@mui/material';
import {
  UserIcon,
  ShieldCheckIcon,
  EyeIcon,
  EyeSlashIcon,
} from '@heroicons/react/24/outline';
import { useAuthStore } from '../../../store/authStore';
import { apiClient } from '../../../services/apiClient';

export default function ProfilePage() {
  const user = useAuthStore((s) => s.user);
  const location = useLocation();
  const [activeTab, setActiveTab] = useState<'info' | 'security'>(
    new URLSearchParams(location.search).get('tab') === 'security' ? 'security' : 'info'
  );

  useEffect(() => {
    const tab = new URLSearchParams(location.search).get('tab');
    setActiveTab(tab === 'security' ? 'security' : 'info');
  }, [location.search]);

  // Password change state
  const [ancienMdp, setAncienMdp] = useState('');
  const [nouveauMdp, setNouveauMdp] = useState('');
  const [confirmMdp, setConfirmMdp] = useState('');
  const [showAncien, setShowAncien] = useState(false);
  const [showNouveau, setShowNouveau] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!user) return null;

  const initials = `${user.prenom?.[0] ?? ''}${user.nom?.[0] ?? ''}`.toUpperCase();
  const primaryRole = user.roles?.[0]?.nom ?? 'Utilisateur';

  const roleColor: Record<string, string> = {
    ADMIN: '#7c3aed',
    RESPONSABLE_OCP: '#16a34a',
    RESPONSABLE_ENTREPRISE: '#ea580c',
    DEMANDEUR: '#0891b2',
  };
  const color = roleColor[primaryRole] ?? '#6b7280';

  const handleChangePassword = async () => {
    setError(null);
    setSuccess(null);

    if (!ancienMdp || !nouveauMdp || !confirmMdp) {
      setError('Tous les champs sont obligatoires.');
      return;
    }
    if (nouveauMdp !== confirmMdp) {
      setError('Le nouveau mot de passe et la confirmation ne correspondent pas.');
      return;
    }
    if (nouveauMdp.length < 8) {
      setError('Le nouveau mot de passe doit contenir au moins 8 caractères.');
      return;
    }

    setSaving(true);
    try {
      await apiClient.put('/users/me/password', {
        ancienMotDePasse: ancienMdp,
        nouveauMotDePasse: nouveauMdp,
      });
      setSuccess('Mot de passe modifié avec succès !');
      setAncienMdp('');
      setNouveauMdp('');
      setConfirmMdp('');
    } catch (err: any) {
      const msg =
        err?.response?.data?.message ??
        err?.response?.data?.error ??
        "Erreur lors de la modification du mot de passe.";
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  const tabs = [
    { key: 'info', label: 'Informations', icon: <UserIcon width={18} /> },
    { key: 'security', label: 'Sécurité', icon: <ShieldCheckIcon width={18} /> },
  ];

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          Mon Profil
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Gérez vos informations personnelles et la sécurité de votre compte
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Left column: Avatar card */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Paper
            sx={{
              borderRadius: 3,
              p: 3,
              boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 2,
            }}
          >
            <Avatar
              sx={{
                width: 80,
                height: 80,
                bgcolor: color,
                fontSize: 28,
                fontWeight: 700,
                color: 'white',
              }}
            >
              {initials}
            </Avatar>
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                {user.prenom} {user.nom}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                {user.email}
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, justifyContent: 'center' }}>
                {user.roles?.map((r) => (
                  <Chip
                    key={r.id}
                    label={r.nom.replace(/_/g, ' ')}
                    size="small"
                    sx={{
                      bgcolor: color + '18',
                      color,
                      fontWeight: 700,
                      fontSize: 10,
                      height: 22,
                    }}
                  />
                ))}
              </Box>
            </Box>
            <Divider flexItem />
            <Box sx={{ width: '100%' }}>
              {[
                { label: 'Matricule', value: user.matricule ?? '-' },
                { label: 'Service', value: (user as any).serviceNom ?? '-' },
                { label: 'Compte actif', value: (user as any).actif ? 'Oui ✓' : 'Non ✗' },
              ].map((item) => (
                <Box key={item.label} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.75 }}>
                  <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                    {item.label}
                  </Typography>
                  <Typography variant="caption" sx={{ fontWeight: 500 }}>
                    {item.value}
                  </Typography>
                </Box>
              ))}
            </Box>
          </Paper>
        </Grid>

        {/* Right column: Tabs */}
        <Grid size={{ xs: 12, md: 8 }}>
          {/* Tab bar */}
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            {tabs.map((tab) => (
              <Button
                key={tab.key}
                variant={activeTab === tab.key ? 'contained' : 'outlined'}
                startIcon={tab.icon}
                onClick={() => {
                  setActiveTab(tab.key as any);
                  setError(null);
                  setSuccess(null);
                }}
                sx={{
                  borderRadius: 2,
                  textTransform: 'none',
                  fontWeight: 600,
                  ...(activeTab === tab.key
                    ? {}
                    : { borderColor: 'divider', color: 'text.secondary' }),
                }}
              >
                {tab.label}
              </Button>
            ))}
          </Box>

          <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', p: 3 }}>
            {/* ── INFO TAB ── */}
            {activeTab === 'info' && (
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>
                  Informations personnelles
                </Typography>
                <Grid container spacing={2}>
                  {[
                    { label: 'Prénom', value: user.prenom },
                    { label: 'Nom', value: user.nom },
                    { label: 'Email', value: user.email },
                    { label: 'Matricule', value: user.matricule ?? '-' },
                    { label: 'Service', value: (user as any).serviceNom ?? '-' },
                    {
                      label: 'Rôle(s)',
                      value: user.roles?.map((r) => r.nom.replace(/_/g, ' ')).join(', ') ?? '-',
                    },
                  ].map((field) => (
                    <Grid key={field.label} size={{ xs: 12, sm: 6 }}>
                      <Box>
                        <Typography
                          variant="caption"
                          color="text.secondary"
                          sx={{ fontWeight: 700, textTransform: 'uppercase', fontSize: 10, letterSpacing: 0.5 }}
                        >
                          {field.label}
                        </Typography>
                        <Typography variant="body1" sx={{ mt: 0.5, fontWeight: 500 }}>
                          {field.value}
                        </Typography>
                      </Box>
                    </Grid>
                  ))}
                </Grid>

                <Alert severity="info" sx={{ mt: 3, borderRadius: 2 }}>
                  Pour modifier vos informations (nom, email…), contactez un administrateur.
                </Alert>
              </Box>
            )}

            {/* ── SECURITY TAB ── */}
            {activeTab === 'security' && (
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 0.5 }}>
                  Changer le mot de passe
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.
                </Typography>

                {success && (
                  <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>
                    {success}
                  </Alert>
                )}
                {error && (
                  <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>
                    {error}
                  </Alert>
                )}

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <TextField
                    label="Ancien mot de passe"
                    type={showAncien ? 'text' : 'password'}
                    value={ancienMdp}
                    onChange={(e) => setAncienMdp(e.target.value)}
                    fullWidth
                    size="small"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <IconButton size="small" onClick={() => setShowAncien((v) => !v)}>
                              {showAncien ? <EyeSlashIcon width={18} /> : <EyeIcon width={18} />}
                            </IconButton>
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                  <TextField
                    label="Nouveau mot de passe"
                    type={showNouveau ? 'text' : 'password'}
                    value={nouveauMdp}
                    onChange={(e) => setNouveauMdp(e.target.value)}
                    fullWidth
                    size="small"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <IconButton size="small" onClick={() => setShowNouveau((v) => !v)}>
                              {showNouveau ? <EyeSlashIcon width={18} /> : <EyeIcon width={18} />}
                            </IconButton>
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                  <TextField
                    label="Confirmer le nouveau mot de passe"
                    type={showConfirm ? 'text' : 'password'}
                    value={confirmMdp}
                    onChange={(e) => setConfirmMdp(e.target.value)}
                    fullWidth
                    size="small"
                    error={confirmMdp.length > 0 && confirmMdp !== nouveauMdp}
                    helperText={
                      confirmMdp.length > 0 && confirmMdp !== nouveauMdp
                        ? 'Les mots de passe ne correspondent pas'
                        : ''
                    }
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <IconButton size="small" onClick={() => setShowConfirm((v) => !v)}>
                              {showConfirm ? <EyeSlashIcon width={18} /> : <EyeIcon width={18} />}
                            </IconButton>
                          </InputAdornment>
                        ),
                      },
                    }}
                  />

                  <Box sx={{ display: 'flex', justifyContent: 'flex-end', pt: 1 }}>
                    <Button
                      variant="contained"
                      onClick={handleChangePassword}
                      disabled={saving}
                      sx={{
                        borderRadius: 2,
                        textTransform: 'none',
                        fontWeight: 600,
                        px: 3,
                      }}
                    >
                      {saving ? <CircularProgress size={18} color="inherit" /> : 'Modifier le mot de passe'}
                    </Button>
                  </Box>
                </Box>
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
