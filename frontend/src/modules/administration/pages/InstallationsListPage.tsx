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

interface Installation {
  id: string;
  libelle: string;
  description?: string;
  zoneId?: string;
  zoneLibelle?: string;
  active: boolean;
}

export default function InstallationsListPage() {
  const [installations, setInstallations] = useState<Installation[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const loadInstallations = async () => {
      try {
        const res = await AdminService.listInstallations();
        setInstallations(res.content || res);
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

  const filtered = installations.filter(i =>
    i.libelle.toLowerCase().includes(search.toLowerCase()) ||
    (i.description && i.description.toLowerCase().includes(search.toLowerCase()))
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
            Installations
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gestion des installations industrielles
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
        <Box sx={{ p: 2, pb: 0 }}>
          <TextField
            size="small"
            placeholder="Rechercher une installation..."
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
                <TableCell sx={{ fontWeight: 600 }}>Zone</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Aucune installation trouvée
                  </TableCell>
                </TableRow>
              )}
              {filtered.map((inst) => (
                <TableRow key={inst.id} hover>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {inst.libelle}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {inst.description || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {inst.zoneLibelle || '-'}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={inst.active ? 'Actif' : 'Inactif'}
                      size="small"
                      color={inst.active ? 'success' : 'default'}
                      sx={{ borderRadius: 1.5, fontWeight: 500 }}
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton
                      component={Link}
                      to={`/administration/installations/${inst.id}`}
                      size="small"
                      sx={{ color: 'primary.main', mr: 0.5 }}
                    >
                      <PencilSquareIcon width={18} />
                    </IconButton>
                    <IconButton
                      size="small"
                      sx={{ color: 'error.main' }}
                      onClick={() => handleDelete(inst.id)}
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