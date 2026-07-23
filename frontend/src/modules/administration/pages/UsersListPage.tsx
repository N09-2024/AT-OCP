import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton,
  Chip, CircularProgress, TextField, InputAdornment,
  Select, MenuItem, FormControl, InputLabel, Stack, Avatar,
  TablePagination,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon, PencilSquareIcon, NoSymbolIcon, CheckCircleIcon,
  MagnifyingGlassIcon, FunnelIcon, ArrowPathIcon,
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

  // Filters
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | 'actif' | 'inactif'>('');
  const [roleFilter, setRoleFilter] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

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
      setUsers((prev) => prev.map((u) => (u.id === user.id ? { ...u, actived: updated.actived } : u)));
    } catch (err) {
      console.error('Erreur changement de statut utilisateur', err);
    }
  };

  // All unique roles for filter dropdown
  const allRoles = useMemo(() => {
    const set = new Set<string>();
    users.forEach(u => u.roles.forEach(r => set.add(r.nom)));
    return [...set].sort();
  }, [users]);

  const filtered = useMemo(() => {
    return users.filter((u) => {
      const matchSearch =
        !search ||
        u.prenom.toLowerCase().includes(search.toLowerCase()) ||
        u.nom.toLowerCase().includes(search.toLowerCase()) ||
        u.email.toLowerCase().includes(search.toLowerCase());
      const matchStatus =
        !statusFilter ||
        (statusFilter === 'actif' ? u.actived : !u.actived);
      const matchRole =
        !roleFilter || u.roles.some(r => r.nom === roleFilter);
      return matchSearch && matchStatus && matchRole;
    });
  }, [users, search, statusFilter, roleFilter]);

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  const hasFilters = search || statusFilter || roleFilter;

  const resetFilters = () => {
    setSearch('');
    setStatusFilter('');
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
      {/* Header */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
            Utilisateurs
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {users.length} utilisateurs · {users.filter(u => u.actived).length} actifs
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
        {/* Search + Filters */}
        <Box sx={{ p: 2.5, borderBottom: '1px solid #f1f5f9' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
            <TextField
              size="small"
              placeholder="Rechercher par nom, prénom, email..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              sx={{
              flexGrow: 1,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <MagnifyingGlassIcon style={{ width: 18, height: 18, color: '#94a3b8' }} />
                    </InputAdornment>
                  ),
                }
              }}
            />

            <TextField
              select
              size="small"
              label="Statut"
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value as '' | 'actif' | 'inactif'); setPage(0); }}
              sx={{
              minWidth: 180, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Tous les statuts</em></MenuItem>
              <MenuItem value="actif">Actif</MenuItem>
              <MenuItem value="inactif">Inactif</MenuItem>
            </TextField>

            <TextField
              select
              size="small"
              label="Rôle"
              value={roleFilter}
              onChange={(e) => { setRoleFilter(e.target.value); setPage(0); }}
              sx={{
              minWidth: 200, flexShrink: 0,
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

          {/* Active filter chips */}
          {hasFilters && (
            <Box sx={{ display: 'flex', gap: 1, mt: 1.5, flexWrap: 'wrap' }}>
              {search && <Chip label={`Recherche: "${search}"`} size="small" onDelete={() => setSearch('')} sx={{ borderRadius: 1.5 }} />}
              {statusFilter && <Chip label={`Statut: ${statusFilter}`} size="small" onDelete={() => setStatusFilter('')} sx={{ borderRadius: 1.5 }} />}
              {roleFilter && <Chip label={`Rôle: ${roleFilter}`} size="small" onDelete={() => setRoleFilter('')} sx={{ borderRadius: 1.5 }} />}
              <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center' }}>
                {filtered.length} résultat(s)
              </Typography>
            </Box>
          )}
        </Box>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700, fontSize: 12, color: '#64748b', bgcolor: '#f8fafc', textTransform: 'uppercase', letterSpacing: 0.5 } }}>
                <TableCell>Utilisateur</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Rôle(s)</TableCell>
                <TableCell>Statut</TableCell>
                <TableCell>Dernière connexion</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucun utilisateur ne correspond à ces filtres.' : 'Aucun utilisateur trouvé'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((user) => (
                  <TableRow key={user.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Avatar sx={{ width: 34, height: 34, bgcolor: '#16a34a', fontSize: 13, fontWeight: 700 }}>
                          {user.prenom?.[0]}{user.nom?.[0]}
                        </Avatar>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>
                          {user.prenom} {user.nom}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">{user.email}</Typography>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                        {user.roles.length > 0 ? (
                          user.roles.map((role) => (
                            <Chip
                              key={role.id}
                              label={role.nom.replace(/_/g, ' ')}
                              size="small"
                              sx={{ borderRadius: 1.5, fontWeight: 600, fontSize: 11, bgcolor: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0' }}
                            />
                          ))
                        ) : (
                          <Typography variant="body2" color="text.secondary">Aucun rôle</Typography>
                        )}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={user.actived ? 'Actif' : 'Inactif'}
                        size="small"
                        sx={{
                          borderRadius: 1.5, fontWeight: 600, fontSize: 11,
                          bgcolor: user.actived ? '#f0fdf4' : '#f8fafc',
                          color: user.actived ? '#16a34a' : '#64748b',
                          border: `1px solid ${user.actived ? '#bbf7d0' : '#e2e8f0'}`,
                        }}
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
                        sx={{ color: '#3b82f6', mr: 0.5, '&:hover': { bgcolor: '#eff6ff' } }}
                        title="Modifier"
                      >
                        <PencilSquareIcon width={18} />
                      </IconButton>
                      <IconButton
                        size="small"
                        sx={{
                          color: user.actived ? '#ef4444' : '#16a34a',
                          '&:hover': { bgcolor: user.actived ? '#fee2e2' : '#f0fdf4' },
                        }}
                        onClick={() => handleToggleActive(user)}
                        title={user.actived ? 'Désactiver le compte' : 'Réactiver le compte'}
                      >
                        {user.actived ? <NoSymbolIcon width={18} /> : <CheckCircleIcon width={18} />}
                      </IconButton>
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