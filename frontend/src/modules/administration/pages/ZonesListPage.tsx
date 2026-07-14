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
  Chip,
  CircularProgress,
  TextField,
  InputAdornment,
} from '@mui/material';
import { Link } from 'react-router-dom';
import {
  PlusIcon,
  PencilSquareIcon,
  TrashIcon,
  MagnifyingGlassIcon,
} from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface Zone {
  id: string;
  libelle: string;
  description?: string;
  active: boolean;
}

export default function ZonesListPage() {
  const [zones, setZones] = useState<Zone[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const loadZones = async () => {
      try {
        const res = await AdminService.listZones();
        setZones(res.content || res);
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

  const filtered = zones.filter(
    (z) =>
      z.libelle.toLowerCase().includes(search.toLowerCase()) ||
      (z.description && z.description.toLowerCase().includes(search.toLowerCase()))
  );

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
            Gestion des zones géographiques
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
        <Box sx={{ p: 2, pb: 0 }}>
          <TextField
            size="small"
            placeholder="Rechercher une zone..."
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
                <TableCell sx={{ fontWeight: 600 }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Aucune zone trouvée
                  </TableCell>
                </TableRow>
              )}
              {filtered.map((zone) => (
                <TableRow key={zone.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {zone.libelle}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {zone.description || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={zone.active ? 'Actif' : 'Inactif'}
                      size="small"
                      color={zone.active ? 'success' : 'default'}
                      sx={{ borderRadius: 1.5, fontWeight: 500 }}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      component={Link}
                      to={`/administration/zones/${zone.id}`}
                      size="small"
                      sx={{ color: 'primary.main', mr: 0.5 }}
                    >
                      <PencilSquareIcon width={18} />
                    </IconButton>
                    <IconButton
                      size="small"
                      sx={{ color: 'error.main' }}
                      onClick={() => handleDelete(zone.id)}
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