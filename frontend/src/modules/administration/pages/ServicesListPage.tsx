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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon,
  PencilSquareIcon,
  TrashIcon,
  MagnifyingGlassIcon,
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
  const [search, setSearch] = useState('');
  const [selectedZone, setSelectedZone] = useState<string>('all');
  const [error, setError] = useState('');

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

  const filtered = services.filter((s) => {
    const nom = (s.nomService || '').toLowerCase();
    const code = (s.codeService || '').toLowerCase();
    const q = search.toLowerCase();
    const matchesSearch = nom.includes(q) || code.includes(q);
    const matchesZone = selectedZone === 'all' || s.zone?.id === selectedZone;
    return matchesSearch && matchesZone;
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
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
            Services OCP
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gestion des services internes OCP et de leurs zones d'affectation
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
        <Box sx={{ p: 2, pb: 0, display: 'flex', gap: 2 }}>
          <TextField
            size="small"
            placeholder="Rechercher un service..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            sx={{ width: 320 }}
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
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Filtrer par Zone</InputLabel>
            <Select
              label="Filtrer par Zone"
              value={selectedZone}
              onChange={(e) => setSelectedZone(e.target.value)}
            >
              <MenuItem value="all">Toutes les zones</MenuItem>
              {zones.map(z => (
                <MenuItem key={z.id} value={z.id}>{z.nomZone} ({z.codeZone})</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
        <Box sx={{ mt: 2 }}>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>Code</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Service</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Zone</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Description</TableCell>
                  <TableCell sx={{ fontWeight: 600 }} align="right">
                    Actions
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filtered.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                      Aucun service trouvé
                    </TableCell>
                  </TableRow>
                )}
                {filtered.map((service) => (
                  <TableRow key={service.id} hover>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'primary.main' }}>
                        {service.codeService}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 500 }}>
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
                        sx={{ color: 'primary.main', mr: 0.5 }}
                      >
                        <PencilSquareIcon width={18} />
                      </IconButton>
                      <IconButton
                        size="small"
                        sx={{ color: 'error.main' }}
                        onClick={() => handleDelete(service.id)}
                      >
                        <TrashIcon width={18} />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </Paper>
    </Box>
  );
}
