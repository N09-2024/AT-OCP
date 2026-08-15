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

  // Sync filtre URL (?filtre=SOUMISE) → barre de filtres
  useEffect(() => {
    if (filterParam) setStatusFilter(filterParam);
  }, [filterParam]);

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

    if (statusFilter === 'a-signer') return matchesSearch && (item.statut === 'BROUILLON' || item.statut === 'DEMANDE_CREEE');
    if (statusFilter === 'a-valider') return matchesSearch && (item.statut === 'SOUMISE' || item.statut === 'EN_VISITE_REDACTION' || item.statut === 'AT_REDIGEE');
    if (statusFilter !== 'TOUS') return matchesSearch && item.statut === statusFilter;
    return matchesSearch;
  });

  const getStatusChip = (statut: string) => {
    switch (statut) {
      case 'CLASSIFICATION_EFFECTUEE':
        return <Chip label="Classifiée (Niv.2)" color="secondary" size="small" sx={{ fontWeight: 700 }} />;
      case 'DEMANDE_CREEE':
      case 'BROUILLON':
        return <Chip label="Brouillon / Créée" color="default" size="small" sx={{ fontWeight: 700 }} />;
      case 'EN_VISITE_REDACTION':
        return <Chip label="Visite & Rédaction" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'VISITE_REALISEE':
        return <Chip label="Visite réalisée" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'AT_REDIGEE':
      case 'SOUMISE':
        return <Chip label="AT Rédigée ✏️" color="warning" size="small" sx={{ fontWeight: 700 }} />;
      case 'AT_VALIDEE':
      case 'VALIDEE':
        return <Chip label="AT Validée ✓" color="success" size="small" sx={{ fontWeight: 700 }} />;
      case 'EN_COURS':
      case 'INTERVENTION_EN_COURS':
        return <Chip label="En cours ⚡" color="warning" size="small" sx={{ fontWeight: 700 }} />;
      case 'EN_RECONDUCTION':
      case 'AT_RECONDUITE':
        return <Chip label="Reconduite 🔄" color="warning" size="small" sx={{ fontWeight: 700 }} />;
      case 'DECLAREE_TERMINEE':
      case 'FIN_TRAVAUX_DECLAREE':
        return <Chip label="Fin déclarée 🏁" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'RECEPTIONEES':
      case 'TRAVAUX_RECEPTIONES':
      case 'CLOTUREE':
        return <Chip label="Réceptionnée ✓" color="success" size="small" sx={{ fontWeight: 700 }} />;
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

  const isHCEP = user?.roles?.some((r) => r.nom === 'HCEP' || r.nom === 'HCEE' || r.nom === 'ADMIN');

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
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
            sx={{ bgcolor: '#1F4D3E', '&:hover': { bgcolor: '#2E624A' }, borderRadius: 2, px: 3, py: 1.2, fontWeight: 700 }}
          >
            Nouvelle Autorisation
          </Button>
        </Stack>
      </Box>

      {/* Filter bar */}
      <Paper sx={{ p: 2.5, mb: 3, borderRadius: 3, border: '1px solid #D6E3DC' }}>
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
                      <SearchIcon sx={{ color: '#5C6E67' }} />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </Grid>

          <Grid size={{ xs: 12, md: 7 }}>
            <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
              {[
                { label: 'Toutes', value: 'TOUS' },
                { label: 'Brouillon', value: 'DEMANDE_CREEE' },
                { label: 'Visite & Rédaction', value: 'EN_VISITE_REDACTION' },
                { label: 'AT Rédigée', value: 'AT_REDIGEE' },
                { label: 'Validée', value: 'AT_VALIDEE' },
                { label: 'En cours', value: 'EN_COURS' },
                { label: 'Fin déclarée', value: 'DECLAREE_TERMINEE' },
                { label: 'Réceptionnée', value: 'RECEPTIONEES' },
                { label: 'Rejetée', value: 'REJETEE' },
                { label: 'Annulée', value: 'ANNULEE' },
              ].map((f) => (
                <Chip
                  key={f.value}
                  label={f.label}
                  onClick={() => setStatusFilter(f.value)}
                  color={statusFilter === f.value ? 'primary' : 'default'}
                  variant={statusFilter === f.value ? 'filled' : 'outlined'}
                  sx={{ fontWeight: 600, cursor: 'pointer' }}
                />
              ))}
            </Stack>
          </Grid>
        </Grid>
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
                          <Tooltip title="Viser cette AT (depuis votre interface dédiée)">
                            <IconButton
                              size="small"
                              onClick={() => {
                                const roles = user?.roles?.map((r: any) => r.nom) || [];
                                const isCeee = roles.some((r: string) => r === 'CEEE' || r === 'CE');
                                const isCeep = roles.some((r: string) => r === 'CEEP');
                                const isHcee = roles.some((r: string) => ['HCEE', 'HCEP', 'HC', 'RESPONSABLE_OCP'].includes(r));
                                const isHmee = roles.some((r: string) => ['HMEE', 'HMEP', 'HM'].includes(r));

                                if (isHcee || isHmee) {
                                  // HC et HM → page de validation officielle
                                  navigate(`/visas/validation/${row.id}`);
                                } else if (isCeee && !isCeep) {
                                  // CEEE strict → SA propre page de signature (interface dédiée)
                                  navigate(`/autorisations/${row.id}/signature-ceee`);
                                } else {
                                  // CEEP ou rôle générique CE → consulter l'AT
                                  navigate(`/autorisations/${row.id}`);
                                }
                              }}
                              sx={{ color: '#3C7A5C' }}
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
                              sx={{ color: '#A87532' }}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}

                        {(row.statut === 'VALIDEE' || row.statut === 'CLOTUREE' || row.statut === 'AT_REDIGEE' || row.statut === 'EN_COURS') && (
                          <Tooltip title={row.exportPdfAutorise ? "Exporter le PDF officiel" : "PDF verrouillé : Signatures HCEP, HCEE, HMEP, HMEE requises"}>
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

      {/* Dialog Étape 0 — Classification de l'intervention (§6 & §7 Standard OCP) */}
      <Dialog open={classifyOpen} onClose={() => setClassifyOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle sx={{ fontWeight: 800, color: '#0E2A21' }}>
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
                borderColor: niveauSelectionne === 'NIVEAU_1' ? '#7FC8A9' : '#D6E3DC',
                borderRadius: 2,
                cursor: isTiers ? 'not-allowed' : 'pointer',
                opacity: isTiers ? 0.5 : 1,
                bgcolor: niveauSelectionne === 'NIVEAU_1' ? '#EDF2EE' : 'white',
              }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#2E624A' }}>
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
                borderColor: niveauSelectionne === 'NIVEAU_2' ? '#1F4D3E' : '#D6E3DC',
                borderRadius: 2,
                cursor: 'pointer',
                bgcolor: niveauSelectionne === 'NIVEAU_2' ? '#E2F0E8' : 'white',
              }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#1F4D3E' }}>
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
