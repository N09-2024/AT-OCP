import { Typography, Paper, Box, Button, TextField, Alert, Link as MuiLink } from '@mui/material';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import { AuthService } from '../../../services/AuthService';
import { useState } from 'react';

const loginSchema = z.object({
  email: z.string().email("Format d'email invalide").min(1, "L'email est requis"),
  password: z.string().min(1, 'Le mot de passe est requis'),
});

type LoginFormInputs = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [error, setError] = useState<string | null>(null);

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
    <Paper
      sx={{
        p: 4,
        width: '100%',
        maxWidth: 420,
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
          Système de Gestion AT
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Veuillez vous connecter pour continuer
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
        <TextField
          label="Email"
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
          {...register('password')}
          error={!!errors.password}
          helperText={errors.password?.message}
          autoComplete="current-password"
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
          {isSubmitting ? 'Connexion en cours...' : 'Se connecter'}
        </Button>

        <Box sx={{ textAlign: 'center', mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Pas encore de compte ?{' '}
            <MuiLink component={Link} to="/auth/register" sx={{ fontWeight: 600, cursor: 'pointer' }}>
              Créer un compte
            </MuiLink>
          </Typography>
        </Box>
      </Box>
    </Paper>
  );
}
