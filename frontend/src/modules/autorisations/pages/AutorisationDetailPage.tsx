import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Stack,
  Paper,
  Tabs,
  Tab,
  Alert,
  Divider,
  Tooltip,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import VerifiedIcon from '@mui/icons-material/Verified';
import ArchiveIcon from '@mui/icons-material/Archive';
import LockIcon from '@mui/icons-material/Lock';
import HistoryIcon from '@mui/icons-material/History';
import EditIcon from '@mui/icons-material/Edit';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import { visaApi } from '../../../services/visaApi';
import { archiveApi } from '../../../services/archiveApi';
import { apiClient } from '../../../services/apiClient';
import type { AutorisationTravail, Visa, HistoriqueAT } from '../../../types';
import FormulaireOCPViewer from '../../../components/common/FormulaireOCPViewer';
import { useAuthStore } from '../../../store/authStore';

export default function AutorisationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  const [loading, setLoading] = useState(true);
  const [at, setAt] = useState<AutorisationTravail | null>(null);
  const [visas, setVisas] = useState<Visa[]>([]);
  const [historiques, setHistoriques] = useState<HistoriqueAT[]>([]);
  const [tabIndex, setTabIndex] = useState(0);
  const [pdfLoading, setPdfLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const loadDetails = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await autorisationTravailApi.findById(id);
      setAt(data);
      const visaList = await visaApi.getVisasByAtId(id);
      setVisas(visaList || []);
      const histoList = await autorisationTravailApi.getHistorique(id);
      setHistoriques(histoList || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetails();
  }, [id]);

  const handleExportPdf = async () => {
    if (!id || !at) return;
    setPdfLoading(true);
    try {
      const blob = await autorisationTravailApi.exportPdf(id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${at.numero || 'AT'}.pdf`;
      a.click();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erreur lors de la génération du PDF.');
    } finally {
      setPdfLoading(false);
    }
  };

  const handleArchiver = async () => {
    if (!id || !at) return;
    try {
      await archiveApi.archiverAT(id);
      alert('AT archivée officiellement avec succès (Étape 8).');
      loadDetails();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erreur lors de l\'archivage.');
    }
  };

  // Étape 4 - Démarrer l'intervention (CEEE Exécutant)
  const handleDemarrerIntervention = async () => {
    if (!id) return;
    setActionLoading(true);
    try {
      await apiClient.post(`/autorisations-travail/${id}/demarrer-intervention`);
      alert('Démarrage des travaux enregistré avec succès (Étape 4). Statut : INTERVENTION EN COURS.');
      loadDetails();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erreur lors du démarrage des travaux.');
    } finally {
      setActionLoading(false);
    }
  };

  // Étape 5b - Reconduire l'AT (CEEP / CEEE / HCEE)
  const handleReconduire = async () => {
    if (!id) return;
    setActionLoading(true);
    try {
      await apiClient.post(`/autorisations-travail/${id}/renew`);
      alert('AT et permis reconduits pour le poste (Étape 5b). Version incrémentée.');
      loadDetails();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erreur lors de la reconduction.');
    } finally {
      setActionLoading(false);
    }
  };

  // Étape 6 - Déclarer la fin des travaux (CEEE Exécutant)
  const handleDeclarerFin = async () => {
    if (!id) return;
    setActionLoading(true);
    try {
      await apiClient.post(`/autorisations-travail/${id}/declarer-fin`);
      alert('Fin des travaux déclarée par le CEEE avec succès (Étape 6). Prêt pour réception.');
      loadDetails();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erreur lors de la déclaration de fin.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  if (!at) {
    return (
      <Box sx={{ p: 4, textAlign: 'center' }}>
        <Alert severity="error">Autorisation de travail introuvable.</Alert>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/autorisations')} sx={{ mt: 2 }}>
          Retour à la liste
        </Button>
      </Box>
    );
  }

  // ═══════════════════════════════════════════════════════════════════════
  // LOGIQUE STRICTE : Rôle de l'utilisateur connecté
  // ═══════════════════════════════════════════════════════════════════════
  const roles = user?.roles?.map((r: any) => r.nom) || [];
  const isAdmin  = roles.includes('ADMIN');
  const isCeep   = roles.includes('CEEP') || isAdmin;
  const isCeee   = roles.includes('CEEE') || isAdmin;
  const isCe     = isCeep || isCeee;
  const isHcep   = roles.includes('HCEP') || isAdmin;
  const isHcee   = roles.includes('HCEE') || isAdmin;
  const isHmep   = roles.includes('HMEP') || isAdmin;
  const isHmee   = roles.includes('HMEE') || isAdmin;
  const isHc     = isHcep || isHcee;
  const isHm     = isHmep || isHmee;

  // Raccourcis de statut
  const statut = at.statut as string;

  // Détection des visas déjà apposés (par rôle du signataire)
  const isPositiveVisa = (v: Visa) =>
    v.statut === 'VALIDE' || v.statut === 'VALIDATION' || v.statut === 'SIGNATURE';

  const detectVisa = (roleKeyword: string) =>
    visas.some(v =>
      isPositiveVisa(v) && (
        v.commentaire?.toUpperCase().includes(roleKeyword) ||
        (v as any).role?.toUpperCase().includes(roleKeyword) ||
        (v as any).utilisateurRole?.toUpperCase().includes(roleKeyword)
      )
    );

  const hasCeeeVisa = detectVisa('CEEE');
  const hasHcepVisa = detectVisa('HCEP');
  const hasHceeVisa = detectVisa('HCEE');
  const hasHmepVisa = detectVisa('HMEP');
  const hasHmeeVisa = detectVisa('HMEE');

  // Toutes les signatures requises avant PDF
  const allSignaturesComplete = hasCeeeVisa && hasHcepVisa && hasHceeVisa && hasHmepVisa && hasHmeeVisa;
  const hmSignaturesComplete  = hasHmepVisa && hasHmeeVisa;

  // Validation globale des droits pour certaines actions
  const hasValidationRights = isHc;

  // ═══════════════════════════════════════════════════════════════════════
  // Règles strictes d'affichage des boutons
  // Standard S-HSE-SEC-31 - Cycle de vie séquentiel
  // ═══════════════════════════════════════════════════════════════════════

  // Étape 0 - Reprendre le brouillon (CEEP uniquement, AT en BROUILLON)
  const showEditDraft = statut === 'BROUILLON' && isCeep;

  // Étape 3b - Visa CEEE (CEEE uniquement, AT en SOUMISE/AT_REDIGEE)
  const showSignCeee =
    (statut === 'SOUMISE' || statut === 'AT_REDIGEE') &&
    isCeee && !hasCeeeVisa;

  // Étape 3c - Visa HCEP (HCEP uniquement, après visa CEEE, AT non encore validée HCEP)
  const showSignHcep =
    (statut === 'SOUMISE' || statut === 'AT_REDIGEE' || statut === 'EN_ATTENTE_VALIDATION') &&
    isHcep && hasCeeeVisa && !hasHcepVisa;

  // Étape 3d - Visa HCEE (HCEE uniquement, après visa HCEP)
  const showSignHcee =
    (statut === 'SOUMISE' || statut === 'AT_REDIGEE' || statut === 'EN_ATTENTE_VALIDATION') &&
    isHcee && hasHcepVisa && !hasHceeVisa;

  // Étape 3e - Visa HMEP (HMEP uniquement, après visa HCEE)
  const showSignHmep =
    (statut === 'SOUMISE' || statut === 'AT_REDIGEE' || statut === 'EN_ATTENTE_VALIDATION') &&
    isHmep && hasHceeVisa && !hasHmepVisa;

  // Étape 3f - Visa HMEE (HMEE uniquement, après visa HMEP)
  const showSignHmee =
    (statut === 'SOUMISE' || statut === 'AT_REDIGEE' || statut === 'EN_ATTENTE_VALIDATION') &&
    isHmee && hasHmepVisa && !hasHmeeVisa;

  // Étape 4 - Démarrer l'intervention (CEEE uniquement, AT VALIDEE)
  const showDemarrer =
    (statut === 'VALIDEE' || statut === 'AT_VALIDEE') && isCeee;

  // Étape 5b - Reconduire (CEEP ou CEEE, AT EN COURS)
  const showReconduire =
    (statut === 'INTERVENTION_EN_COURS' || statut === 'AT_RECONDUITE') && isCe;

  // Étape 6 - Déclarer la fin (CEEE uniquement, AT EN COURS)
  const showDeclarerFin =
    (statut === 'INTERVENTION_EN_COURS' || statut === 'AT_RECONDUITE') && isCeee;

  // Étape 7 - Réception conjointe (CEEP ou CEEE, AT FIN_TRAVAUX_DECLAREE)
  const showReceptionner =
    statut === 'FIN_TRAVAUX_DECLAREE' && isCe;

  // PDF officiel - disponible après toutes signatures HM, pour tout rôle qui a signé
  const showPdf = hmSignaturesComplete || allSignaturesComplete ||
    (statut === 'CLOTUREE') || (statut === 'TRAVAUX_RECEPTIONES');

  // Étape 8 - Archivage officiel (HCEP ou HCEE uniquement, AT CLOTUREE)
  const showArchiver =
    (statut === 'CLOTUREE' || statut === 'TRAVAUX_RECEPTIONES') && hasValidationRights;

  return (
    <Box sx={{ p: 3, maxWidth: 1100, mx: 'auto' }}>
      {/* Header Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/autorisations')} sx={{ color: '#5C6E67' }}>
          Retour aux ATs
        </Button>

        <Stack direction="row" spacing={1.5} sx={{ flexWrap: 'wrap', gap: 1 }}>
          {/* Étape 0 - Reprendre brouillon (CEEP) */}
          {showEditDraft && (
            <Button
              variant="contained"
              startIcon={<EditIcon />}
              onClick={() => navigate(`/autorisations/${at.id}/editer`)}
              sx={{ fontWeight: 700, background: '#A87532', '&:hover': { background: '#A87532' } }}
            >
              Reprendre le brouillon
            </Button>
          )}

          {/* Étape 3b - Visa CEEE */}
          {showSignCeee && (
            <Tooltip title="Accuser réception et apposer votre visa CEEE">
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={() => navigate(`/autorisations/${at.id}/signature-ceee`)}
                sx={{ fontWeight: 700 }}
              >
                Signer l'AT (Visa CEEE)
              </Button>
            </Tooltip>
          )}

          {/* Étape 3c - Visa HCEP */}
          {showSignHcep && (
            <Tooltip title="Apposer votre visa en tant que Hors Cadre Propriétaire (HCEP)">
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={() => navigate(`/visas/validation/${at.id}?role=HCEP`)}
                sx={{ fontWeight: 700 }}
              >
                Signer l'AT (Visa HCEP)
              </Button>
            </Tooltip>
          )}

          {/* Étape 3d - Visa HCEE */}
          {showSignHcee && (
            <Tooltip title="Apposer votre visa en tant que Hors Cadre Exécutant (HCEE)">
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={() => navigate(`/visas/validation/${at.id}?role=HCEE`)}
                sx={{ fontWeight: 700 }}
              >
                Signer l'AT (Visa HCEE)
              </Button>
            </Tooltip>
          )}

          {/* Étape 3e - Visa HMEP */}
          {showSignHmep && (
            <Tooltip title="Apposer votre visa en tant que Haute Maîtrise Propriétaire (HMEP)">
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={() => navigate(`/visas/validation/${at.id}?role=HMEP`)}
                sx={{ fontWeight: 700 }}
              >
                Signer l'AT (Visa HMEP)
              </Button>
            </Tooltip>
          )}

          {/* Étape 3f - Visa HMEE */}
          {showSignHmee && (
            <Tooltip title="Apposer votre visa en tant que Haute Maîtrise Exécutante (HMEE)">
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={() => navigate(`/visas/validation/${at.id}?role=HMEE`)}
                sx={{ fontWeight: 700 }}
              >
                Signer l'AT (Visa HMEE)
              </Button>
            </Tooltip>
          )}

          {/* Étape 4 - Démarrer l'intervention (CEEE) */}
          {showDemarrer && (
            <Button
              variant="contained"
              color="success"
              startIcon={<PlayArrowIcon />}
              onClick={handleDemarrerIntervention}
              disabled={actionLoading}
              sx={{ fontWeight: 700 }}
            >
              Démarrer l'intervention
            </Button>
          )}

          {/* Étape 5b - Reconduire l'AT (CEEP / CEEE) */}
          {showReconduire && (
            <Button
              variant="outlined"
              color="warning"
              startIcon={<AutorenewIcon />}
              onClick={handleReconduire}
              disabled={actionLoading}
              sx={{ fontWeight: 700 }}
            >
              Reconduire l'AT (2ème / 3ème poste)
            </Button>
          )}

          {/* Étape 6 - Déclarer la fin des travaux (CEEE) */}
          {showDeclarerFin && (
            <Button
              variant="contained"
              color="info"
              startIcon={<TaskAltIcon />}
              onClick={handleDeclarerFin}
              disabled={actionLoading}
              sx={{ fontWeight: 700 }}
            >
              Déclarer la fin des travaux
            </Button>
          )}

          {/* Étape 7 - Réception conjointe (CEEP + CEEE) */}
          {showReceptionner && (
            <Button
              variant="contained"
              color="primary"
              startIcon={<VerifiedIcon />}
              onClick={() => navigate(`/receptions?atId=${at.id}`)}
              sx={{ fontWeight: 700 }}
            >
              Réception conjointe & Clôture
            </Button>
          )}

          {/* PDF Officiel */}
          {showPdf && (
            <Button
              variant="outlined"
              color="success"
              startIcon={pdfLoading ? <CircularProgress size={18} color="inherit" /> : <PictureAsPdfIcon />}
              onClick={handleExportPdf}
              disabled={pdfLoading}
              sx={{ fontWeight: 700 }}
            >
              Télécharger le PDF Officiel
            </Button>
          )}

          {/* Étape 8 - Archivage officiel (HCEP / HCEE) */}
          {showArchiver && (
            <Button
              variant="outlined"
              color="secondary"
              startIcon={<ArchiveIcon />}
              onClick={handleArchiver}
              sx={{ fontWeight: 700 }}
            >
              Archiver officiellement (Étape 8)
            </Button>
          )}
        </Stack>
      </Box>

      {at.exportPdfAutorise === false && (at.exportPdfMotifsRefus || []).length > 0 && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
            Conditions requises pour télécharger le document PDF officiel (Standard S-HSE-SEC-31 & Formulaire F-HSE-SEC-31-04) :
          </Typography>
          <ul style={{ margin: '4px 0 0 16px', padding: 0 }}>
            {at.exportPdfMotifsRefus?.map((motif, i) => (
              <li key={i}>{motif}</li>
            ))}
          </ul>
        </Alert>
      )}

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabIndex} onChange={(_, v) => setTabIndex(v)}>
          <Tab label="Formulaire Officiel F-HSE-SEC-31-04" />
          <Tab label={`Visas (${visas.length})`} />
          <Tab label={`Historique d'Audit (${historiques.length})`} />
        </Tabs>
      </Box>

      {/* Tab 0: Form Viewer */}
      {tabIndex === 0 && <FormulaireOCPViewer at={at} visas={visas} />}

      {/* Tab 1: Visas */}
      {tabIndex === 1 && (
        <Paper sx={{ p: 3, borderRadius: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
            Liste des Visas & Signatures
          </Typography>
          {visas.length === 0 ? (
            <Alert severity="info">Aucun visa enregistré pour le moment.</Alert>
          ) : (
            visas.map((v) => (
              <Box key={v.id} sx={{ p: 2, mb: 2, border: '1px solid #D6E3DC', borderRadius: 2, bgcolor: '#F7FAF8' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    Signataire : {v.utilisateurNomComplet || 'Utilisateur'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {v.dateSignature ? new Date(v.dateSignature).toLocaleString('fr-FR') : 'En attente'}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  Statut : <strong>{v.statut}</strong> | IP : {v.adresseIP || 'Non capturée'}
                </Typography>
                {v.commentaire && (
                  <Typography variant="body2" sx={{ fontStyle: 'italic', mt: 1, p: 1, bgcolor: 'white', borderRadius: 1 }}>
                    « {v.commentaire} »
                  </Typography>
                )}
              </Box>
            ))
          )}
        </Paper>
      )}

      {/* Tab 2: Historique */}
      {tabIndex === 2 && (
        <Paper sx={{ p: 3, borderRadius: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
            Journal des Événements & Audit
          </Typography>
          {historiques.length === 0 ? (
            <Alert severity="info">Aucun événement enregistré.</Alert>
          ) : (
            historiques.map((h) => (
              <Box key={h.id} sx={{ p: 2, mb: 1.5, borderLeft: '4px solid #1F4D3E', bgcolor: '#F7FAF8', borderRadius: 1 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    {h.action} par {h.utilisateurNomComplet || 'Système'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(h.dateAction).toLocaleString('fr-FR')}
                  </Typography>
                </Box>
                {h.commentaire && (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                    {h.commentaire}
                  </Typography>
                )}
              </Box>
            ))
          )}
        </Paper>
      )}
    </Box>
  );
}