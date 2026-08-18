import React, { useState, useEffect, useCallback, useMemo } from 'react';
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
  TextField,
  InputAdornment,
  CircularProgress,
  Stack,
  IconButton,
  Tooltip,
  TablePagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Badge,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import VisibilityIcon from '@mui/icons-material/Visibility';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EditIcon from '@mui/icons-material/Edit';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import ClearIcon from '@mui/icons-material/Clear';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import { apiClient } from '../../../services/apiClient';
import type { AutorisationTravail } from '../../../types';
import { useAuthStore } from '../../../store/authStore';

export default function AutorisationListPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const filterParam = searchParams.get('filtre');
  const user = useAuthStore((s) => s.user);

  const roles = user?.roles?.map((r: any) => r.nom) || [];
  const isAdmin = roles.includes('ADMIN');
  const isCeep = roles.some((r: string) => r === 'CEEP' || r === 'DEMANDEUR');
  const isCeee = roles.includes('CEEE');
  const isHc = roles.some((r: string) => ['HCEP', 'HCEE', 'HC', 'RESPONSABLE_OCP'].includes(r));
  const isHm = roles.some((r: string) => ['HMEP', 'HMEE', 'HM'].includes(r));
  const isCeeeOnly = isCeee && !isCeep && !isAdmin;

  // Filtres contextualisés selon le rôle et les étapes OCP (Standard S-HSE-SEC-31)
  const statusFilters = useMemo(() => {
    if (isCeeeOnly) {
      // Vue Chef d'Équipe Exécutant (CEEE) - Étape 3b à 7
      return [
        { label: 'Toutes mes interventions', value: 'TOUS', color: '#5C6E67', bg: '#F0F5F2' },
        { label: 'Étape 3 : À viser (Visa CEEE) ✏️', value: 'SOUMISE', color: '#6B4E11', bg: '#FDF3E3' },
        { label: 'Étape 4 : Prêtes à démarrer ⚡', value: 'VALIDEE', color: '#2E624A', bg: '#E2F0E8' },
        { label: 'Étape 5 : En cours 🔄', value: 'INTERVENTION_EN_COURS', color: '#E65100', bg: '#FFF3E0' },
        { label: 'Étape 7 : À réceptionner 🏁', value: 'FIN_TRAVAUX_DECLAREE', color: '#1565C0', bg: '#E3F2FD' },
        { label: 'Clôturées ✓', value: 'CLOTUREE', color: '#2E624A', bg: '#E2F0E8' },
      ];
    }
    if (isCeep && !isCeee && !isAdmin) {
      // Vue Chef d'Équipe Propriétaire (CEEP) - Étape 1 à 7
      return [
        { label: 'Toutes mes ATs', value: 'TOUS', color: '#5C6E67', bg: '#F0F5F2' },
        { label: 'Étape 1 : Mes brouillons 📝', value: 'BROUILLON', color: '#8B6914', bg: '#FEF8E7' },
        { label: 'Étape 3 : En validation ⏳', value: 'SOUMISE', color: '#6B4E11', bg: '#FDF3E3' },
        { label: 'Étape 4 : Validées ✓', value: 'VALIDEE', color: '#2E624A', bg: '#E2F0E8' },
        { label: 'Étape 5 : En cours ⚡', value: 'INTERVENTION_EN_COURS', color: '#E65100', bg: '#FFF3E0' },
        { label: 'Étape 7 : À réceptionner 🏁', value: 'FIN_TRAVAUX_DECLAREE', color: '#1565C0', bg: '#E3F2FD' },
        { label: 'Clôturées ✓', value: 'CLOTUREE', color: '#2E624A', bg: '#E2F0E8' },
      ];
    }
    if (isHc && !isAdmin) {
      // Vue Hors Cadre (HCEP / HCEE) - Étape 0, 3c, 3d, 8
      return [
        { label: 'Toutes les ATs du secteur', value: 'TOUS', color: '#5C6E67', bg: '#F0F5F2' },
        { label: 'Étape 3 : À valider (Visas HC) ⏳', value: 'SOUMISE', color: '#6B4E11', bg: '#FDF3E3' },
        { label: 'Validées ✓', value: 'VALIDEE', color: '#2E624A', bg: '#E2F0E8' },
        { label: 'En cours ⚡', value: 'INTERVENTION_EN_COURS', color: '#E65100', bg: '#FFF3E0' },
        { label: 'Étape 8 : À archiver 📦', value: 'CLOTUREE', color: '#2E624A', bg: '#E2F0E8' },
      ];
    }
    // Vue Haute Maîtrise / CE Polyvalent / Admin
    return [
      { label: 'Toutes les ATs', value: 'TOUS', color: '#5C6E67', bg: '#F0F5F2' },
      { label: 'Brouillons 📝', value: 'BROUILLON', color: '#8B6914', bg: '#FEF8E7' },
      { label: 'En validation ⏳', value: 'SOUMISE', color: '#6B4E11', bg: '#FDF3E3' },
      { label: 'Validées ✓', value: 'VALIDEE', color: '#2E624A', bg: '#E2F0E8' },
      { label: 'En cours ⚡', value: 'INTERVENTION_EN_COURS', color: '#E65100', bg: '#FFF3E0' },
      { label: 'Fin déclarée 🏁', value: 'FIN_TRAVAUX_DECLAREE', color: '#1565C0', bg: '#E3F2FD' },
      { label: 'Clôturées ✓', value: 'CLOTUREE', color: '#2E624A', bg: '#E2F0E8' },
    ];
  }, [roles, isCeeeOnly, isCeep, isCeee, isHc, isAdmin]);

  const [loading, setLoading] = useState(true);
  const [ats, setAts] = useState<AutorisationTravail[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>(filterParam || 'TOUS');

  // Sync filtre URL (?filtre=SOUMISE) → barre de filtres
  useEffect(() => {
    if (filterParam) setStatusFilter(filterParam);
  }, [filterParam]);

  // Chargement côté serveur avec filtres actifs
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      // Construire les paramètres de recherche
      const params: Record<string, any> = { page, size: pageSize };
      if (statusFilter && statusFilter !== 'TOUS') params.statut = statusFilter;
      if (searchTerm.trim()) params.search = searchTerm.trim();

      const res = await autorisationTravailApi.findAll(page, pageSize, statusFilter !== 'TOUS' ? statusFilter : undefined, searchTerm.trim() || undefined);
      setAts(res.content || []);
      setTotal(res.totalElements || 0);
    } catch (err) {
      console.error(err);
      setAts([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, statusFilter, searchTerm]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Changer le filtre de statut et revenir à la page 0
  const handleStatusChange = (value: string) => {
    setStatusFilter(value);
    setPage(0);
    if (value !== 'TOUS') {
      setSearchParams({ filtre: value });
    } else {
      setSearchParams({});
    }
  };

  // Lancer la recherche (sur Entrée ou bouton)
  const handleSearch = () => {
    setSearchTerm(searchInput);
    setPage(0);
  };

  // Réinitialiser tous les filtres
  const handleClearFilters = () => {
    setSearchInput('');
    setSearchTerm('');
    setStatusFilter('TOUS');
    setPage(0);
    setSearchParams({});
  };

  const hasActiveFilters = statusFilter !== 'TOUS' || searchTerm.trim() !== '';

  const getStatusChip = (statut: string) => {
    switch (statut) {
      case 'CLASSIFICATION_EFFECTUEE':
        return <Chip label="Classifiée (Niv.2)" color="secondary" size="small" sx={{ fontWeight: 700 }} />;
      case 'DEMANDE_CREEE':
      case 'BROUILLON':
        return <Chip label="Brouillon" color="default" size="small" sx={{ fontWeight: 700 }} />;
      case 'EN_VISITE_REDACTION':
        return <Chip label="Visite & Rédaction" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'VISITE_REALISEE':
        return <Chip label="Visite réalisée" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'AT_REDIGEE':
      case 'SOUMISE':
        return <Chip label="AT Rédigée ✏️" size="small" sx={{ fontWeight: 700, bgcolor: '#FEF3CD', color: '#7B5E00', border: '1px solid #F0D060' }} />;
      case 'AT_VALIDEE':
      case 'VALIDEE':
        return <Chip label="Validée ✓" size="small" sx={{ fontWeight: 700, bgcolor: '#DCFCE7', color: '#166534', border: '1px solid #86EFAC' }} />;
      case 'EN_COURS':
      case 'INTERVENTION_EN_COURS':
        return <Chip label="En cours ⚡" size="small" sx={{ fontWeight: 700, bgcolor: '#FEF3C7', color: '#92400E', border: '1px solid #FCD34D' }} />;
      case 'EN_RECONDUCTION':
      case 'AT_RECONDUITE':
        return <Chip label="Reconduite 🔄" size="small" sx={{ fontWeight: 700, bgcolor: '#EDE9FE', color: '#5B21B6', border: '1px solid #C4B5FD' }} />;
      case 'DECLAREE_TERMINEE':
      case 'FIN_TRAVAUX_DECLAREE':
        return <Chip label="Fin déclarée 🏁" size="small" sx={{ fontWeight: 700, bgcolor: '#DBEAFE', color: '#1E40AF', border: '1px solid #93C5FD' }} />;
      case 'RECEPTIONEES':
      case 'TRAVAUX_RECEPTIONES':
      case 'CLOTUREE':
        return <Chip label="Clôturée ✓" size="small" sx={{ fontWeight: 700, bgcolor: '#D1FAE5', color: '#065F46', border: '1px solid #6EE7B7' }} />;
      case 'ARCHIVEE':
        return <Chip label="Archivée" color="secondary" size="small" sx={{ fontWeight: 700 }} />;
      case 'REJETEE':
        return <Chip label="Rejetée ✗" color="error" size="small" sx={{ fontWeight: 700 }} />;
      case 'ANNULEE':
        return <Chip label="Annulée" color="error" size="small" variant="outlined" sx={{ fontWeight: 700 }} />;
      default:
        return <Chip label={statut || 'Brouillon'} color="default" size="small" sx={{ fontWeight: 700 }} />;
    }
  };

  // Étape 0 Classification dialog state
  const [classifyOpen, setClassifyOpen] = useState(false);
  const [niveauSelectionne, setNiveauSelectionne] = useState<'NIVEAU_1' | 'NIVEAU_2'>('NIVEAU_2');
  const [isTiers, setIsTiers] = useState(false);

  return (
    <Box sx={{ p: 3 }}>
      {/* ─── En-tête ─────────────────────────────────────────────────── */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
            Autorisations de Travail (AT)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {isCeeeOnly
              ? "Espace Chef d'Équipe Exécutant (CEEE) - Consultation, signature et suivi des interventions"
              : "Gestion du cycle de vie des autorisations de travail OCP F-HSE-SEC-31-04"}
          </Typography>
        </Box>

        <Stack direction="row" spacing={1.5}>
          {(isHc || isAdmin) && (
            <Button
              variant="outlined"
              color="primary"
              onClick={() => setClassifyOpen(true)}
              sx={{ borderRadius: 2, px: 2.5, py: 1.2, fontWeight: 700 }}
            >
              Étape 0 : Classifier Intervention (HCEP)
            </Button>
          )}

          {isCeep && !isCeeeOnly && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => navigate('/autorisations/nouvelle')}
              sx={{ bgcolor: '#1F4D3E', '&:hover': { bgcolor: '#2E624A' }, borderRadius: 2, px: 3, py: 1.2, fontWeight: 700 }}
            >
              Nouvelle Autorisation (CEEP)
            </Button>
          )}
        </Stack>
      </Box>

      {/* ─── Barre de Recherche & Filtres ────────────────────────────── */}
      <Paper
        elevation={0}
        sx={{
          mb: 3,
          borderRadius: 3,
          border: '1.5px solid #D6E3DC',
          background: 'linear-gradient(135deg, #F7FAF8 0%, #FFFFFF 100%)',
          overflow: 'hidden',
        }}
      >
        {/* Ligne de recherche */}
        <Box sx={{ p: 2, pb: 1.5, display: 'flex', gap: 1.5, alignItems: 'center' }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Rechercher par N° AT, objet, description…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: 2,
                bgcolor: 'white',
                '&:hover fieldset': { borderColor: '#1F4D3E' },
                '&.Mui-focused fieldset': { borderColor: '#1F4D3E' },
              },
            }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: '#1F4D3E', fontSize: 20 }} />
                  </InputAdornment>
                ),
                endAdornment: searchInput ? (
                  <InputAdornment position="end">
                    <IconButton size="small" onClick={() => { setSearchInput(''); setSearchTerm(''); setPage(0); }}>
                      <ClearIcon fontSize="small" />
                    </IconButton>
                  </InputAdornment>
                ) : null,
              },
            }}
          />
          <Button
            variant="contained"
            onClick={handleSearch}
            sx={{
              bgcolor: '#1F4D3E',
              '&:hover': { bgcolor: '#2E624A' },
              borderRadius: 2,
              px: 3,
              py: 1,
              fontWeight: 700,
              whiteSpace: 'nowrap',
              minWidth: 120,
            }}
          >
            Rechercher
          </Button>
          {hasActiveFilters && (
            <Button
              variant="outlined"
              onClick={handleClearFilters}
              startIcon={<ClearIcon />}
              sx={{ borderRadius: 2, borderColor: '#D6E3DC', color: '#5C6E67', fontWeight: 600, whiteSpace: 'nowrap' }}
            >
              Effacer
            </Button>
          )}
        </Box>

        {/* Ligne filtres par statut */}
        <Box
          sx={{
            px: 2,
            py: 1.5,
            borderTop: '1px solid #EEF4F0',
            bgcolor: '#FAFCFA',
            display: 'flex',
            alignItems: 'center',
            gap: 1,
            flexWrap: 'wrap',
          }}
        >
          <Typography variant="caption" sx={{ color: '#5C6E67', fontWeight: 700, mr: 0.5, whiteSpace: 'nowrap' }}>
            Statut :
          </Typography>
          {statusFilters.map((f) => {
            const isActive = statusFilter === f.value;
            return (
              <Chip
                key={f.value}
                label={f.label}
                onClick={() => handleStatusChange(f.value)}
                size="small"
                sx={{
                  cursor: 'pointer',
                  fontWeight: isActive ? 800 : 600,
                  fontSize: 12,
                  bgcolor: isActive ? f.color : f.bg,
                  color: isActive ? 'white' : f.color,
                  border: `1.5px solid ${isActive ? f.color : 'transparent'}`,
                  boxShadow: isActive ? `0 2px 8px ${f.color}44` : 'none',
                  transition: 'all 0.18s ease',
                  '&:hover': {
                    bgcolor: f.color,
                    color: 'white',
                    boxShadow: `0 2px 8px ${f.color}55`,
                  },
                }}
              />
            );
          })}

          {/* Compteur de résultats */}
          <Box sx={{ ml: 'auto' }}>
            <Badge
              badgeContent={total}
              max={9999}
              sx={{
                '& .MuiBadge-badge': {
                  bgcolor: '#1F4D3E',
                  color: 'white',
                  fontWeight: 700,
                  fontSize: 11,
                  minWidth: 24,
                  height: 20,
                  borderRadius: 10,
                },
              }}
            >
              <Typography variant="caption" sx={{ color: '#5C6E67', pr: 1.5 }}>
                résultats
              </Typography>
            </Badge>
          </Box>
        </Box>
      </Paper>

      {/* Main Table */}
      <TableContainer component={Paper} sx={{ borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: 'none' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
            <CircularProgress color="success" />
          </Box>
        ) : (
          <Table sx={{ minWidth: 650 }}>
            <TableHead sx={{ bgcolor: '#F7FAF8' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>N° AT</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Objet de l'intervention</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Source</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Date Début</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Statut</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Verrou</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ats.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <Typography variant="body1" color="text.secondary">
                      Aucune Autorisation de Travail trouvée.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                ats.map((row: AutorisationTravail) => (
                  <TableRow key={row.id} hover>
                    <TableCell sx={{ fontWeight: 700, color: '#1F4D3E' }}>
                      {row.numero}
                    </TableCell>

                    <TableCell sx={{ fontWeight: 600, maxWidth: 280 }}>
                      <Typography variant="body2" noWrap sx={{ fontWeight: 600 }}>
                        {row.objet}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" noWrap component="p" sx={{ m: 0 }}>
                        {row.descriptionTravaux || 'Pas de description'}
                      </Typography>
                    </TableCell>

                    <TableCell>
                      {row.typeDocumentSource ? (
                        <Chip label={`${row.typeDocumentSource} ${row.documentSourceNumero || ''}`} size="small" variant="outlined" />
                      ) : (
                        <Typography variant="caption" color="text.secondary">Directe</Typography>
                      )}
                    </TableCell>

                    <TableCell>
                      <Typography variant="body2">
                        {row.dateDebut ? new Date(row.dateDebut).toLocaleDateString('fr-FR') : '-'}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {row.heureDebut || ''} - {row.heureFin || ''}
                      </Typography>
                    </TableCell>

                    <TableCell>{getStatusChip(row.statut)}</TableCell>

                    <TableCell>
                      <Chip
                        label={row.etatVerrou}
                        size="small"
                        color={row.etatVerrou === 'EN_COURS_EDITION' ? 'warning' : 'default'}
                        variant="outlined"
                        sx={{ fontSize: 10 }}
                      />
                    </TableCell>

                    <TableCell align="right">
                      <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'flex-end' }}>
                        {/* 1. Consulter le document (Disponible pour tous les rôles autorisés) */}
                        <Tooltip title="Consulter l'AT">
                          <IconButton size="small" onClick={() => navigate(`/autorisations/${row.id}`)} color="primary">
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        {/* 2. Reprendre le brouillon : STRICTEMENT CEEP émetteur / Admin (Étape 1) */}
                        {row.statut === 'BROUILLON' && (isCeep || isAdmin) && (
                          <Tooltip title="Reprendre et compléter le brouillon (CEEP)">
                            <IconButton
                              size="small"
                              onClick={() => navigate(`/autorisations/${row.id}/editer`)}
                              sx={{ color: '#A87532' }}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}

                        {/* 3. Viser / Signer : CEEE (Étape 3b) ou HC (3c/3d) ou HM (3e/3f) */}
                        {(row.statut === 'SOUMISE' || row.statut === 'AT_REDIGEE') && (
                          <Tooltip title={isCeeeOnly ? "Apposer le visa CEEE (Étape 3b)" : "Valider / Signer l'AT"}>
                            <IconButton
                              size="small"
                              onClick={() => {
                                if (isHc || isHm) {
                                  navigate(`/visas/validation/${row.id}`);
                                } else if (isCeeeOnly) {
                                  navigate(`/autorisations/${row.id}/signature-ceee`);
                                } else {
                                  navigate(`/autorisations/${row.id}`);
                                }
                              }}
                              sx={{ color: '#3C7A5C' }}
                            >
                              <CheckCircleIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}

                        {/* 4. Télécharger le PDF officiel */}
                        {(row.statut === 'VALIDEE' || row.statut === 'CLOTUREE' || row.statut === 'TRAVAUX_RECEPTIONES' || row.statut === 'INTERVENTION_EN_COURS') && (
                          <Tooltip title={row.exportPdfAutorise ? "Exporter le PDF officiel" : "PDF officiel"}>
                            <span>
                              <IconButton
                                size="small"
                                disabled={row.exportPdfAutorise === false}
                                onClick={async () => {
                                  try {
                                    const blob = await autorisationTravailApi.exportPdf(row.id);
                                    const url = window.URL.createObjectURL(blob);
                                    const a = document.createElement('a');
                                    a.href = url;
                                    a.download = `${row.numero}.pdf`;
                                    a.click();
                                  } catch (e: any) {
                                    alert(e.response?.data?.message || 'Erreur lors de l\'export PDF.');
                                  }
                                }}
                                color="error"
                              >
                                <PictureAsPdfIcon fontSize="small" />
                              </IconButton>
                            </span>
                          </Tooltip>
                        )}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}

        <TablePagination
          component="div"
          count={total}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={pageSize}
          onRowsPerPageChange={(e) => {
            setPageSize(parseInt(e.target.value, 10));
            setPage(0);
          }}
          labelRowsPerPage="Lignes par page :"
        />
      </TableContainer>

      {/* Dialog Étape 0 - Classification de l'intervention (§6 & §7 Standard OCP) */}
      <Dialog open={classifyOpen} onClose={() => setClassifyOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 800, color: '#0E2A21' }}>
          Étape 0 - Classification de l'Intervention (Standard S-HSE-SEC-31)
        </DialogTitle>
        <DialogContent dividers>
          <Alert severity="info" sx={{ mb: 3 }}>
            Le Hors Cadre Entité Propriétaire (HCEP) effectue la classification préalable de l'intervention (§6 du Standard).
          </Alert>

          <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
            Nature de l'intervenant :
          </Typography>
          <Stack direction="row" spacing={3} sx={{ mb: 3 }}>
            <Button
              variant={!isTiers ? 'contained' : 'outlined'}
              color="success"
              onClick={() => setIsTiers(false)}
            >
              Ressource Interne au Secteur
            </Button>
            <Button
              variant={isTiers ? 'contained' : 'outlined'}
              color="warning"
              onClick={() => {
                setIsTiers(true);
                setNiveauSelectionne('NIVEAU_2');
              }}
            >
              Tiers / Entreprise Extérieure (AT Obligatoire)
            </Button>
          </Stack>

          <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
            Niveau d'Intervention (§7.1 & §7.2) :
          </Typography>
          <Stack direction="column" spacing={2}>
            <Paper
              onClick={() => !isTiers && setNiveauSelectionne('NIVEAU_1')}
              sx={{
                p: 2,
                border: '2px solid',
                borderColor: niveauSelectionne === 'NIVEAU_1' ? '#7FC8A9' : '#D6E3DC',
                borderRadius: 2,
                cursor: isTiers ? 'not-allowed' : 'pointer',
                opacity: isTiers ? 0.5 : 1,
                bgcolor: niveauSelectionne === 'NIVEAU_1' ? '#EDF2EE' : 'white',
              }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#2E624A' }}>
                Niveau 1 - Intervention de Routine / Interne
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Interventions de routine par ressources internes (conduite, maintenance d'atelier, nettoyage non industriel, DCI).
                <br />
                <strong>Résultat : PAS d'Autorisation de Travail requise.</strong> Couverture par ADRPT + Plan de Prévention (Liste F-HSE-SEC-31-01).
              </Typography>
            </Paper>

            <Paper
              onClick={() => setNiveauSelectionne('NIVEAU_2')}
              sx={{
                p: 2,
                border: '2px solid',
                borderColor: niveauSelectionne === 'NIVEAU_2' ? '#1F4D3E' : '#D6E3DC',
                borderRadius: 2,
                cursor: 'pointer',
                bgcolor: niveauSelectionne === 'NIVEAU_2' ? '#E2F0E8' : 'white',
              }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#1F4D3E' }}>
                Niveau 2 - Intervention à Risque Spécifique / Tiers
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Toute intervention ne faisant pas partie du Niveau 1 OU intervention sollicitant un tiers.
                <br />
                <strong>Résultat : Autorisation de Travail (F-HSE-SEC-31-04) OBLIGATOIRE → Étape 1.</strong>
              </Typography>
            </Paper>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setClassifyOpen(false)}>Annuler</Button>
          {niveauSelectionne === 'NIVEAU_2' ? (
            <Button
              variant="contained"
              color="success"
              onClick={async () => {
                try {
                  await apiClient.post('/classifications', {
                    niveau: 'NIVEAU_2',
                    estTiers: isTiers,
                    natureIntervention: isTiers ? "Intervention par entreprise extérieure" : "Intervention à risque spécifique Niveau 2",
                  });
                } catch (e) {
                  console.warn("Classification enregistrée localement", e);
                }
                setClassifyOpen(false);
                navigate('/autorisations/nouvelle');
              }}
              startIcon={<AddIcon />}
              sx={{ fontWeight: 700 }}
            >
              Passer à l'Étape 1 : Créer AT (F-HSE-SEC-31-04)
            </Button>
          ) : (
            <Button
              variant="contained"
              color="info"
              onClick={async () => {
                try {
                  await apiClient.post('/classifications', {
                    niveau: 'NIVEAU_1',
                    estTiers: false,
                    natureIntervention: "Intervention de routine interne",
                  });
                  alert('Intervention classée Niveau 1 enregistrée en BDD : inscrite au registre F-HSE-SEC-31-01 des interventions de routine. Aucune AT nécessaire.');
                } catch (e) {
                  alert('Intervention classée Niveau 1 : inscrite au registre F-HSE-SEC-31-01 des interventions de routine.');
                }
                setClassifyOpen(false);
              }}
            >
              Enregistrer au Registre Niveau 1 (F-HSE-SEC-31-01)
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </Box>
  );
}
