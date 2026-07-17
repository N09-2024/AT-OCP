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
  TextField,
  InputAdornment,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon,
  PencilSquareIcon,
  NoSymbolIcon,
  CheckCircleIcon,
  MagnifyingGlassIcon,
} from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface User {
  id: string;
  email: string;
  prenom: string;
  nom: string;
  actived: boolean;
  roles: { id: string; nom: string }[];
  derniereConnexion?: string;
}

export default function UsersListPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const res = await AdminService.listUsers();
        setUsers(res.content);
      } catch (err) {
        console.error('Erreur chargement utilisateurs', err);
      } finally {
        setLoading(false);
      }
    };
    loadUsers();
  }, []);

  const handleToggleActive = async (user: User) => {
    const action = user.actived ? 'désactiver' : 'réactiver';
    if (!window.confirm(`Confirmer : ${action} le compte de ${user.prenom} ${user.nom} ?`)) return;
    try {
      const updated = user.actived
        ? await AdminService.deactivateUser(user.id)
        : await AdminService.activateUser(user.id);
      setUsers((prev) =>
        prev.map((u) => (u.id === user.id ? { ...u, actived: updated.actived } : u))
      );
    } catch (err) {
      console.error('Erreur changement de statut utilisateur', err);
    }
  };

  const filtered = users.filter(
    (u) =>
      u.prenom.toLowerCase().includes(search.toLowerCase()) ||
      u.nom.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase())
  );

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
            Utilisateurs
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gérer les comptes utilisateurs du système
          </Typography>
        </Box>
        <Button
          component={Link}
          to="/administration/utilisateurs/nouveau"
          variant="contained"
          color="success"
          startIcon={<PlusIcon width={18} />}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
        >
          Nouvel utilisateur
        </Button>
      </Box>

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <Box sx={{ p: 2, pb: 0 }}>
          <TextField
            size="small"
            placeholder="Rechercher un utilisateur..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            sx={{ width: 320, mb: 2 }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <MagnifyingGlassIcon width={18} />
                  </InputAdornment>
                ),
              },
            }}
          />
        </Box>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Nom</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Email</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Rôle</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Dernière connexion</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Aucun utilisateur trouvé
                  </TableCell>
                </TableRow>
              )}
              {filtered.map((user) => (
                <TableRow key={user.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {user.prenom} {user.nom}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {user.email}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      {user.roles.length > 0 ? (
                        user.roles.map((role) => (
                          <Chip
                            key={role.id}
                            label={role.nom}
                            size="small"
                            color="primary"
                            variant="outlined"
                            sx={{ borderRadius: 1.5, fontWeight: 500 }}
                          />
                        ))
                      ) : (
                        <Typography variant="body2" color="text.secondary">
                          Aucun rôle
                        </Typography>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={user.actived ? 'Actif' : 'Inactif'}
                      size="small"
                      color={user.actived ? 'success' : 'default'}
                      sx={{ borderRadius: 1.5, fontWeight: 500 }}
                    />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {user.derniereConnexion
                        ? new Date(user.derniereConnexion).toLocaleDateString('fr-FR')
                        : 'Jamais'}
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      component={Link}
                      to={`/administration/utilisateurs/${user.id}`}
                      size="small"
                      sx={{ color: 'primary.main', mr: 0.5 }}
                    >
                      <PencilSquareIcon width={18} />
                    </IconButton>
                    <IconButton
                      size="small"
                      sx={{ color: user.actived ? 'error.main' : 'success.main' }}
                      onClick={() => handleToggleActive(user)}
                      title={user.actived ? 'Désactiver le compte' : 'Réactiver le compte'}
                    >
                      {user.actived ? (
                        <NoSymbolIcon width={18} />
                      ) : (
                        <CheckCircleIcon width={18} />
                      )}
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