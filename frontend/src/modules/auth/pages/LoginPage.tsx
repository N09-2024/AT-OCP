import React, { useState } from 'react';
import {
  Typography,
  Paper,
  Box,
  Button,
  TextField,
  Alert,
  Link as MuiLink,
  Checkbox,
  FormControlLabel,
  InputAdornment,
  IconButton,
  Container,
  Stack,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import { AuthService } from '../../../services/AuthService';
import { OCP, OCP_FONTS, OCP_GRADIENTS } from '../../../theme/tokens';

/**
 * NOTE POLICES - à ajouter dans public/index.html (ou via @font-face) :
 * <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@500;700&family=Inter:wght@400;500;600&family=IBM+Plex+Mono:wght@500&display=swap" rel="stylesheet">
 */

const { deep: DEEP, forest: FOREST, moss: MOSS, mint: MINT, sage: SAGE, ink: INK, slate: SLATE } = OCP;
const { hero: GRADIENT_HERO, cta: GRADIENT_CTA, ctaHover: GRADIENT_CTA_HOVER } = OCP_GRADIENTS;
const FONT_DISPLAY = OCP_FONTS.display;
const FONT_BODY = OCP_FONTS.body;
const FONT_MONO = OCP_FONTS.mono;

const loginSchema = z.object({
  email: z.string().email("Format d'email invalide").min(1, "L'email est requis"),
  password: z.string().min(1, 'Le mot de passe est requis'),
});

type LoginFormInputs = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormInputs>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormInputs) => {
    try {
      setError(null);
      const response = await AuthService.login(data);
      login(response.user, response.token, response.permissions);
      navigate('/dashboard');
    } catch (err: any) {
      console.error('[LoginPage] Error logging in:', err);
      const backendMsg = err?.response?.data?.message || err?.response?.data?.error || err?.message;
      if (err?.response?.status === 401) {
        setError(backendMsg || 'Email ou mot de passe incorrect.');
      } else if (err?.code === 'ERR_NETWORK') {
        setError('Impossible de contacter le serveur backend. Vérifiez que le serveur est démarré.');
      } else {
        setError(backendMsg || 'Erreur lors de la connexion. Veuillez réessayer.');
      }
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        width: '100%',
        bgcolor: SAGE,
        fontFamily: FONT_BODY,
        display: 'flex',
        flexDirection: 'column',
        p: { xs: 2, sm: 3, md: 4 },
      }}
    >
      {/* Top Left Brand Logo */}
      <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <Box
          component="img"
          src="/OCP_Group.svg.webp"
          alt="Logo OCP"
          sx={{ width: 40, height: 40, objectFit: 'contain' }}
        />
        <Box>
          <Typography
            sx={{ fontFamily: FONT_DISPLAY, fontWeight: 700, fontSize: '1.05rem', color: FOREST, letterSpacing: 0.2, lineHeight: 1 }}
          >
            OCP GROUP
          </Typography>
          <Typography
            sx={{ fontFamily: FONT_MONO, color: MOSS, fontWeight: 500, fontSize: 10, letterSpacing: 1.2, display: 'block', mt: 0.4 }}
          >
            GESTION AT &amp; HSE
          </Typography>
        </Box>
      </Box>

      {/* Main Split Layout Container */}
      <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Container maxWidth="lg" disableGutters sx={{ width: '100%' }}>
          <Paper
            elevation={0}
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', md: 'row' },
              borderRadius: 2,
              overflow: 'hidden',
              border: `1px solid ${FOREST}22`,
              boxShadow: '0 24px 48px -20px rgba(14,42,33,0.28)',
              bgcolor: '#FFFFFF',
            }}
          >
            {/* LEFT COLUMN: LOGIN FORM - style souche de permis */}
            <Box
              sx={{
                width: { xs: '100%', md: '42%' },
                p: { xs: 3, sm: 5 },
                position: 'relative',
                borderRight: { md: `1px dashed ${SLATE}55` },
              }}
            >
              {/* perforations le long de la bordure droite (desktop) */}
              <Box
                sx={{
                  display: { xs: 'none', md: 'flex' },
                  position: 'absolute',
                  right: -7,
                  top: 0,
                  bottom: 0,
                  flexDirection: 'column',
                  justifyContent: 'space-evenly',
                }}
              >
                {Array.from({ length: 9 }).map((_, i) => (
                  <Box key={i} sx={{ width: 14, height: 14, borderRadius: '50%', bgcolor: SAGE, border: `1px solid ${SLATE}33` }} />
                ))}
              </Box>

              <Box sx={{ mb: 4 }}>
                <Typography
                  sx={{ fontFamily: FONT_MONO, fontSize: 11, color: MOSS, letterSpacing: 1.5, fontWeight: 500, mb: 1 }}
                >
                  ACCÈS SÉCURISÉ
                </Typography>
                <Typography
                  variant="h4"
                  sx={{ fontFamily: FONT_DISPLAY, fontWeight: 700, color: INK, letterSpacing: -0.3, mb: 1.5 }}
                >
                  Bon retour
                </Typography>
                <Typography variant="body2" sx={{ color: SLATE, fontSize: '0.9rem', lineHeight: 1.6 }}>
                  Connectez-vous pour retrouver vos Autorisations de Travail et suivre vos permis en cours.
                </Typography>
              </Box>

              {error && (
                <Alert
                  severity="error"
                  sx={{ mb: 3, borderRadius: 1, fontWeight: 500, fontSize: '0.85rem', bgcolor: '#FBEAE3', color: '#7A2E1A' }}
                >
                  {error}
                </Alert>
              )}

              <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                {/* Email Field */}
                <Box>
                  <Typography sx={{ fontFamily: FONT_MONO, fontSize: 11, fontWeight: 500, color: SLATE, mb: 0.75, letterSpacing: 0.5, display: 'block' }}>
                    EMAIL
                  </Typography>
                  <TextField
                    placeholder="prenom.nom@ocp.ma"
                    variant="outlined"
                    fullWidth
                    {...register('email')}
                    error={!!errors.email}
                    helperText={errors.email?.message}
                    autoComplete="email"
                    slotProps={{
                      input: {
                        sx: {
                          borderRadius: 1,
                          bgcolor: SAGE,
                          fontFamily: FONT_BODY,
                          '& fieldset': { borderColor: `${SLATE}44` },
                          '&:hover fieldset': { borderColor: FOREST },
                          '&.Mui-focused fieldset': { borderColor: FOREST },
                        },
                      },
                    }}
                  />
                </Box>

                {/* Password Field */}
                <Box>
                  <Typography sx={{ fontFamily: FONT_MONO, fontSize: 11, fontWeight: 500, color: SLATE, mb: 0.75, letterSpacing: 0.5, display: 'block' }}>
                    MOT DE PASSE
                  </Typography>
                  <TextField
                    placeholder="Votre mot de passe"
                    type={showPassword ? 'text' : 'password'}
                    variant="outlined"
                    fullWidth
                    {...register('password')}
                    error={!!errors.password}
                    helperText={errors.password?.message}
                    autoComplete="current-password"
                    slotProps={{
                      input: {
                        sx: {
                          borderRadius: 1,
                          bgcolor: SAGE,
                          fontFamily: FONT_BODY,
                          '& fieldset': { borderColor: `${SLATE}44` },
                          '&:hover fieldset': { borderColor: FOREST },
                          '&.Mui-focused fieldset': { borderColor: FOREST },
                        },
                        endAdornment: (
                          <InputAdornment position="end">
                            <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" size="small" sx={{ color: SLATE }}>
                              {showPassword ? <VisibilityOff sx={{ fontSize: 20 }} /> : <Visibility sx={{ fontSize: 20 }} />}
                            </IconButton>
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                </Box>

                {/* Remember Me */}
                <FormControlLabel
                  sx={{ mt: -1 }}
                  control={
                    <Checkbox
                      checked={rememberMe}
                      onChange={(e) => setRememberMe(e.target.checked)}
                      size="small"
                      sx={{ color: `${SLATE}88`, '&.Mui-checked': { color: FOREST } }}
                    />
                  }
                  label={<Typography sx={{ color: SLATE, fontSize: '0.85rem' }}>Se souvenir de moi</Typography>}
                />

                {/* Submit Button */}
                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  disabled={isSubmitting}
                  sx={{
                    py: 1.4,
                    borderRadius: 1,
                    fontFamily: FONT_DISPLAY,
                    fontSize: '0.95rem',
                    fontWeight: 700,
                    textTransform: 'none',
                    letterSpacing: 0.3,
                    background: GRADIENT_CTA,
                    color: '#FFFFFF',
                    boxShadow: 'none',
                    transition: 'background 0.2s ease',
                    '&:hover': { background: GRADIENT_CTA_HOVER, boxShadow: 'none' },
                  }}
                >
                  {isSubmitting ? 'Connexion en cours…' : 'Se connecter'}
                </Button>

                <Box sx={{ textAlign: 'center', mt: 0.5 }}>
                  <Typography variant="body2" sx={{ color: SLATE }}>
                    Pas encore de compte ?{' '}
                    <MuiLink
                      component={Link}
                      to="/auth/register"
                      sx={{ color: FOREST, fontWeight: 600, textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
                    >
                      Créer un compte
                    </MuiLink>
                  </Typography>
                </Box>
              </Box>
            </Box>

            {/* RIGHT COLUMN: PANNEAU HÉRO - style plan technique, dégradé vert */}
            <Box
              sx={{
                width: { xs: '100%', md: '58%' },
                display: { xs: 'none', md: 'flex' },
                flexDirection: 'column',
                justifyContent: 'center',
                position: 'relative',
                overflow: 'hidden',
                background: GRADIENT_HERO,
                color: SAGE,
                p: { md: 6, lg: 7 },
                backgroundImage: `linear-gradient(${SAGE}11 1px, transparent 1px), linear-gradient(90deg, ${SAGE}11 1px, transparent 1px), ${GRADIENT_HERO}`,
                backgroundSize: '28px 28px, 28px 28px, 100% 100%',
              }}
            >
              <Box sx={{ width: 56, height: 56, borderRadius: 1, border: `1px solid ${MINT}66`, display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 4 }}>
                <AssignmentTurnedInIcon sx={{ fontSize: 30, color: MINT }} />
              </Box>

              <Typography sx={{ fontFamily: FONT_MONO, fontSize: 12, color: MINT, letterSpacing: 2, mb: 1.5 }}>
                STANDARD S-HSE-SEC-31
              </Typography>

              <Typography
                variant="h3"
                sx={{ fontFamily: FONT_DISPLAY, fontWeight: 700, lineHeight: 1.15, mb: 2, maxWidth: 480, letterSpacing: -0.5 }}
              >
                Chaque intervention, sous contrôle
              </Typography>

              <Typography sx={{ color: `${SAGE}CC`, fontSize: '1rem', mb: 4, maxWidth: 460, lineHeight: 1.65 }}>
                La plateforme de référence d'OCP Group pour émettre, suivre et clôturer les Autorisations de
                Travail en toute traçabilité, du terrain jusqu'au bureau HSE.
              </Typography>

              <Stack spacing={1.75} sx={{ maxWidth: 480 }}>
                {[
                  'Un dossier numérique unique, de la demande à la clôture',
                  'Signatures et validations horodatées à chaque étape',
                  'Une vision claire de vos chantiers, en temps réel',
                ].map((text, idx) => (
                  <Box key={idx} sx={{ display: 'flex', alignItems: 'flex-start', gap: 1.5 }}>
                    <Box sx={{ width: 5, height: 5, borderRadius: '50%', bgcolor: MINT, mt: 1, flexShrink: 0 }} />
                    <Typography sx={{ fontSize: '0.92rem', color: `${SAGE}E6` }}>{text}</Typography>
                  </Box>
                ))}
              </Stack>
            </Box>
          </Paper>
        </Container>
      </Box>
    </Box>
  );
}