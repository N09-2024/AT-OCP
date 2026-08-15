import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton,
  CircularProgress, TextField, InputAdornment, Alert,
  Stack, Chip, TablePagination, FormControl, InputLabel, Select, MenuItem,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon, PencilSquareIcon, TrashIcon,
  MagnifyingGlassIcon, ArrowPathIcon,
} from '@heroicons/react/24/outline';
import { apiClient } from '../../../services/apiClient';

interface SimpleItem {
  id: string;
  nom?: string;
  libelle?: string;
  description?: string;
  [key: string]: any;
}

interface FieldConfig {
  key: string;
  label: string;
  required?: boolean;
  multiline?: boolean;
  rows?: number;
}

interface SimpleReferentielPageProps {
  title: string;
  subtitle: string;
  apiPath: string;          // e.g. '/epis'
  routeBase: string;        // e.g. '/administration/epis'
  labelField?: string;      // which field to use as label (any backend field name)
  createLabel: string;      // e.g. 'Nouvel EPI'
  searchPlaceholder: string;
  fields?: FieldConfig[];   // dynamic fields to display in table
}

export default function SimpleReferentielPage({
  title,
  subtitle,
  apiPath,
  routeBase,
  labelField = 'nom',
  createLabel,
  searchPlaceholder,
  fields = [],
}: SimpleReferentielPageProps) {
  const [items, setItems] = useState<SimpleItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filters & Sorting
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState<'asc' | 'desc' | ''>('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(15);

  const load = async () => {
    try {
      setLoading(true);
      const res = await apiClient.get(`${apiPath}?size=500`);
      const data = res.data;
      setItems(Array.isArray(data) ? data : (data.content ?? []));
    } catch (err) {
      console.error(`Erreur chargement ${title}`, err);
      setError(`Impossible de charger les données`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [apiPath]);

  const handleDelete = async (id: string) => {
    if (!window.confirm(`Confirmer la suppression ?`)) return;
    try {
      await apiClient.delete(`${apiPath}/${id}`);
      setItems((prev) => prev.filter((i) => i.id !== id));
    } catch {
      setError('Erreur lors de la suppression');
    }
  };

  const getLabel = (item: SimpleItem) =>
    item[labelField] ?? item.nom ?? item.libelle ?? item.id;

  const filteredAndSorted = useMemo(() => {
    // 1. Filter
    let result = items.filter((item) => {
      const label = getLabel(item)?.toLowerCase() ?? '';
      
      // Search in all dynamic fields
      const fieldValues = fields.map(f => (item[f.key] ?? '').toString().toLowerCase()).join(' ');
      const q = search.toLowerCase();
      
      return !search || label.includes(q) || fieldValues.includes(q);
    });

    // 2. Sort
    if (sortBy === 'asc') {
      result = result.sort((a, b) => getLabel(a).localeCompare(getLabel(b)));
    } else if (sortBy === 'desc') {
      result = result.sort((a, b) => getLabel(b).localeCompare(getLabel(a)));
    }

    return result;
  }, [items, search, sortBy, labelField, fields]);

  const paginated = filteredAndSorted.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  const hasFilters = search.length > 0 || sortBy !== '';

  const resetFilters = () => {
    setSearch('');
    setSortBy('');
    setPage(0);
  };

  // If fields are provided, use them as columns. Otherwise default to Libellé + Description
  const tableColumns = fields.length > 0 ? fields : [
    { key: labelField, label: 'Libellé' },
    { key: 'description', label: 'Description' }
  ];

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
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {subtitle} • {filteredAndSorted.length} élément(s)
          </Typography>
        </Box>
        <Button
          component={Link}
          to={`${routeBase}/nouveau`}
          variant="contained"
          color="success"
          startIcon={<PlusIcon width={18} />}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
        >
          {createLabel}
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
              placeholder={searchPlaceholder || "Rechercher..."}
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
              label="Trier par"
              value={sortBy}
              onChange={(e) => { setSortBy(e.target.value as 'asc' | 'desc' | ''); setPage(0); }}
              sx={{
                minWidth: 220,
                flexShrink: 0,
                '& .MuiOutlinedInput-root': {
                  bgcolor: '#F7FAF8',
                  borderRadius: 2,
                  '& fieldset': { borderColor: '#D6E3DC' },
                  '&:hover fieldset': { borderColor: '#D6E3DC' },
                  '&.Mui-focused fieldset': { borderColor: '#1F4D3E', borderWidth: '1px' },
                }
              }}
            >
              <MenuItem value=""><em>Par défaut</em></MenuItem>
              <MenuItem value="asc">Nom (A-Z)</MenuItem>
              <MenuItem value="desc">Nom (Z-A)</MenuItem>
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
              {sortBy === 'asc' && <Chip label={`Tri: A-Z`} size="small" onDelete={() => setSortBy('')} sx={{ borderRadius: 1.5 }} />}
              {sortBy === 'desc' && <Chip label={`Tri: Z-A`} size="small" onDelete={() => setSortBy('')} sx={{ borderRadius: 1.5 }} />}
              <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center' }}>
                {filteredAndSorted.length} résultat(s)
              </Typography>
            </Box>
          )}
        </Box>

        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700, fontSize: 12, color: '#5C6E67', bgcolor: '#F7FAF8', textTransform: 'uppercase', letterSpacing: 0.5 } }}>
                {tableColumns.map((col, index) => (
                   <TableCell key={index}>{col.label}</TableCell>
                ))}
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={tableColumns.length + 1} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucun élément ne correspond à ces critères.' : 'Aucun élément trouvé'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((item) => (
                  <TableRow key={item.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                    {tableColumns.map((col, index) => (
                      <TableCell key={index}>
                        {index === 0 ? (
                           <Typography variant="body2" sx={{ fontWeight: 600 }}>
                             {item[col.key] || '-'}
                           </Typography>
                        ) : (
                           <Typography variant="body2" color="text.secondary">
                             {item[col.key] || '-'}
                           </Typography>
                        )}
                      </TableCell>
                    ))}
                    <TableCell align="right">
                      <IconButton
                        component={Link}
                        to={`${routeBase}/${item.id}`}
                        size="small"
                        sx={{ color: '#1F4D3E', mr: 0.5, '&:hover': { bgcolor: '#EDF2EE' } }}
                      >
                        <PencilSquareIcon width={18} />
                      </IconButton>
                      <IconButton
                        size="small"
                        sx={{ color: '#9A3D2F', '&:hover': { bgcolor: '#FBEAE3' } }}
                        onClick={() => handleDelete(item.id)}
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
          count={filteredAndSorted.length}
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
