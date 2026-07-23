import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton,
  CircularProgress, TextField, InputAdornment, Stack,
  TablePagination, Chip,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon, PencilSquareIcon, TrashIcon,
  MagnifyingGlassIcon, ArrowPathIcon,
} from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface Zone {
  id: string;
  nomZone: string;
  codeZone: string;
  descriptionZone?: string;
}

export default function ZonesListPage() {
  const [zones, setZones] = useState<Zone[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [search, setSearch] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

  useEffect(() => {
    const loadZones = async () => {
      try {
        const res = await AdminService.listZones();
        setZones(Array.isArray(res) ? res : (res as any).content ?? []);
      } catch (err) {
        console.error('Erreur chargement zones', err);
      } finally {
        setLoading(false);
      }
    };
    loadZones();
  }, []);

  const handleDelete = async (id: string) => {
    if (!window.confirm('Confirmer la suppression de cette zone ?')) return;
    try {
      await AdminService.deleteZone(id);
      setZones((prev) => prev.filter((z) => z.id !== id));
    } catch (err) {
      console.error('Erreur suppression zone', err);
    }
  };

  const filtered = useMemo(() => {
    return zones.filter(
      (z) =>
        !search ||
        z.nomZone.toLowerCase().includes(search.toLowerCase()) ||
        z.codeZone.toLowerCase().includes(search.toLowerCase()) ||
        (z.descriptionZone && z.descriptionZone.toLowerCase().includes(search.toLowerCase()))
    );
  }, [zones, search]);

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
            Zones
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {zones.length} zones géographiques enregistrées
          </Typography>
        </Box>
        <Button
          component={Link}
          to="/administration/zones/nouveau"
          variant="contained"
          color="success"
          startIcon={<PlusIcon width={18} />}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
        >
          Nouvelle zone
        </Button>
      </Box>

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <Box sx={{ p: 2.5, borderBottom: '1px solid #f1f5f9' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'center' }}>
            <TextField
              size="small"
              placeholder="Rechercher par nom, code ou description..."
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
                <TableCell>Code</TableCell>
                <TableCell>Nom</TableCell>
                <TableCell>Description</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucune zone ne correspond à cette recherche.' : 'Aucune zone trouvée'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((zone) => (
                  <TableRow key={zone.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'text.secondary', fontFamily: 'monospace' }}>
                        {zone.codeZone}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {zone.nomZone}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {zone.descriptionZone || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        component={Link}
                        to={`/administration/zones/${zone.id}`}
                        size="small"
                        sx={{ color: '#3b82f6', mr: 0.5, '&:hover': { bgcolor: '#eff6ff' } }}
                      >
                        <PencilSquareIcon width={18} />
                      </IconButton>
                      <IconButton
                        size="small"
                        sx={{ color: '#ef4444', '&:hover': { bgcolor: '#fee2e2' } }}
                        onClick={() => handleDelete(zone.id)}
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