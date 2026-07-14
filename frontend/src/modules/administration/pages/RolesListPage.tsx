import { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Chip,
  CircularProgress,
} from '@mui/material';
import { Link } from 'react-router-dom';
import { PlusIcon, PencilSquareIcon, TrashIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface Role {
  id: string;
  nom: string;
  description: string;
  permissionsCount: number;
  usersCount: number;
}

export default function RolesListPage() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadRoles = async () => {
      try {
        const res = await AdminService.listRoles();
        setRoles(res.content);
      } catch (err) {
        console.error('Erreur chargement rôles', err);
      } finally {
        setLoading(false);
      }
    };
    loadRoles();
  }, []);

  const handleDelete = async (id: string) => {
    if (!window.confirm('Confirmer la suppression de ce rôle ?')) return;
    try {
      await AdminService.deleteRole(id);
      setRoles((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      console.error('Erreur suppression rôle', err);
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
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
            Rôles & Permissions
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gérer les rôles et les permissions d'accès
          </Typography>
        </Box>
        <Button
          component={Link}
          to="/administration/roles/nouveau"
          variant="contained"
          color="success"
          startIcon={<PlusIcon width={18} />}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
        >
          Nouveau rôle
        </Button>
      </Box>

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Nom du rôle</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Description</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Permissions</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Utilisateurs</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {roles.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Aucun rôle trouvé
                  </TableCell>
                </TableRow>
              )}
              {roles.map((role) => (
                <TableRow key={role.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {role.nom}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {role.description || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={`${role.permissionsCount} permissions`}
                      size="small"
                      color="primary"
                      variant="outlined"
                      sx={{ borderRadius: 1.5, fontWeight: 500 }}
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={`${role.usersCount} utilisateur(s)`}
                      size="small"
                      variant="outlined"
                      sx={{ borderRadius: 1.5, fontWeight: 500 }}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      component={Link}
                      to={`/administration/roles/${role.id}`}
                      size="small"
                      sx={{ color: 'primary.main', mr: 0.5 }}
                    >
                      <PencilSquareIcon width={18} />
                    </IconButton>
                    <IconButton
                      size="small"
                      sx={{ color: 'error.main' }}
                      onClick={() => handleDelete(role.id)}
                    >
                      <TrashIcon width={18} />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
    </Box>
  );
}