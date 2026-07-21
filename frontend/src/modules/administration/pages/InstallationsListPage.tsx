import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton,
  CircularProgress, TextField, InputAdornment, Select, MenuItem,
  FormControl, InputLabel, Stack, TablePagination, Chip,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon, PencilSquareIcon, TrashIcon,
  MagnifyingGlassIcon, FunnelIcon, ArrowPathIcon,
} from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface Installation {
  id: string;
  nomInstallation: string;
  codeInstallation: string;
  atelier?: string;
  localisation?: string;
  service?: { id: string; nomService: string; codeService: string };
}

export default function InstallationsListPage() {
  const [installations, setInstallations] = useState<Installation[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [search, setSearch] = useState('');
  const [serviceFilter, setServiceFilter] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

  useEffect(() => {
    const loadInstallations = async () => {
      try {
        const res = await AdminService.listInstallations();
        setInstallations(res.content || res); // Depending on API response (paginated vs list)
      } catch (err) {
        console.error('Erreur chargement installations', err);
      } finally {
        setLoading(false);
      }
    };
    loadInstallations();
  }, []);

  const handleDelete = async (id: string) => {
    if (!window.confirm('Confirmer la suppression de cette installation ?')) return;
    try {
      await AdminService.deleteInstallation(id);
      setInstallations(prev => prev.filter(i => i.id !== id));
    } catch (err) {
      console.error('Erreur suppression installation', err);
    }
  };

  const allServices = useMemo(() => {
    const set = new Set<string>();
    installations.forEach(i => {
      if (i.service?.nomService) set.add(i.service.nomService);
    });
    return [...set].sort();
  }, [installations]);

  const filtered = useMemo(() => {
    return installations.filter(i => {
      const matchSearch =
        !search ||
        i.nomInstallation.toLowerCase().includes(search.toLowerCase()) ||
        i.codeInstallation.toLowerCase().includes(search.toLowerCase()) ||
        (i.atelier && i.atelier.toLowerCase().includes(search.toLowerCase()));
      const matchService = !serviceFilter || i.service?.nomService === serviceFilter;
      return matchSearch && matchService;
    });
  }, [installations, search, serviceFilter]);

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  const hasFilters = search || serviceFilter;

  const resetFilters = () => {
    setSearch('');
    setServiceFilter('');
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
            Installations
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {installations.length} installations enregistrées
          </Typography>
        </Box>
        <Button
          component={Link}
          to="/administration/installations/nouveau"
          variant="contained"
          color="success"
          startIcon={<PlusIcon width={18} />}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
        >
          Nouvelle installation
        </Button>
      </Box>

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, borderBottom: '1px solid #f1f5f9' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="center">
            <TextField
              size="small"
              placeholder="Rechercher par nom, code, atelier..."
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
              label="Service rattaché"
              value={serviceFilter}
              onChange={(e) => { setServiceFilter(e.target.value); setPage(0); }}
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
              <MenuItem value=""><em>Tous les services</em></MenuItem>
              {allServices.map(s => (
                <MenuItem key={s} value={s}>{s}</MenuItem>
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
              {serviceFilter && <Chip label={`Service: ${serviceFilter}`} size="small" onDelete={() => setServiceFilter('')} sx={{ borderRadius: 1.5 }} />}
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
                <TableCell>Code</TableCell>
                <TableCell>Nom</TableCell>
                <TableCell>Atelier</TableCell>
                <TableCell>Service rattaché</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucune installation ne correspond à ces filtres.' : 'Aucune installation trouvée'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((inst) => (
                  <TableRow key={inst.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.secondary', fontFamily: 'monospace' }}>
                        {inst.codeInstallation}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 500 }}>
                        {inst.nomInstallation}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {inst.atelier || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {inst.service ? `${inst.service.nomService} (${inst.service.codeService})` : '-'}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        component={Link}
                        to={`/administration/installations/${inst.id}`}
                        size="small"
                        sx={{ color: '#3b82f6', mr: 0.5, '&:hover': { bgcolor: '#eff6ff' } }}
                      >
                        <PencilSquareIcon width={18} />
                      </IconButton>
                      <IconButton
                        size="small"
                        sx={{ color: '#ef4444', '&:hover': { bgcolor: '#fee2e2' } }}
                        onClick={() => handleDelete(inst.id)}
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