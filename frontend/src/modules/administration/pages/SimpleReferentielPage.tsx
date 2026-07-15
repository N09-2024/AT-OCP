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
  CircularProgress,
  TextField,
  InputAdornment,
  Alert,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon,
  PencilSquareIcon,
  TrashIcon,
  MagnifyingGlassIcon,
} from '@heroicons/react/24/outline';
import { apiClient } from '../../../services/apiClient';

interface SimpleItem {
  id: string;
  nom?: string;
  libelle?: string;
  description?: string;
  [key: string]: any;
}

interface SimpleReferentielPageProps {
  title: string;
  subtitle: string;
  apiPath: string;          // e.g. '/epis'
  routeBase: string;        // e.g. '/administration/epis'
  labelField?: string;      // which field to use as label (any backend field name)
  createLabel: string;      // e.g. 'Nouvel EPI'
  searchPlaceholder: string;
}

export default function SimpleReferentielPage({
  title,
  subtitle,
  apiPath,
  routeBase,
  labelField = 'nom',
  createLabel,
  searchPlaceholder,
}: SimpleReferentielPageProps) {
  const [items, setItems] = useState<SimpleItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      setLoading(true);
      const res = await apiClient.get(`${apiPath}?size=200`);
      const data = res.data;
      // Handle both array and paginated responses
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

  const filtered = items.filter((item) => {
    const label = getLabel(item)?.toLowerCase() ?? '';
    const desc = (item.description ?? '').toLowerCase();
    const q = search.toLowerCase();
    return label.includes(q) || desc.includes(q);
  });

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
            {title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {subtitle}
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
        <Box sx={{ p: 2, pb: 0 }}>
          <TextField
            size="small"
            placeholder={searchPlaceholder}
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
                <TableCell sx={{ fontWeight: 600 }}>Libellé</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Description</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Aucun élément trouvé
                  </TableCell>
                </TableRow>
              )}
              {filtered.map((item) => (
                <TableRow key={item.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {getLabel(item)}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {item.description || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      component={Link}
                      to={`${routeBase}/${item.id}`}
                      size="small"
                      sx={{ color: 'primary.main', mr: 0.5 }}
                    >
                      <PencilSquareIcon width={18} />
                    </IconButton>
                    <IconButton
                      size="small"
                      sx={{ color: 'error.main' }}
                      onClick={() => handleDelete(item.id)}
                    >
                      <TrashIcon width={18} />
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
