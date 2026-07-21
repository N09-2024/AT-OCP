import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Chip, CircularProgress,
  Alert, TextField, InputAdornment, FormControl, InputLabel, Select,
  MenuItem, Stack, TablePagination, Avatar,
} from '@mui/material';
import { CheckIcon, XMarkIcon, MagnifyingGlassIcon, FunnelIcon, ArrowPathIcon } from '@heroicons/react/24/outline';
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

  // Filters
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

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

  useEffect(() => { loadPending(); }, []);

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

  // All unique roles for filter
  const allRoles = useMemo(() => {
    const set = new Set<string>();
    users.forEach(u => u.roles?.forEach(r => set.add(r.nom)));
    return [...set].sort();
  }, [users]);

  const filtered = useMemo(() => {
    return users.filter(u => {
      const q = search.toLowerCase();
      const matchSearch = !search ||
        u.prenom.toLowerCase().includes(q) ||
        u.nom.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        (u.matricule && u.matricule.toLowerCase().includes(q));
      const matchRole = !roleFilter || u.roles?.some(r => r.nom === roleFilter);
      return matchSearch && matchRole;
    });
  }, [users, search, roleFilter]);

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  const hasFilters = search || roleFilter;

  const resetFilters = () => {
    setSearch('');
    setRoleFilter('');
    setPage(0);
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
            {users.length} demande(s) en attente de validation
          </Typography>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, borderBottom: '1px solid #f1f5f9' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="center">
            <TextField
              size="small"
              placeholder="Rechercher par nom, email, matricule..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              sx={{
              flex: '2 1 300px', maxWidth: 400,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <MagnifyingGlassIcon style={{ width: 18, height: 18, color: '#94a3b8' }} />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              select
              size="small"
              label="Rôle demandé"
              value={roleFilter}
              onChange={(e) => { setRoleFilter(e.target.value); setPage(0); }}
              sx={{
              minWidth: 220, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Tous les rôles</em></MenuItem>
              {allRoles.map(r => (
                <MenuItem key={r} value={r}>{r.replace(/_/g, ' ')}</MenuItem>
              ))}
            </TextField>

            {hasFilters && (
              <Button
                size="small"
                variant="outlined"
                startIcon={<ArrowPathIcon width={14} />}
                onClick={resetFilters}
                sx={{ borderRadius: 2, borderColor: '#e2e8f0', color: 'text.secondary', whiteSpace: 'nowrap', height: 40 }}
              >
                Réinitialiser
              </Button>
            )}
          </Stack>

          {hasFilters && (
            <Box sx={{ display: 'flex', gap: 1, mt: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
              {search && <Chip label={`Recherche: "${search}"`} size="small" onDelete={() => setSearch('')} sx={{ borderRadius: 1.5 }} />}
              {roleFilter && <Chip label={`Rôle: ${roleFilter}`} size="small" onDelete={() => setRoleFilter('')} sx={{ borderRadius: 1.5 }} />}
              <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center' }}>
                {filtered.length} résultat(s)
              </Typography>
            </Box>
          )}
        </Box>

        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700, fontSize: 12, color: '#64748b', bgcolor: '#f8fafc', textTransform: 'uppercase', letterSpacing: 0.5 } }}>
                <TableCell>Utilisateur</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Rôle demandé</TableCell>
                <TableCell>Date d'inscription</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucune inscription ne correspond à ces filtres.' : 'Aucune inscription en attente'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((user) => (
                  <TableRow key={user.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Avatar sx={{ width: 34, height: 34, bgcolor: '#f59e0b', fontSize: 13, fontWeight: 700 }}>
                          {user.prenom?.[0]}{user.nom?.[0]}
                        </Avatar>
                        <Box>
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {user.prenom} {user.nom}
                          </Typography>
                          {user.matricule && (
                            <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                              {user.matricule}
                            </Typography>
                          )}
                        </Box>
                      </Box>
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
                            label={role.nom.replace(/_/g, ' ')}
                            size="small"
                            sx={{ borderRadius: 1.5, fontWeight: 600, fontSize: 11, bgcolor: '#eff6ff', color: '#3b82f6', border: '1px solid #bfdbfe' }}
                          />
                        ))}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {user.dateCreation
                          ? new Date(user.dateCreation).toLocaleDateString('fr-FR', {
                              day: '2-digit', month: 'short', year: 'numeric',
                              hour: '2-digit', minute: '2-digit'
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
                        sx={{ mr: 1, borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
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
                        sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
                      >
                        Rejeter
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <TablePagination
          component="div"
          count={filtered.length}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => { setRowsPerPage(parseInt(e.target.value, 10)); setPage(0); }}
          rowsPerPageOptions={[10, 15, 25, 50]}
          labelRowsPerPage="Lignes par page"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} sur ${count}`}
        />
      </Paper>
    </Box>
  );
}