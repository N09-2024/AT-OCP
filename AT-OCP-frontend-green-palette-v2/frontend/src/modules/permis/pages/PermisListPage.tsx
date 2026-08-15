import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, Button,
  TextField, InputAdornment,
  MenuItem, Stack, TablePagination, CircularProgress, Alert
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { PermisService } from '../../../services/PermisService';
import type { PermisResponse } from '../../../services/PermisService';
import { PlusIcon, EyeIcon, MagnifyingGlassIcon, ArrowPathIcon } from '@heroicons/react/24/outline';
import { useAuthStore } from '../../../store/authStore';

export default function PermisListPage() {
  const [permisList, setPermisList] = useState<PermisResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const navigate = useNavigate();
  const hasRole = useAuthStore((s) => s.hasRole);

  // Filters
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

  useEffect(() => {
    loadPermis();
  }, []);

  const loadPermis = async () => {
    try {
      setLoading(true);
      const data = await PermisService.getAllPermis();
      setPermisList(data);
    } catch (error) {
      console.error('Failed to load permis', error);
      setError('Erreur lors du chargement des permis');
    } finally {
      setLoading(false);
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

  const availableTypes = useMemo(() => {
    return [...new Set(permisList.map(p => p.typePermis?.nom).filter(Boolean))];
  }, [permisList]);

  const availableStatuses = useMemo(() => {
    return [...new Set(permisList.map(p => p.statutVerification).filter(Boolean))];
  }, [permisList]);

  const filtered = useMemo(() => {
    return permisList.filter(p => {
      const q = search.toLowerCase();
      const matchSearch = !search || 
        p.numero?.toLowerCase().includes(q) || 
        p.typePermis?.nom?.toLowerCase().includes(q);
      const matchStatus = !statusFilter || p.statutVerification === statusFilter;
      const matchType = !typeFilter || p.typePermis?.nom === typeFilter;
      return matchSearch && matchStatus && matchType;
    });
  }, [permisList, search, statusFilter, typeFilter]);

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  const hasFilters = search || statusFilter || typeFilter;

  const resetFilters = () => {
    setSearch('');
    setStatusFilter('');
    setTypeFilter('');
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
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 'bold' }}>Gestion des Permis</Typography>
          <Typography variant="body2" color="text.secondary">
            {permisList.length} permis enregistré(s)
          </Typography>
        </Box>
        {hasRole('CREATE_PERMIS') && (
          <Button
            variant="contained"
            color="success"
            startIcon={<PlusIcon width={20} />}
            onClick={() => navigate('/permis/nouveau')}
            sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
          >
            Nouveau Permis
          </Button>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>{error}</Alert>
      )}

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, borderBottom: '1px solid #E3ECE7' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
            <TextField
              size="small"
              placeholder="Rechercher par numéro ou type..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              sx={{
              flexGrow: 1,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#F7FAF8',
                borderRadius: 2,
                '& fieldset': { borderColor: '#D6E3DC' },
                '&:hover fieldset': { borderColor: '#D6E3DC' },
                '&.Mui-focused fieldset': { borderColor: '#1F4D3E', borderWidth: '1px' },
              }
            }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <MagnifyingGlassIcon style={{ width: 18, height: 18, color: '#5C6E67' }} />
                    </InputAdornment>
                  ),
                }
              }}
            />

            <TextField
              select
              size="small"
              label="Filtrer par Statut"
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              sx={{
              minWidth: 200, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#F7FAF8',
                borderRadius: 2,
                '& fieldset': { borderColor: '#D6E3DC' },
                '&:hover fieldset': { borderColor: '#D6E3DC' },
                '&.Mui-focused fieldset': { borderColor: '#1F4D3E', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Tous les statuts</em></MenuItem>
              {availableStatuses.map(s => (
                <MenuItem key={s} value={s}>{s.replace(/_/g, ' ')}</MenuItem>
              ))}
            </TextField>

            <TextField
              select
              size="small"
              label="Filtrer par Type"
              value={typeFilter}
              onChange={(e) => { setTypeFilter(e.target.value); setPage(0); }}
              sx={{
              minWidth: 200, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#F7FAF8',
                borderRadius: 2,
                '& fieldset': { borderColor: '#D6E3DC' },
                '&:hover fieldset': { borderColor: '#D6E3DC' },
                '&.Mui-focused fieldset': { borderColor: '#1F4D3E', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Tous les types</em></MenuItem>
              {availableTypes.map(t => (
                <MenuItem key={t} value={t}>{t}</MenuItem>
              ))}
            </TextField>

            {hasFilters && (
              <Button
                size="small"
                variant="outlined"
                startIcon={<ArrowPathIcon width={14} />}
                onClick={resetFilters}
                sx={{ borderRadius: 2, borderColor: '#D6E3DC', color: 'text.secondary', whiteSpace: 'nowrap', height: 40 }}
              >
                Réinitialiser
              </Button>
            )}
          </Stack>

          {hasFilters && (
            <Box sx={{ display: 'flex', gap: 1, mt: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
              {search && <Chip label={`Recherche: "${search}"`} size="small" onDelete={() => setSearch('')} sx={{ borderRadius: 1.5 }} />}
              {statusFilter && <Chip label={`Statut: ${statusFilter}`} size="small" onDelete={() => setStatusFilter('')} sx={{ borderRadius: 1.5 }} />}
              {typeFilter && <Chip label={`Type: ${typeFilter}`} size="small" onDelete={() => setTypeFilter('')} sx={{ borderRadius: 1.5 }} />}
              <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center' }}>
                {filtered.length} résultat(s)
              </Typography>
            </Box>
          )}
        </Box>

        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700, fontSize: 12, color: '#5C6E67', bgcolor: '#F7FAF8', textTransform: 'uppercase', letterSpacing: 0.5 } }}>
                <TableCell>Numéro</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Émission</TableCell>
                <TableCell>Expiration</TableCell>
                <TableCell>Statut IA</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucun permis ne correspond à ces critères.' : 'Aucun permis trouvé'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((permis) => (
                  <TableRow key={permis.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    <TableCell sx={{ fontWeight: 600 }}>{permis.numero}</TableCell>
                    <TableCell>{permis.typePermis?.nom}</TableCell>
                    <TableCell>{new Date(permis.dateEmission).toLocaleDateString()}</TableCell>
                    <TableCell>{new Date(permis.dateExpiration).toLocaleDateString()}</TableCell>
                    <TableCell>
                      <Chip
                        label={permis.statutVerification}
                        color={getStatusColor(permis.statutVerification) as any}
                        size="small"
                        sx={{ borderRadius: 1.5, fontWeight: 600, fontSize: 11 }}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        size="small"
                        startIcon={<EyeIcon width={16} />}
                        onClick={() => navigate(`/permis/${permis.id}`)}
                        sx={{ color: '#1F4D3E', textTransform: 'none', fontWeight: 600 }}
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
