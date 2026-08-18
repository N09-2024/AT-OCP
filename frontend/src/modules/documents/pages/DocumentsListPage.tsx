import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  CircularProgress,
  ToggleButtonGroup,
  ToggleButton,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import { documentInterventionApi } from '../../../services/documentInterventionApi';

export default function DocumentsListPage() {
  const navigate = useNavigate();
  const [docType, setDocType] = useState<'DI' | 'OT' | 'BT'>('DI');
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState<any[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      if (docType === 'DI') {
        const res = await documentInterventionApi.getDemandesIntervention();
        setItems(res.content || []);
      } else if (docType === 'OT') {
        const res = await documentInterventionApi.getOrdresTravail();
        setItems(res.content || []);
      } else {
        const res = await documentInterventionApi.getBonsTravail();
        setItems(res.content || []);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [docType]);

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
            Documents d'Intervention
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Demandes d'Intervention (DI), Ordres de Travail (OT), Bons de Travail (BT)
          </Typography>
        </Box>

        <ToggleButtonGroup
          value={docType}
          exclusive
          onChange={(_, v) => v && setDocType(v)}
          size="small"
        >
          <ToggleButton value="DI" sx={{ px: 3, fontWeight: 700 }}>Demandes (DI)</ToggleButton>
          <ToggleButton value="OT" sx={{ px: 3, fontWeight: 700 }}>Ordres (OT)</ToggleButton>
          <ToggleButton value="BT" sx={{ px: 3, fontWeight: 700 }}>Bons (BT)</ToggleButton>
        </ToggleButtonGroup>
      </Box>

      <TableContainer component={Paper} sx={{ borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: 'none' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
            <CircularProgress color="success" />
          </Box>
        ) : (
          <Table>
            <TableHead sx={{ bgcolor: '#F7FAF8' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>N° Document</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Objet</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Date Création</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 6 }}>
                    <Typography variant="body1" color="text.secondary">
                      Aucun document trouvé pour ce type.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                items.map((row) => (
                  <TableRow key={row.id} hover>
                    <TableCell sx={{ fontWeight: 700, color: '#1F4D3E' }}>
                      {row.numero}
                    </TableCell>
                    <TableCell sx={{ fontWeight: 600 }}>{row.objet}</TableCell>
                    <TableCell>
                      <Chip label={row.statut || 'N/A'} size="small" color="primary" variant="outlined" />
                    </TableCell>
                    <TableCell>
                      {row.dateCreation ? new Date(row.dateCreation).toLocaleDateString('fr-FR') : '-'}
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        variant="contained"
                        size="small"
                        startIcon={<AddIcon />}
                        onClick={() => navigate('/autorisations/nouvelle')}
                        sx={{ bgcolor: '#1F4D3E', '&:hover': { bgcolor: '#2E624A' }, fontWeight: 700 }}
                      >
                        Créer AT
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}
      </TableContainer>
    </Box>
  );
}
