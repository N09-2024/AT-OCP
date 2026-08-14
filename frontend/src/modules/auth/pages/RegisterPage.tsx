import React, { useState } from 'react';
import {
  Typography,
  Paper,
  Box,
  Button,
  TextField,
  Alert,
  Link as MuiLink,
  Container,
  Stack,
} from '@mui/material';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link } from 'react-router-dom';
import { apiClient } from '../../../services/apiClient';

/**
 * NOTE POLICES — à ajouter dans public/index.html (ou via @font-face) :
 * <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@500;700&family=Inter:wght@400;500;600&family=IBM+Plex+Mono:wght@500&display=swap" rel="stylesheet">
 */

// --- Design tokens — palette verte, dégradés (identiques à LoginPage) ---
const DEEP = '#0E2A21';          // vert très profond, quasi noir (fond héro)
const FOREST = '#1F4D3E';        // vert OCP, couleur de marque
const MOSS = '#3C7A5C';          // vert intermédiaire, dégradés
const MINT = '#7FC8A9';          // vert clair, accents et texte sur fond sombre
const SAGE = '#EDF2EE';          // fond général, blanc verdâtre
const INK = '#16241E';           // texte principal
const SLATE = '#5C6E67';         // texte secondaire

const GRADIENT_HERO = `linear-gradient(160deg, ${DEEP} 0%, ${FOREST} 65%, ${MOSS} 130%)`;
const GRADIENT_CTA = `linear-gradient(135deg, ${MOSS} 0%, ${FOREST} 100%)`;
const GRADIENT_CTA_HOVER = `linear-gradient(135deg, #34694E 0%, #163C30 100%)`;

const FONT_DISPLAY = "'Space Grotesk', sans-serif";
const FONT_BODY = "'Inter', sans-serif";
const FONT_MONO = "'IBM Plex Mono', monospace";

const registerSchema = z.object({
  prenom: z.string().min(1, 'Le prénom est requis'),
  nom: z.string().min(1, 'Le nom est requis'),
  email: z.string().email("Format d'email invalide").min(1, "L'email est requis"),
  motDePasse: z.string().min(8, 'Le mot de passe doit contenir au moins 8 caractères')
    .regex(/[A-Z]/, 'Le mot de passe doit contenir au moins une majuscule')
    .regex(/[0-9]/, 'Le mot de passe doit contenir au moins un chiffre'),
  confirmMotDePasse: z.string().min(1, 'La confirmation est requise'),
}).refine((data) => data.motDePasse === data.confirmMotDePasse, {
  message: 'Les mots de passe ne correspondent pas',
  path: ['confirmMotDePasse'],
});

type RegisterFormInputs = z.infer<typeof registerSchema>;

// Champ de saisie stylé "souche de permis", cohérent avec LoginPage
function FormField({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <Box>
      <Typography sx={{ fontFamily: FONT_MONO, fontSize: 11, fontWeight: 500, color: SLATE, mb: 0.75, letterSpacing: 0.5, display: 'block' }}>
        {label}
      </Typography>
      {children}
    </Box>
  );
}

const fieldSx = {
  borderRadius: 1,
  bgcolor: SAGE,
  fontFamily: FONT_BODY,
  '& fieldset': { borderColor: `${SLATE}44` },
  '&:hover fieldset': { borderColor: FOREST },
  '&.Mui-focused fieldset': { borderColor: FOREST },
};

export default function RegisterPage() {
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormInputs>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormInputs) => {
    try {
      setError(null);
      await apiClient.post('/auth/register', {
        prenom: data.prenom,
        nom: data.nom,
        email: data.email,
        motDePasse: data.motDePasse,
      });
      setSuccess(true);
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Erreur lors de l'inscription";
      setError(msg);
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
        <svg width="32" height="32" viewBox="0 0 100 100" fill="none">
          <defs>
            <linearGradient id="logoGradReg" x1="0" y1="0" x2="100" y2="100" gradientUnits="userSpaceOnUse">
              <stop offset="0%" stopColor={MOSS} />
              <stop offset="100%" stopColor={FOREST} />
            </linearGradient>
          </defs>
          <path d="M50 10L61 40H93L67 59L77 89L50 70L23 89L33 59L7 40H39L50 10Z" fill="url(#logoGradReg)" />
          <circle cx="50" cy="50" r="14" fill={SAGE} />
          <circle cx="50" cy="50" r="9" fill={FOREST} />
        </svg>
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
              bgcolor: '#ffffff',
            }}
          >
            {/* LEFT COLUMN: FORM — style souche de permis */}
            <Box
              sx={{
                width: { xs: '100%', md: '48%' },
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

              {success ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', pt: { xs: 2, md: 6 } }}>
                  <Box sx={{ width: 56, height: 56, borderRadius: 1, border: `1px solid ${MOSS}66`, display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 3, bgcolor: SAGE }}>
                    <AssignmentTurnedInIcon sx={{ fontSize: 30, color: FOREST }} />
                  </Box>
                  <Typography sx={{ fontFamily: FONT_MONO, fontSize: 11, color: MOSS, letterSpacing: 1.5, fontWeight: 500, mb: 1 }}>
                    DEMANDE ENREGISTRÉE
                  </Typography>
                  <Typography
                    variant="h4"
                    sx={{ fontFamily: FONT_DISPLAY, fontWeight: 700, color: INK, letterSpacing: -0.3, mb: 1.5 }}
                  >
                    Compte en attente
                  </Typography>
                  <Typography variant="body2" sx={{ color: SLATE, fontSize: '0.9rem', lineHeight: 1.6, mb: 4 }}>
                    Votre inscription a bien été transmise. Un administrateur doit valider votre compte avant
                    votre première connexion — vous serez averti dès son activation.
                  </Typography>
                  <Button
                    component={Link}
                    to="/auth/login"
                    variant="contained"
                    fullWidth
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
                    Retour à la connexion
                  </Button>
                </Box>
              ) : (
                <>
                  <Box sx={{ mb: 4 }}>
                    <Typography
                      sx={{ fontFamily: FONT_MONO, fontSize: 11, color: MOSS, letterSpacing: 1.5, fontWeight: 500, mb: 1 }}
                    >
                      NOUVELLE DEMANDE D'ACCÈS
                    </Typography>
                    <Typography
                      variant="h4"
                      sx={{ fontFamily: FONT_DISPLAY, fontWeight: 700, color: INK, letterSpacing: -0.3, mb: 1.5 }}
                    >
                      Créer un compte
                    </Typography>
                    <Typography variant="body2" sx={{ color: SLATE, fontSize: '0.9rem', lineHeight: 1.6 }}>
                      Renseignez vos informations pour demander l'accès à la plateforme. Votre compte sera
                      activé après validation par un administrateur.
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
                    <Box sx={{ display: 'flex', gap: 2 }}>
                      <FormField label="PRÉNOM">
                        <TextField
                          placeholder="Karim"
                          variant="outlined"
                          fullWidth
                          {...register('prenom')}
                          error={!!errors.prenom}
                          helperText={errors.prenom?.message}
                          slotProps={{ input: { sx: fieldSx } }}
                        />
                      </FormField>
                      <FormField label="NOM">
                        <TextField
                          placeholder="Bennani"
                          variant="outlined"
                          fullWidth
                          {...register('nom')}
                          error={!!errors.nom}
                          helperText={errors.nom?.message}
                          slotProps={{ input: { sx: fieldSx } }}
                        />
                      </FormField>
                    </Box>

                    <FormField label="EMAIL">
                      <TextField
                        placeholder="prenom.nom@ocp.ma"
                        type="email"
                        variant="outlined"
                        fullWidth
                        {...register('email')}
                        error={!!errors.email}
                        helperText={errors.email?.message}
                        autoComplete="email"
                        slotProps={{ input: { sx: fieldSx } }}
                      />
                    </FormField>

                    <FormField label="MOT DE PASSE">
                      <TextField
                        placeholder="8 caractères min, 1 majuscule, 1 chiffre"
                        type="password"
                        variant="outlined"
                        fullWidth
                        {...register('motDePasse')}
                        error={!!errors.motDePasse}
                        helperText={errors.motDePasse?.message || '8 caractères min, 1 majuscule, 1 chiffre'}
                        slotProps={{ input: { sx: fieldSx } }}
                      />
                    </FormField>

                    <FormField label="CONFIRMER LE MOT DE PASSE">
                      <TextField
                        placeholder="Retapez votre mot de passe"
                        type="password"
                        variant="outlined"
                        fullWidth
                        {...register('confirmMotDePasse')}
                        error={!!errors.confirmMotDePasse}
                        helperText={errors.confirmMotDePasse?.message}
                        slotProps={{ input: { sx: fieldSx } }}
                      />
                    </FormField>

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
                      {isSubmitting ? 'Inscription en cours…' : "S'inscrire"}
                    </Button>

                    <Box sx={{ textAlign: 'center', mt: 0.5 }}>
                      <Typography variant="body2" sx={{ color: SLATE }}>
                        Déjà un compte ?{' '}
                        <MuiLink
                          component={Link}
                          to="/auth/login"
                          sx={{ color: FOREST, fontWeight: 600, textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
                        >
                          Se connecter
                        </MuiLink>
                      </Typography>
                    </Box>
                  </Box>
                </>
              )}
            </Box>

            {/* RIGHT COLUMN: PANNEAU HÉRO — style plan technique, dégradé vert */}
            <Box
              sx={{
                width: { xs: '100%', md: '52%' },
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
                Rejoignez la plateforme OCP
              </Typography>

              <Typography sx={{ color: `${SAGE}CC`, fontSize: '1rem', mb: 4, maxWidth: 460, lineHeight: 1.65 }}>
                Un seul compte pour émettre, suivre et clôturer vos Autorisations de Travail, avec une
                traçabilité complète à chaque étape du processus.
              </Typography>

              <Stack spacing={1.75} sx={{ maxWidth: 480 }}>
                {[
                  'Validation par un administrateur avant premier accès',
                  'Un espace personnel pour suivre vos demandes',
                  'Des échanges sécurisés avec les équipes HSE',
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