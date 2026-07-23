import { useEffect, useState, useMemo } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, CircularProgress,
  TextField, InputAdornment, TablePagination,
  Select, MenuItem, FormControl, InputLabel, Stack, Button,
} from '@mui/material';
import { MagnifyingGlassIcon, FunnelIcon, ArrowPathIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';
import type { AuditLogEntryFlat } from '../../../services/AdminService';

const ACTION_STYLES: Record<string, { label: string; bg: string; color: string }> = {
  CREATE:  { label: 'Création',      bg: '#f0fdf4', color: '#16a34a' },
  UPDATE:  { label: 'Modification',  bg: '#eff6ff', color: '#2563eb' },
  DELETE:  { label: 'Suppression',   bg: '#fef2f2', color: '#dc2626' },
  LOGIN:   { label: 'Connexion',     bg: '#f5f3ff', color: '#7c3aed' },
  LOGOUT:  { label: 'Déconnexion',   bg: '#f8fafc', color: '#64748b' },
  APPROVE: { label: 'Approbation',   bg: '#f0fdf4', color: '#16a34a' },
  REJECT:  { label: 'Rejet',         bg: '#fef2f2', color: '#dc2626' },
};

function getActionStyle(action: string) {
  const key = action?.toUpperCase();
  return ACTION_STYLES[key] ?? { label: action, bg: '#f8fafc', color: '#64748b' };
}

export default function AuditLogPage() {
  const [entries, setEntries] = useState<AuditLogEntryFlat[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [search, setSearch] = useState('');
  const [actionFilter, setActionFilter] = useState('');
  const [userFilter, setUserFilter] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');

  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);

  useEffect(() => {
    AdminService.listAuditLogs()
      .then(setEntries)
      .catch((err) => console.error('Erreur chargement audit', err))
      .finally(() => setLoading(false));
  }, []);

  // All unique actions for dropdown
  const allActions = useMemo(() => {
    return [...new Set(entries.map(e => e.action.toUpperCase()))].sort();
  }, [entries]);

  // All unique users for dropdown
  const allUsers = useMemo(() => {
    return [...new Set(entries.map(e => e.utilisateur).filter(Boolean))].sort();
  }, [entries]);

  const filtered = useMemo(() => {
    return entries.filter((e) => {
      const matchSearch =
        !search ||
        e.action.toLowerCase().includes(search.toLowerCase()) ||
        e.entity.toLowerCase().includes(search.toLowerCase()) ||
        e.utilisateur.toLowerCase().includes(search.toLowerCase()) ||
        e.details.toLowerCase().includes(search.toLowerCase());
      const matchAction = !actionFilter || e.action.toUpperCase() === actionFilter;
      const matchUser = !userFilter || e.utilisateur === userFilter;
      const entryDate = new Date(e.dateCreation);
      const matchFrom = !dateFrom || entryDate >= new Date(dateFrom);
      const matchTo = !dateTo || entryDate <= new Date(dateTo + 'T23:59:59');
      return matchSearch && matchAction && matchUser && matchFrom && matchTo;
    });
  }, [entries, search, actionFilter, userFilter, dateFrom, dateTo]);

  const paginated = filtered.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const hasFilters = search || actionFilter || userFilter || dateFrom || dateTo;

  const resetFilters = () => {
    setSearch('');
    setActionFilter('');
    setUserFilter('');
    setDateFrom('');
    setDateTo('');
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
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          Journal d'activité
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {entries.length} entrées · Historique des actions effectuées sur le système
        </Typography>
      </Box>

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
        {/* Filters */}
        <Box sx={{ p: 2.5, borderBottom: '1px solid #f1f5f9' }}>
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
            {/* Text search */}
            <TextField
              size="small"
              placeholder="Rechercher dans le journal..."
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              sx={{
              flex: '2 1 260px',
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

            {/* Action filter */}
            <TextField
              select
              size="small"
              label="Action"
              value={actionFilter}
              onChange={(e) => { setActionFilter(e.target.value); setPage(0); }}
              sx={{
              flex: '1 1 150px',
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Toutes</em></MenuItem>
              {allActions.map(a => (
                <MenuItem key={a} value={a}>{a}</MenuItem>
              ))}
            </TextField>

            {/* User filter */}
            <TextField
              select
              size="small"
              label="Utilisateur"
              value={userFilter}
              onChange={(e) => { setUserFilter(e.target.value); setPage(0); }}
              sx={{
              minWidth: 200, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Tous les utilisateurs</em></MenuItem>
              {allUsers.map(u => (
                <MenuItem key={u} value={u}>{u}</MenuItem>
              ))}
            </TextField>

            {/* Date range */}
            <TextField
              size="small"
              type="date"
              label="Du"
              value={dateFrom}
              onChange={(e) => { setDateFrom(e.target.value); setPage(0); }}
              sx={{
              flex: '1 1 140px',
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              size="small"
              type="date"
              label="Au"
              value={dateTo}
              onChange={(e) => { setDateTo(e.target.value); setPage(0); }}
              sx={{
              flex: '1 1 140px',
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
              slotProps={{ inputLabel: { shrink: true } }}
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

          {/* Active filter chips + result count */}
          {hasFilters && (
            <Box sx={{ display: 'flex', gap: 1, mt: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
              {search && <Chip label={`"${search}"`} size="small" onDelete={() => setSearch('')} sx={{ borderRadius: 1.5 }} />}
              {actionFilter && <Chip label={`Action: ${getActionStyle(actionFilter).label}`} size="small" onDelete={() => setActionFilter('')} sx={{ borderRadius: 1.5 }} />}
              {userFilter && <Chip label={`Par: ${userFilter}`} size="small" onDelete={() => setUserFilter('')} sx={{ borderRadius: 1.5 }} />}
              {dateFrom && <Chip label={`Depuis: ${new Date(dateFrom).toLocaleDateString('fr-FR')}`} size="small" onDelete={() => setDateFrom('')} sx={{ borderRadius: 1.5 }} />}
              {dateTo && <Chip label={`Jusqu'au: ${new Date(dateTo).toLocaleDateString('fr-FR')}`} size="small" onDelete={() => setDateTo('')} sx={{ borderRadius: 1.5 }} />}
              <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center' }}>
                {filtered.length} entrée(s)
              </Typography>
            </Box>
          )}
        </Box>

        {/* Table */}
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700, fontSize: 12, color: '#64748b', bgcolor: '#f8fafc', textTransform: 'uppercase', letterSpacing: 0.5 } }}>
                <TableCell>Date & heure</TableCell>
                <TableCell>Action</TableCell>
                <TableCell>Entité</TableCell>
                <TableCell>Utilisateur</TableCell>
                <TableCell>Détails</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {paginated.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    {hasFilters ? 'Aucune entrée ne correspond à ces filtres.' : 'Aucune entrée trouvée'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((entry) => {
                  const style = getActionStyle(entry.action);
                  return (
                    <TableRow key={entry.id} hover sx={{ '&:last-child td': { border: 0 } }}>
                      <TableCell sx={{ whiteSpace: 'nowrap' }}>
                        <Typography variant="body2" color="text.secondary" sx={{ fontSize: 12 }}>
                          {new Date(entry.dateCreation).toLocaleString('fr-FR')}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={style.label}
                          size="small"
                          sx={{ bgcolor: style.bg, color: style.color, fontWeight: 700, fontSize: 11, borderRadius: 1.5 }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 500 }}>
                          {entry.entity}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">{entry.utilisateur}</Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 300 }} noWrap>
                          {entry.details}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  );
                })
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
          rowsPerPageOptions={[10, 25, 50, 100]}
          labelRowsPerPage="Lignes par page"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} sur ${count}`}
        />
      </Paper>
    </Box>
  );
}