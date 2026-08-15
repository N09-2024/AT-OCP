import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  Avatar,
  Chip,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  Divider,
  Grid,
} from '@mui/material';
import {
  UserIcon,
  ShieldCheckIcon,
  EyeIcon,
  EyeSlashIcon,
  BuildingOfficeIcon,
  IdentificationIcon,
  EnvelopeIcon,
  CheckCircleIcon,
} from '@heroicons/react/24/outline';
import { useAuthStore } from '../../../store/authStore';
import { apiClient } from '../../../services/apiClient';

// OCP Green
const PRIMARY = '#3C7A5C';

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

  const roleConfig: Record<string, { color: string; bg: string; label: string }> = {
    ADMIN:                  { color: '#3C7A5C', bg: '#EDF2EE', label: 'Administrateur' },
    CEEP:                   { color: '#3C7A5C', bg: '#EDF2EE', label: 'CEEP — Chef Équipe Propriétaire' },
    CEEE:                   { color: '#3C7A5C', bg: '#E2F0E8', label: 'CEEE — Chef Équipe Exécutant' },
    HCEP:                   { color: '#3C7A5C', bg: '#EDF2EE', label: 'HCEP — Hors Cadre Propriétaire' },
    HCEE:                   { color: '#2E624A', bg: '#E2F0E8', label: 'HCEE — Hors Cadre Exécutant' },
    HMEP:                   { color: '#3C7A5C', bg: '#EDF2EE', label: 'HMEP — Haute Maîtrise Propriétaire' },
    HMEE:                   { color: '#2E624A', bg: '#EDF2EE', label: 'HMEE — Haute Maîtrise Exécutante' },
    RESPONSABLE_ENTREPRISE: { color: '#2E624A', bg: '#EDF2EE', label: 'Responsable Entreprise Externe' },
  };
  const _roleInfo = roleConfig[primaryRole] ?? { color: '#5C6E67', bg: '#F7FAF8', label: primaryRole };

  const handleChangePassword = async () => {
    setError(null);
    setSuccess(null);
    if (!ancienMdp || !nouveauMdp || !confirmMdp) { setError('Tous les champs sont obligatoires.'); return; }
    if (nouveauMdp !== confirmMdp) { setError('Le nouveau mot de passe et la confirmation ne correspondent pas.'); return; }
    if (nouveauMdp.length < 8) { setError('Le nouveau mot de passe doit contenir au moins 8 caractères.'); return; }
    setSaving(true);
    try {
      await apiClient.put('/users/me/password', { ancienMotDePasse: ancienMdp, nouveauMotDePasse: nouveauMdp });
      setSuccess('Mot de passe modifié avec succès !');
      setAncienMdp(''); setNouveauMdp(''); setConfirmMdp('');
    } catch (err: any) {
      setError(err?.response?.data?.message ?? err?.response?.data?.error ?? 'Erreur lors de la modification.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto' }}>

      {/* ── HERO BANNER ── */}
      <Box
        sx={{
          borderRadius: 4,
          background: `linear-gradient(135deg, ${PRIMARY} 0%, #2E624A 60%, #2E624A 100%)`,
          p: { xs: 3, md: 4 },
          mb: 3,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* decorative circles */}
        <Box sx={{ position: 'absolute', top: -40, right: -40, width: 200, height: 200, borderRadius: '50%', bgcolor: 'rgba(255,255,255,0.06)' }} />
        <Box sx={{ position: 'absolute', bottom: -60, right: 80, width: 140, height: 140, borderRadius: '50%', bgcolor: 'rgba(255,255,255,0.05)' }} />

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, position: 'relative' }}>
          <Avatar
            sx={{
              width: 80,
              height: 80,
              bgcolor: 'rgba(255,255,255,0.2)',
              fontSize: 28,
              fontWeight: 800,
              color: 'white',
              border: '3px solid rgba(255,255,255,0.4)',
              backdropFilter: 'blur(8px)',
            }}
          >
            {initials}
          </Avatar>
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 800, color: 'white', lineHeight: 1.2 }}>
              {user.prenom} {user.nom}
            </Typography>
            <Typography sx={{ color: 'rgba(255,255,255,0.75)', fontSize: 14, mt: 0.3 }}>
              {user.email}
            </Typography>
            <Box sx={{ mt: 1.5, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              {user.roles?.map((r) => (
                <Chip
                  key={r.id}
                  label={r.nom.replace(/_/g, ' ')}
                  size="small"
                  sx={{
                    bgcolor: 'rgba(255,255,255,0.2)',
                    color: 'white',
                    fontWeight: 700,
                    fontSize: 11,
                    backdropFilter: 'blur(4px)',
                    border: '1px solid rgba(255,255,255,0.3)',
                  }}
                />
              ))}
            </Box>
          </Box>
        </Box>
      </Box>

      {/* ── STATS ROW ── */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        {[
          { icon: <IdentificationIcon width={20} />, label: 'Matricule', value: user.matricule ?? '—' },
          { icon: <BuildingOfficeIcon width={20} />, label: 'Service', value: (user as any).serviceNom ?? '—' },
          { icon: <EnvelopeIcon width={20} />, label: 'Email', value: user.email },
          { icon: <CheckCircleIcon width={20} />, label: 'Statut', value: (user as any).actif !== false ? 'Actif' : 'Inactif' },
        ].map((stat) => (
          <Grid key={stat.label} size={{ xs: 6, sm: 3 }}>
            <Paper
              sx={{
                borderRadius: 3,
                p: 2,
                boxShadow: '0 1px 3px rgba(0,0,0,0.07)',
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                gap: 0.5,
              }}
            >
              <Box sx={{ color: PRIMARY, display: 'flex', alignItems: 'center', gap: 0.5 }}>
                {stat.icon}
                <Typography variant="caption" sx={{ fontWeight: 700, textTransform: 'uppercase', fontSize: 10, letterSpacing: 0.5, color: 'text.secondary' }}>
                  {stat.label}
                </Typography>
              </Box>
              <Typography variant="body2" sx={{ fontWeight: 600, wordBreak: 'break-all', mt: 0.5 }}>
                {stat.value}
              </Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      {/* ── TABS ── */}
      <Paper sx={{ borderRadius: 4, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        {/* Tab header */}
        <Box sx={{ display: 'flex', borderBottom: '1px solid', borderColor: 'divider', bgcolor: '#F7FAF8' }}>
          {[
            { key: 'info', label: 'Informations personnelles', icon: <UserIcon width={17} /> },
            { key: 'security', label: 'Sécurité', icon: <ShieldCheckIcon width={17} /> },
          ].map((tab) => (
            <Box
              key={tab.key}
              onClick={() => { setActiveTab(tab.key as any); setError(null); setSuccess(null); }}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                px: 3,
                py: 2,
                cursor: 'pointer',
                fontSize: 14,
                fontWeight: 600,
                color: activeTab === tab.key ? PRIMARY : 'text.secondary',
                borderBottom: activeTab === tab.key ? `2px solid ${PRIMARY}` : '2px solid transparent',
                mb: '-1px',
                transition: 'all 0.15s',
                '&:hover': { color: PRIMARY },
              }}
            >
              {tab.icon}
              {tab.label}
            </Box>
          ))}
        </Box>

        {/* Tab content */}
        <Box sx={{ p: { xs: 3, md: 4 } }}>

          {/* ── INFO TAB ── */}
          {activeTab === 'info' && (
            <Box>
              <Grid container spacing={3}>
                {[
                  { label: 'Prénom', value: user.prenom },
                  { label: 'Nom', value: user.nom },
                  { label: 'Email', value: user.email },
                  { label: 'Matricule', value: user.matricule ?? '—' },
                  { label: 'Service', value: (user as any).serviceNom ?? '—' },
                  { label: 'Rôle(s)', value: user.roles?.map((r) => r.nom.replace(/_/g, ' ')).join(', ') ?? '—' },
                ].map((field) => (
                  <Grid key={field.label} size={{ xs: 12, sm: 6 }}>
                    <Box
                      sx={{
                        p: 2,
                        borderRadius: 2,
                        border: '1px solid',
                        borderColor: 'divider',
                        bgcolor: '#F7FAF8',
                      }}
                    >
                      <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary', textTransform: 'uppercase', fontSize: 10, letterSpacing: 0.5 }}>
                        {field.label}
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600, mt: 0.5, fontSize: 14 }}>
                        {field.value}
                      </Typography>
                    </Box>
                  </Grid>
                ))}
              </Grid>

              <Divider sx={{ my: 3 }} />

              <Alert
                severity="info"
                sx={{ borderRadius: 2, '& .MuiAlert-message': { fontSize: 13 } }}
              >
                Pour modifier vos informations (nom, email…), veuillez contacter un administrateur.
              </Alert>
            </Box>
          )}

          {/* ── SECURITY TAB ── */}
          {activeTab === 'security' && (
            <Box sx={{ maxWidth: 480 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 0.5 }}>
                Changer le mot de passe
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3, lineHeight: 1.6 }}>
                Le mot de passe doit contenir au moins 8 caractères, incluant une majuscule, une minuscule, un chiffre et un caractère spécial.
              </Typography>

              {success && <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>{success}</Alert>}
              {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {[
                  { label: 'Ancien mot de passe', value: ancienMdp, setter: setAncienMdp, show: showAncien, toggle: () => setShowAncien((v) => !v) },
                  { label: 'Nouveau mot de passe', value: nouveauMdp, setter: setNouveauMdp, show: showNouveau, toggle: () => setShowNouveau((v) => !v) },
                  { label: 'Confirmer le nouveau mot de passe', value: confirmMdp, setter: setConfirmMdp, show: showConfirm, toggle: () => setShowConfirm((v) => !v),
                    error: confirmMdp.length > 0 && confirmMdp !== nouveauMdp,
                    helperText: confirmMdp.length > 0 && confirmMdp !== nouveauMdp ? 'Les mots de passe ne correspondent pas' : '' },
                ].map((field) => (
                  <TextField
                    key={field.label}
                    label={field.label}
                    type={field.show ? 'text' : 'password'}
                    value={field.value}
                    onChange={(e) => field.setter(e.target.value)}
                    fullWidth
                    size="small"
                    error={field.error}
                    helperText={field.helperText}
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <IconButton size="small" onClick={field.toggle} edge="end">
                              {field.show ? <EyeSlashIcon width={18} /> : <EyeIcon width={18} />}
                            </IconButton>
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                ))}

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', pt: 1 }}>
                  <Button
                    variant="contained"
                    onClick={handleChangePassword}
                    disabled={saving}
                    sx={{
                      borderRadius: 2,
                      textTransform: 'none',
                      fontWeight: 600,
                      px: 4,
                      py: 1,
                      bgcolor: PRIMARY,
                      '&:hover': { bgcolor: '#2E624A' },
                    }}
                  >
                    {saving ? <CircularProgress size={18} color="inherit" /> : 'Enregistrer le nouveau mot de passe'}
                  </Button>
                </Box>
              </Box>
            </Box>
          )}
        </Box>
      </Paper>
    </Box>
  );
}
