import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Alert, Box, Button, Typography, Paper } from '@mui/material';
import LockIcon from '@mui/icons-material/Lock';

export const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/auth/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

export const PermissionGuard: React.FC<{
  permission: string;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ permission, children, fallback }) => {
  const hasPermission = useAuthStore((state) => state.hasPermission(permission));
  const userRoles = useAuthStore((state) => state.user?.roles || []);
  const isAdmin = userRoles.some((r) => r.nom === 'ADMIN');

  if (isAdmin || hasPermission) {
    return <>{children}</>;
  }

  if (fallback) {
    return <>{fallback}</>;
  }

  return (
    <Box sx={{ p: 4, maxWidth: 600, mx: 'auto', mt: 4 }}>
      <Paper sx={{ p: 4, textAlign: 'center', border: '1px solid #fecdd3', bgcolor: '#fff1f2' }}>
        <LockIcon sx={{ fontSize: 48, color: '#e11d48', mb: 2 }} />
        <Typography variant="h6" color="#9f1239" gutterBottom sx={{ fontWeight: 700 }}>
          Accès Non Autorisé
        </Typography>
        <Typography variant="body2" color="#be123c" sx={{ mb: 3 }}>
          Vous ne disposez pas de la permission requise (<code>{permission}</code>) pour exécuter cette action.
        </Typography>
        <Button variant="outlined" color="error" onClick={() => window.history.back()}>
          Retour
        </Button>
      </Paper>
    </Box>
  );
};

export const RoleGuard: React.FC<{
  roles: string[];
  children: React.ReactNode;
  fallback?: React.ReactNode;
}> = ({ roles, children, fallback }) => {
  const userRoles = useAuthStore((state) => state.user?.roles || []);
  const hasRequiredRole = userRoles.some((r) => roles.includes(r.nom) || r.nom === 'ADMIN');

  if (hasRequiredRole) {
    return <>{children}</>;
  }

  if (fallback) {
    return <>{fallback}</>;
  }

  return (
    <Box sx={{ p: 4, maxWidth: 600, mx: 'auto', mt: 4 }}>
      <Paper sx={{ p: 4, textAlign: 'center', border: '1px solid #fecdd3', bgcolor: '#fff1f2' }}>
        <LockIcon sx={{ fontSize: 48, color: '#e11d48', mb: 2 }} />
        <Typography variant="h6" color="#9f1239" gutterBottom sx={{ fontWeight: 700 }}>
          Accès Restreint
        </Typography>
        <Typography variant="body2" color="#be123c" sx={{ mb: 3 }}>
          Cette page est réservée aux utilisateurs ayant l'un des rôles suivants : <strong>{roles.join(', ')}</strong>.
        </Typography>
        <Button variant="outlined" color="error" onClick={() => window.history.back()}>
          Retour
        </Button>
      </Paper>
    </Box>
  );
};
