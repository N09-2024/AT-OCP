import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton,
  Chip, CircularProgress, TextField, InputAdornment, Stack,
  TablePagination,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon, PencilSquareIcon, TrashIcon,
  MagnifyingGlassIcon, ArrowPathIcon,
} from '@heroicons/react/24/outline';
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

  // Filters
  const [search, setSearch] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

  useEffect(() => {
    const loadRoles = async () => {
      try {
        const res = await AdminService.listRoles();
        setRoles(res.content || res);
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

  const filtered = useMemo(() => {
    return roles.filter(r =>
      !search ||
      r.nom.toLowerCase().includes(search.toLowerCase()) ||
      (r.description && r.description.toLowerCase().includes(search.toLowerCase()))
    );
  }, [roles, search]);

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  const hasFilters = search.length > 0;

  const resetFilters = () => {
    setSearch('');
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
            Rôles & Permissions
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {roles.length} rôles enregistrés dans le système
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
        <Box sx={{ p: 2.5, borderBottom: '1px solid #f1f5f9' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
            <TextField
              size="small"
              placeholder="Rechercher par nom ou description..."
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
                <TableCell>Nom du rôle</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Permissions</TableCell>
                <TableCell>Utilisateurs</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucun rôle ne correspond à cette recherche.' : 'Aucun rôle trouvé'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((role) => (
                  <TableRow key={role.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {role.nom.replace(/_/g, ' ')}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {role.description || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={`${role.permissionsCount || (role as any).permissions?.length || 0} permissions`}
                        size="small"
                        sx={{ borderRadius: 1.5, fontWeight: 600, fontSize: 11, bgcolor: '#eff6ff', color: '#3b82f6', border: '1px solid #bfdbfe' }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={`${role.usersCount || 0} utilisateur(s)`}
                        size="small"
                        sx={{ borderRadius: 1.5, fontWeight: 600, fontSize: 11, bgcolor: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0' }}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        component={Link}
                        to={`/administration/roles/${role.id}`}
                        size="small"
                        sx={{ color: '#3b82f6', mr: 0.5, '&:hover': { bgcolor: '#eff6ff' } }}
                      >
                        <PencilSquareIcon width={18} />
                      </IconButton>
                      <IconButton
                        size="small"
                        sx={{ color: '#ef4444', '&:hover': { bgcolor: '#fee2e2' } }}
                        onClick={() => handleDelete(role.id)}
                      >
                        <TrashIcon width={18} />
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