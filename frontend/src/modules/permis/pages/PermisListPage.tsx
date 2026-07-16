import { useEffect, useState } from 'react';
import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { PermisService } from '../../../services/PermisService';
import type { PermisResponse } from '../../../services/PermisService';
import { PlusIcon, EyeIcon } from '@heroicons/react/24/outline';
import { useAuthStore } from '../../../store/authStore';

export default function PermisListPage() {
  const [permisList, setPermisList] = useState<PermisResponse[]>([]);
  const navigate = useNavigate();
  const hasRole = useAuthStore((s) => s.hasRole);

  useEffect(() => {
    loadPermis();
  }, []);

  const loadPermis = async () => {
    try {
      const data = await PermisService.getAllPermis();
      setPermisList(data);
    } catch (error) {
      console.error('Failed to load permis', error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CONFORME': return 'success';
      case 'NON_CONFORME': return 'error';
      case 'A_VERIFIER': return 'warning';
      default: return 'default';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }}>Gestion des Permis</Typography>
        {hasRole('CREATE_PERMIS') && (
          <Button
            variant="contained"
            startIcon={<PlusIcon width={20} />}
            onClick={() => navigate('/permis/nouveau')}
          >
            Nouveau Permis
          </Button>
        )}
      </Box>

      <TableContainer component={Paper} elevation={0} sx={{ border: '1px solid', borderColor: 'divider' }}>
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'grey.50' }}>
              <TableCell>Numéro</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Émission</TableCell>
              <TableCell>Expiration</TableCell>
              <TableCell>Statut IA</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {permisList.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                  <Typography color="text.secondary">Aucun permis trouvé</Typography>
                </TableCell>
              </TableRow>
            ) : (
              permisList.map((permis) => (
                <TableRow key={permis.id} hover>
                  <TableCell>{permis.numero}</TableCell>
                  <TableCell>{permis.typePermis?.nom}</TableCell>
                  <TableCell>{new Date(permis.dateEmission).toLocaleDateString()}</TableCell>
                  <TableCell>{new Date(permis.dateExpiration).toLocaleDateString()}</TableCell>
                  <TableCell>
                    <Chip
                      label={permis.statutVerification}
                      color={getStatusColor(permis.statutVerification) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      size="small"
                      startIcon={<EyeIcon width={16} />}
                      onClick={() => navigate(`/permis/${permis.id}`)}
                    >
                      Détails
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
