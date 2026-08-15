import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton,
  CircularProgress, TextField, InputAdornment, Alert,
  FormControl, InputLabel, Select, MenuItem, Stack, Chip,
  TablePagination,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon, PencilSquareIcon, TrashIcon,
  MagnifyingGlassIcon, FunnelIcon, ArrowPathIcon,
} from '@heroicons/react/24/outline';
import { apiClient } from '../../../services/apiClient';

interface Zone {
  id: string;
  nomZone: string;
  codeZone: string;
}

interface ServiceItem {
  id: string;
  nomService: string;
  codeService: string;
  descriptionService?: string;
  zone?: Zone;
}

export default function ServicesListPage() {
  const [services, setServices] = useState<ServiceItem[]>([]);
  const [zones, setZones] = useState<Zone[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filters
  const [search, setSearch] = useState('');
  const [selectedZone, setSelectedZone] = useState<string>('all');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

  const loadData = async () => {
    try {
      setLoading(true);
      const [servicesRes, zonesRes] = await Promise.all([
        apiClient.get('/services?size=500'),
        apiClient.get('/zones?size=500')
      ]);
      setServices(servicesRes.data.content ?? servicesRes.data ?? []);
      setZones(zonesRes.data.content ?? zonesRes.data ?? []);
    } catch (err) {
      console.error('Erreur chargement services', err);
      setError('Impossible de charger les données');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const handleDelete = async (id: string) => {
    if (!window.confirm('Confirmer la suppression de ce service ?')) return;
    try {
      await apiClient.delete(`/services/${id}`);
      setServices((prev) => prev.filter((s) => s.id !== id));
    } catch {
      setError('Erreur lors de la suppression');
    }
  };

  const filtered = useMemo(() => {
    return services.filter((s) => {
      const q = search.toLowerCase();
      const matchesSearch = !search ||
        (s.nomService || '').toLowerCase().includes(q) ||
        (s.codeService || '').toLowerCase().includes(q) ||
        (s.descriptionService || '').toLowerCase().includes(q);
      const matchesZone = selectedZone === 'all' || s.zone?.id === selectedZone;
      return matchesSearch && matchesZone;
    });
  }, [services, search, selectedZone]);

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  const hasFilters = search || selectedZone !== 'all';

  const resetFilters = () => {
    setSearch('');
    setSelectedZone('all');
    setPage(0);
  };

  const selectedZoneObj = zones.find(z => z.id === selectedZone);

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
            Services OCP
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {services.length} services enregistrés
          </Typography>
        </Box>
        <Button
          component={Link}
          to="/administration/services/nouveau"
          variant="contained"
          color="success"
          startIcon={<PlusIcon width={18} />}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
        >
          Nouveau service
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, borderBottom: '1px solid #E3ECE7' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
            <TextField
              size="small"
              placeholder="Rechercher par nom, code ou description..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              sx={{
              flex: '2 1 300px', maxWidth: 400,
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
              label="Filtrer par Zone"
              value={selectedZone}
              onChange={(e) => { setSelectedZone(e.target.value); setPage(0); }}
              sx={{
              minWidth: 220, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#F7FAF8',
                borderRadius: 2,
                '& fieldset': { borderColor: '#D6E3DC' },
                '&:hover fieldset': { borderColor: '#D6E3DC' },
                '&.Mui-focused fieldset': { borderColor: '#1F4D3E', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value="all"><em>Toutes les zones</em></MenuItem>
              {zones.map(z => (
                <MenuItem key={z.id} value={z.id}>{z.nomZone} ({z.codeZone})</MenuItem>
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
              {selectedZone !== 'all' && selectedZoneObj && <Chip label={`Zone: ${selectedZoneObj.nomZone}`} size="small" onDelete={() => setSelectedZone('all')} sx={{ borderRadius: 1.5 }} />}
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
                <TableCell>Code</TableCell>
                <TableCell>Service</TableCell>
                <TableCell>Zone</TableCell>
                <TableCell>Description</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucun service ne correspond à ces filtres.' : 'Aucun service trouvé'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((service) => (
                  <TableRow key={service.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.secondary', fontFamily: 'monospace' }}>
                        {service.codeService}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {service.nomService}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {service.zone ? `${service.zone.nomZone}` : '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {service.descriptionService || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        component={Link}
                        to={`/administration/services/${service.id}`}
                        size="small"
                        sx={{ color: '#1F4D3E', mr: 0.5, '&:hover': { bgcolor: '#EDF2EE' } }}
                      >
                        <PencilSquareIcon width={18} />
                      </IconButton>
                      <IconButton
                        size="small"
                        sx={{ color: '#9A3D2F', '&:hover': { bgcolor: '#FBEAE3' } }}
                        onClick={() => handleDelete(service.id)}
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
