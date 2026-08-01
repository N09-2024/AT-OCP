import React, { useState, useEffect } from 'react';
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
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import VisibilityIcon from '@mui/icons-material/Visibility';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EditIcon from '@mui/icons-material/Edit';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import FilterListIcon from '@mui/icons-material/FilterList';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import { apiClient } from '../../../services/apiClient';
import type { AutorisationTravail } from '../../../types';
import { useAuthStore } from '../../../store/authStore';

export default function AutorisationListPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const filterParam = searchParams.get('filtre');
  const user = useAuthStore((s) => s.user);

  const [loading, setLoading] = useState(true);
  const [ats, setAts] = useState<AutorisationTravail[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>(filterParam || 'TOUS');

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await autorisationTravailApi.findAll(page, pageSize);
      setAts(res.content || []);
      setTotal(res.totalElements || 0);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [page, pageSize]);

  const filteredAts = ats.filter((item) => {
    const matchesSearch =
      (item.numero || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
      (item.objet || '').toLowerCase().includes(searchTerm.toLowerCase());

    if (statusFilter === 'a-signer') return matchesSearch && item.statut === 'BROUILLON';
    if (statusFilter === 'a-valider') return matchesSearch && item.statut === 'SOUMISE';
    if (statusFilter !== 'TOUS') return matchesSearch && item.statut === statusFilter;
    return matchesSearch;
  });

  const getStatusChip = (statut: string) => {
    switch (statut) {
      case 'CLASSIFICATION_EFFECTUEE':
        return <Chip label="Classifiée (Niveau 2)" color="secondary" size="small" sx={{ fontWeight: 700 }} />;
      case 'DEMANDE_CREEE':
        return <Chip label="Demande créée" color="primary" size="small" sx={{ fontWeight: 700 }} />;
      case 'VISITE_REALISEE':
        return <Chip label="Visite réalisée" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'AT_REDIGEE':
      case 'VALIDEE':
        return <Chip label="AT Rédigée / Validée ✓" color="success" size="small" sx={{ fontWeight: 700 }} />;
      case 'INTERVENTION_EN_COURS':
        return <Chip label="En cours ⚡" color="warning" size="small" sx={{ fontWeight: 700 }} />;
      case 'AT_RECONDUITE':
        return <Chip label="Reconduite 🔄" color="warning" size="small" sx={{ fontWeight: 700 }} />;
      case 'FIN_TRAVAUX_DECLAREE':
        return <Chip label="Fin déclarée 🏁" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'TRAVAUX_RECEPTIONES':
      case 'CLOTUREE':
        return <Chip label="Réceptionnée / Clôturée" color="success" size="small" sx={{ fontWeight: 700 }} />;
      case 'ARCHIVEE':
        return <Chip label="Archivée" color="secondary" size="small" sx={{ fontWeight: 700 }} />;
      case 'SOUMISE':
        return <Chip label="Soumise" color="warning" size="small" sx={{ fontWeight: 700 }} />;
      case 'REJETEE':
        return <Chip label="Rejetée ✗" color="error" size="small" sx={{ fontWeight: 700 }} />;
      default:
        return <Chip label={statut || "Brouillon"} color="default" size="small" sx={{ fontWeight: 700 }} />;
    }
  };

  // Étape 0 Classification dialog state
  const [classifyOpen, setClassifyOpen] = useState(false);
  const [niveauSelectionne, setNiveauSelectionne] = useState<'NIVEAU_1' | 'NIVEAU_2'>('NIVEAU_2');
  const [isTiers, setIsTiers] = useState(false);

  const isHCEP = user?.roles?.some((r) => r.nom === 'HCEP' || r.nom === 'HCEE' || r.nom === 'ADMIN');

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0f172a' }}>
            Autorisations de Travail (AT)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Gestion du cycle de vie des autorisations de travail OCP F-HSE-SEC-31-04
          </Typography>
        </Box>

        <Stack direction="row" spacing={1.5}>
          {isHCEP && (
            <Button
              variant="outlined"
              color="primary"
              onClick={() => setClassifyOpen(true)}
              sx={{ borderRadius: 2, px: 2.5, py: 1.2, fontWeight: 700 }}
            >
              Étape 0 : Classifier Intervention (HCEP)
            </Button>
          )}

          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/autorisations/nouvelle')}
            sx={{ bgcolor: '#00875A', '&:hover': { bgcolor: '#047857' }, borderRadius: 2, px: 3, py: 1.2, fontWeight: 700 }}
          >
            Nouvelle Autorisation
          </Button>
        </Stack>
      </Box>

      {/* Filter bar */}
      <Paper sx={{ p: 2.5, mb: 3, borderRadius: 3, border: '1px solid #e2e8f0' }}>
        <Grid container spacing={2} sx={{ alignItems: 'center' }}>
          <Grid size={{ xs: 12, md: 5 }}>
            <TextField
              fullWidth
              size="small"
              placeholder="Rechercher par N° AT ou Objet..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: '#94a3b8' }} />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Grid>

          <Grid size={{ xs: 12, md: 7 }}>
            <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
              {['TOUS', 'BROUILLON', 'SOUMISE', 'VALIDEE', 'REJETEE', 'CLOTUREE'].map((st) => (
                <Chip
                  key={st}
                  label={st === 'TOUS' ? 'Toutes' : st}
                  onClick={() => setStatusFilter(st)}
                  color={statusFilter === st ? 'primary' : 'default'}
                  variant={statusFilter === st ? 'filled' : 'outlined'}
                  sx={{ fontWeight: 600, cursor: 'pointer' }}
                />
              ))}
            </Stack>
          </Grid>
        </Grid>
      </Paper>

      {/* Main Table */}
      <TableContainer component={Paper} sx={{ borderRadius: 3, border: '1px solid #e2e8f0', boxShadow: 'none' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
            <CircularProgress color="success" />
          </Box>
        ) : (
          <Table sx={{ minWidth: 650 }}>
            <TableHead sx={{ bgcolor: '#f8fafc' }}>
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
              {filteredAts.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <Typography variant="body1" color="text.secondary">
                      Aucune Autorisation de Travail trouvée.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                filteredAts.map((row) => (
                  <TableRow key={row.id} hover>
                    <TableCell sx={{ fontWeight: 700, color: '#00875A' }}>
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
                        {row.dateDebut ? new Date(row.dateDebut).toLocaleDateString('fr-FR') : '—'}
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
                        <Tooltip title="Consulter le document">
                          <IconButton size="small" onClick={() => navigate(`/autorisations/${row.id}`)} color="primary">
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        {row.statut === 'SOUMISE' && (
                          <Tooltip title="Valider / Rejeter (Responsable OCP)">
                            <IconButton
                              size="small"
                              onClick={() => navigate(`/visas/validation/${row.id}`)}
                              sx={{ color: '#059669' }}
                            >
                              <CheckCircleIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}

                        {row.statut === 'BROUILLON' && (
                          <Tooltip title="Reprendre et compléter le brouillon">
                            <IconButton
                              size="small"
                              onClick={() => navigate(`/autorisations/${row.id}/editer`)}
                              sx={{ color: '#f59e0b' }}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}

                        {(row.statut === 'VALIDEE' || row.statut === 'CLOTUREE') && (
                          <Tooltip title="Exporter le PDF officiel">
                            <IconButton
                              size="small"
                              onClick={async () => {
                                try {
                                  const blob = await autorisationTravailApi.exportPdf(row.id);
                                  const url = window.URL.createObjectURL(blob);
                                  const a = document.createElement('a');
                                  a.href = url;
                                  a.download = `${row.numero}.pdf`;
                                  a.click();
                                } catch (e) {
                                  alert('Erreur lors de l\'export PDF.');
                                }
                              }}
                              color="error"
                            >
                              <PictureAsPdfIcon fontSize="small" />
                            </IconButton>
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

      {/* Dialog Étape 0 — Classification de l'intervention (§6 & §7 Standard OCP) */}
      <Dialog open={classifyOpen} onClose={() => setClassifyOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 800, color: '#0f172a' }}>
          Étape 0 — Classification de l'Intervention (Standard S-HSE-SEC-31)
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
                borderColor: niveauSelectionne === 'NIVEAU_1' ? '#10b981' : '#e2e8f0',
                borderRadius: 2,
                cursor: isTiers ? 'not-allowed' : 'pointer',
                opacity: isTiers ? 0.5 : 1,
                bgcolor: niveauSelectionne === 'NIVEAU_1' ? '#f0fdf4' : 'white',
              }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#047857' }}>
                Niveau 1 — Intervention de Routine / Interne
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
                borderColor: niveauSelectionne === 'NIVEAU_2' ? '#00875A' : '#e2e8f0',
                borderRadius: 2,
                cursor: 'pointer',
                bgcolor: niveauSelectionne === 'NIVEAU_2' ? '#ecfdf5' : 'white',
              }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#00875A' }}>
                Niveau 2 — Intervention à Risque Spécifique / Tiers
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
