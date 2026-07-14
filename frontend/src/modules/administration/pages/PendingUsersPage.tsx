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
  Chip,
  CircularProgress,
  Alert,
} from '@mui/material';
import { CheckIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface PendingUser {
  id: string;
  email: string;
  prenom: string;
  nom: string;
  matricule: string;
  dateCreation: string;
  roles: { id: string; nom: string }[];
}

export default function PendingUsersPage() {
  const [users, setUsers] = useState<PendingUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [error, setError] = useState('');

  const loadPending = async () => {
    try {
      setError('');
      const res = await AdminService.listPendingUsers();
      setUsers(Array.isArray(res) ? res : []);
    } catch (err) {
      console.error('Erreur chargement inscriptions', err);
      setError('Impossible de charger les inscriptions en attente');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPending();
  }, []);

  const handleApprove = async (id: string) => {
    if (!window.confirm('Approuver cette inscription ?')) return;
    setActionLoading(id);
    try {
      await AdminService.approveUser(id);
      setUsers((prev) => prev.filter((u) => u.id !== id));
    } catch (err) {
      console.error('Erreur approbation', err);
      setError("Erreur lors de l'approbation");
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (id: string) => {
    if (!window.confirm('Rejeter cette inscription ? Le compte sera supprimé.')) return;
    setActionLoading(id);
    try {
      await AdminService.rejectUser(id);
      setUsers((prev) => prev.filter((u) => u.id !== id));
    } catch (err) {
      console.error('Erreur rejet', err);
      setError('Erreur lors du rejet');
    } finally {
      setActionLoading(null);
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
            Inscriptions en attente
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Valider ou rejeter les demandes de création de compte
          </Typography>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Nom</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Email</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Rôle</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Date d'inscription</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Aucune inscription en attente
                  </TableCell>
                </TableRow>
              )}
              {users.map((user) => (
                <TableRow key={user.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {user.prenom} {user.nom}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {user.matricule}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {user.email}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      {user.roles?.map((role) => (
                        <Chip
                          key={role.id}
                          label={role.nom}
                          size="small"
                          color="primary"
                          variant="outlined"
                          sx={{ borderRadius: 1.5, fontWeight: 500 }}
                        />
                      ))}
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {user.dateCreation
                        ? new Date(user.dateCreation).toLocaleDateString('fr-FR', {
                            day: 'numeric',
                            month: 'long',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit',
                          })
                        : '-'}
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      size="small"
                      variant="contained"
                      color="success"
                      startIcon={<CheckIcon width={16} />}
                      disabled={actionLoading === user.id}
                      onClick={() => handleApprove(user.id)}
                      sx={{ mr: 1, borderRadius: 2, textTransform: 'none' }}
                    >
                      Approuver
                    </Button>
                    <Button
                      size="small"
                      variant="outlined"
                      color="error"
                      startIcon={<XMarkIcon width={16} />}
                      disabled={actionLoading === user.id}
                      onClick={() => handleReject(user.id)}
                      sx={{ borderRadius: 2, textTransform: 'none' }}
                    >
                      Rejeter
                    </Button>
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