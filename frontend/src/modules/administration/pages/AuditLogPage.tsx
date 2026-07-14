import { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  CircularProgress,
  TextField,
  InputAdornment,
  TablePagination,
} from '@mui/material';
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';
import type { AuditLogEntryFlat } from '../../../services/AdminService';


const ACTION_COLORS: Record<string, 'success' | 'error' | 'info' | 'warning' | 'default'> = {
  CREATE: 'success',
  UPDATE: 'info',
  DELETE: 'error',
  LOGIN: 'primary' as any,
  LOGOUT: 'default',
};

export default function AuditLogPage() {
  const [entries, setEntries] = useState<AuditLogEntryFlat[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);

  useEffect(() => {
    AdminService.listAuditLogs()
      .then(setEntries)
      .catch((err) => console.error('Erreur chargement audit', err))
      .finally(() => setLoading(false));
  }, []);

  const filtered = entries.filter(
    (e) =>
      e.action.toLowerCase().includes(search.toLowerCase()) ||
      e.entity.toLowerCase().includes(search.toLowerCase()) ||
      e.utilisateur.toLowerCase().includes(search.toLowerCase()) ||
      e.details.toLowerCase().includes(search.toLowerCase())
  );

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          Journal d'activité
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Historique des actions effectuées sur le système
        </Typography>
      </Box>

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        <Box sx={{ p: 2, pb: 0 }}>
          <TextField
            size="small"
            placeholder="Rechercher dans le journal..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            sx={{ width: 360, mb: 2 }}
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
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Date</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Action</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Entité</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Utilisateur</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Détails</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    Aucune entrée trouvée
                  </TableCell>
                </TableRow>
              )}
              {paginated.map((entry) => (
                <TableRow key={entry.id} hover>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                    <Typography variant="body2" color="text.secondary">
                      {new Date(entry.dateCreation).toLocaleString('fr-FR')}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={entry.action}
                      size="small"
                      color={ACTION_COLORS[entry.action] || 'default'}
                      variant="outlined"
                      sx={{ borderRadius: 1.5, fontWeight: 500, fontSize: 11 }}
                    />
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {entry.entity}
                      {entry.entityId ? ` #${entry.entityId}` : ''}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary">
                      {entry.utilisateur}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 300 }} noWrap>
                      {entry.details}
                    </Typography>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={filtered.length}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          labelRowsPerPage="Lignes par page"
        />
      </Paper>
    </Box>
  );
}