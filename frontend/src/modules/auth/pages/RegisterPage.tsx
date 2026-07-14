import { Typography, Paper, Box, Button, TextField, Alert, Link as MuiLink } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { apiClient } from '../../../services/apiClient';

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

export default function RegisterPage() {
  const { register } = useAuthStore();
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

  if (success) {
    return (
      <Paper
        sx={{
          p: 4,
          width: '100%',
          maxWidth: 480,
          borderRadius: 3,
          boxShadow: '0 10px 25px -5px rgba(0,0,0,.1), 0 8px 10px -6px rgba(0,0,0,.1)',
        }}
      >
        <Alert severity="success" sx={{ mb: 3 }}>
          Inscription réussie ! Votre compte est en attente de validation par un administrateur.
          Vous recevrez une notification une fois votre compte activé.
        </Alert>
        <Button
          component={Link}
          to="/auth/login"
          variant="contained"
          color="primary"
          fullWidth
          sx={{ mt: 2, py: 1.5 }}
        >
          Retour à la connexion
        </Button>
      </Paper>
    );
  }

  return (
    <Paper
      sx={{
        p: 4,
        width: '100%',
        maxWidth: 480,
        borderRadius: 3,
        boxShadow: '0 10px 25px -5px rgba(0,0,0,.1), 0 8px 10px -6px rgba(0,0,0,.1)',
      }}
    >
      {/* Logo block */}
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 4 }}>
        <Box
          sx={{
            width: 64,
            height: 64,
            bgcolor: 'primary.main',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontSize: 20,
            fontWeight: 'bold',
            mb: 2,
          }}
        >
          OCP
        </Box>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          Créer un compte
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Inscrivez-vous pour soumettre des autorisations de travail
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Box
        component="form"
        onSubmit={handleSubmit(onSubmit)}
        sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}
      >
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField
            label="Prénom"
            variant="outlined"
            fullWidth
            {...register('prenom')}
            error={!!errors.prenom}
            helperText={errors.prenom?.message}
          />
          <TextField
            label="Nom"
            variant="outlined"
            fullWidth
            {...register('nom')}
            error={!!errors.nom}
            helperText={errors.nom?.message}
          />
        </Box>

        <TextField
          label="Email"
          type="email"
          variant="outlined"
          fullWidth
          {...register('email')}
          error={!!errors.email}
          helperText={errors.email?.message}
          autoComplete="email"
        />

        <TextField
          label="Mot de passe"
          type="password"
          variant="outlined"
          fullWidth
          {...register('motDePasse')}
          error={!!errors.motDePasse}
          helperText={errors.motDePasse?.message || '8 caractères min, 1 majuscule, 1 chiffre'}
        />

        <TextField
          label="Confirmer le mot de passe"
          type="password"
          variant="outlined"
          fullWidth
          {...register('confirmMotDePasse')}
          error={!!errors.confirmMotDePasse}
          helperText={errors.confirmMotDePasse?.message}
        />

        <Button
          type="submit"
          variant="contained"
          color="primary"
          fullWidth
          size="large"
          disabled={isSubmitting}
          sx={{ mt: 2, py: 1.5, fontSize: '1rem' }}
        >
          {isSubmitting ? 'Inscription en cours...' : "S'inscrire"}
        </Button>

        <Box sx={{ textAlign: 'center', mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Déjà un compte ?{' '}
            <MuiLink component={Link} to="/auth/login" sx={{ fontWeight: 600, cursor: 'pointer' }}>
              Se connecter
            </MuiLink>
          </Typography>
        </Box>
      </Box>
    </Paper>
  );
}