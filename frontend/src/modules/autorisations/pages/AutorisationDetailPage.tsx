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

  // Étape 4 — Démarrer l'intervention (CEEE Exécutant)
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

  // Étape 5b — Reconduire l'AT (CEEP / CEEE / HCEE)
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

  // Étape 6 — Déclarer la fin des travaux (CEEE Exécutant)
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

  // Rôles ayant des droits de validation/garantie sur l'AT (HCEE, HCEP, ADMIN)
  const roles = user?.roles?.map((r: any) => r.nom) || [];
  // HC = HCEP/HCEE | HM = HMEP/HMEE | CE = CEEP/CEEE (standard S-HSE-SEC-31)
  const isHc = roles.some((n: string) => ['HCEE', 'HCEP', 'ADMIN'].includes(n));
  const isHm = roles.some((n: string) => ['HMEP', 'HMEE', 'ADMIN'].includes(n));
  const isCe = roles.some((n: string) => ['CEEE', 'CEEP'].includes(n));
  const hasValidationRights = isHc; // validation formelle souvent HCEE
  const isCeee = roles.includes('CEEE') || roles.includes('ADMIN');
  // Garantir une AT soumise : HC ou HM ; viser case CEEE : CEEE
  const canGarantirSoumise = isHc || isHm;
  const canViserSoumise = canGarantirSoumise || isCeee;

  return (
    <Box sx={{ p: 3, maxWidth: 1100, mx: 'auto' }}>
      {/* Header Actions */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/autorisations')} sx={{ color: '#475569' }}>
          Retour aux ATs
        </Button>

        <Stack direction="row" spacing={1.5} sx={{ flexWrap: 'wrap', gap: 1 }}>
          {at.statut === 'BROUILLON' && (
            <Button
              variant="contained"
              startIcon={<EditIcon />}
              onClick={() => navigate(`/autorisations/${at.id}/editer`)}
              sx={{ fontWeight: 700, background: '#f59e0b', '&:hover': { background: '#d97706' } }}
            >
              Reprendre le brouillon
           {/* Actions de signatures adaptées CEEP -> CEEE -> HC (HCEP, HCEE) -> HM (HMEP, HMEE) */}
          {(() => {
            const hasCeep = at.statut !== 'BROUILLON';
            const hasCeee = visas.some(v => (v.statut === 'VALIDE' || v.statut === 'VALIDATION') && (v.commentaire?.toUpperCase().includes('CEEE') || (v as any).utilisateurNomComplet?.includes('CEEE')));
            const hasHcep = visas.some(v => (v.statut === 'VALIDE' || v.statut === 'VALIDATION') && (v.commentaire?.toUpperCase().includes('HCEP') || (v as any).utilisateurNomComplet?.includes('HCEP')));
            const hasHcee = visas.some(v => (v.statut === 'VALIDE' || v.statut === 'VALIDATION') && (v.commentaire?.toUpperCase().includes('HCEE') || (v as any).utilisateurNomComplet?.includes('HCEE')));
            const hasHmep = visas.some(v => (v.statut === 'VALIDE' || v.statut === 'VALIDATION') && (v.commentaire?.toUpperCase().includes('HMEP') || (v as any).utilisateurNomComplet?.includes('HMEP')));
            const hasHmee = visas.some(v => (v.statut === 'VALIDE' || v.statut === 'VALIDATION') && (v.commentaire?.toUpperCase().includes('HMEE') || (v as any).utilisateurNomComplet?.includes('HMEE')));

            const isHcepUser = roles.includes('HCEP') || roles.includes('ADMIN');
            const isHceeUser = roles.includes('HCEE') || roles.includes('HC') || roles.includes('ADMIN');
            const isHmepUser = roles.includes('HMEP') || roles.includes('HM') || roles.includes('ADMIN');
            const isHmeeUser = roles.includes('HMEE') || roles.includes('HM') || roles.includes('ADMIN');

            const ceepCeeeComplete = hasCeep && (hasCeee || at.statut === 'AT_REDIGEE' || at.statut === 'VALIDEE');
            const hcepHceeComplete = hasHcep && hasHcee;

            return (
              <>
                {!hasHcep && (isHcepUser || roles.includes('HC')) && (
                  <Tooltip title={!ceepCeeeComplete ? "CEEP et CEEE doivent signer d'abord" : "Signer l'AT en tant que HCEP"}>
                    <span>
                      <Button
                        variant="contained"
                        color="primary"
                        startIcon={<CheckCircleIcon />}
                        disabled={!ceepCeeeComplete && !roles.includes('ADMIN')}
                        onClick={() => navigate(`/visas/validation/${at.id}?role=HCEP`)}
                        sx={{ fontWeight: 700 }}
                      >
                        Signer AT (HCEP — Hors Cadre Propriétaire)
                      </Button>
                    </span>
                  </Tooltip>
                )}

                {!hasHcee && (isHceeUser || roles.includes('HC')) && (
                  <Tooltip title={!ceepCeeeComplete ? "CEEP et CEEE doivent signer d'abord" : "Signer l'AT en tant que HCEE"}>
                    <span>
                      <Button
                        variant="contained"
                        color="info"
                        startIcon={<CheckCircleIcon />}
                        disabled={!ceepCeeeComplete && !roles.includes('ADMIN')}
                        onClick={() => navigate(`/visas/validation/${at.id}?role=HCEE`)}
                        sx={{ fontWeight: 700 }}
                      >
                        Signer AT (HCEE — Hors Cadre Exécutant)
                      </Button>
                    </span>
                  </Tooltip>
                )}

                {!hasHmep && (isHmepUser || roles.includes('HM')) && (
                  <Tooltip title={!hcepHceeComplete ? "HCEP et HCEE doivent signer d'abord" : "Signer l'AT en tant que HMEP"}>
                    <span>
                      <Button
                        variant="contained"
                        color="secondary"
                        startIcon={<CheckCircleIcon />}
                        disabled={!hcepHceeComplete && !roles.includes('ADMIN')}
                        onClick={() => navigate(`/visas/validation/${at.id}?role=HMEP`)}
                        sx={{ fontWeight: 700 }}
                      >
                        Signer AT (HMEP — Haute Maîtrise Propriétaire)
                      </Button>
                    </span>
                  </Tooltip>
                )}

                {!hasHmee && (isHmeeUser || roles.includes('HM')) && (
                  <Tooltip title={!hcepHceeComplete ? "HCEP et HCEE doivent signer d'abord" : "Signer l'AT en tant que HMEE"}>
                    <span>
                      <Button
                        variant="contained"
                        color="success"
                        startIcon={<CheckCircleIcon />}
                        disabled={!hcepHceeComplete && !roles.includes('ADMIN')}
                        onClick={() => navigate(`/visas/validation/${at.id}?role=HMEE`)}
                        sx={{ fontWeight: 700 }}
                      >
                        Signer AT (HMEE — Haute Maîtrise Exécutant)
                      </Button>
                    </span>
                  </Tooltip>
                )}
              </>
            );
          })()}}

          {(at.statut === 'VALIDEE' || at.statut === 'AT_REDIGEE') && (
            <Button
              variant="contained"
              color="success"
              startIcon={<PlayArrowIcon />}
              onClick={handleDemarrerIntervention}
              disabled={actionLoading}
              sx={{ fontWeight: 700 }}
            >
              Étape 4 : Démarrer l'intervention (CEEE)
            </Button>
          )}

          {(at.statut === 'INTERVENTION_EN_COURS' || at.statut === 'AT_RECONDUITE') && (
            <>
              <Button
                variant="outlined"
                color="warning"
                startIcon={<AutorenewIcon />}
                onClick={handleReconduire}
                disabled={actionLoading}
                sx={{ fontWeight: 700 }}
              >
                Étape 5b : Visa de Reconduction
              </Button>

              <Button
                variant="contained"
                color="info"
                startIcon={<TaskAltIcon />}
                onClick={handleDeclarerFin}
                disabled={actionLoading}
                sx={{ fontWeight: 700 }}
              >
                Étape 6 : Déclarer la fin des travaux (CEEE)
              </Button>
            </>
          )}

          {(at.statut === 'FIN_TRAVAUX_DECLAREE' || at.statut === 'VALIDEE') && (
            <Button
              variant="contained"
              color="primary"
              startIcon={<VerifiedIcon />}
              onClick={() => navigate(`/receptions?atId=${at.id}`)}
              sx={{ fontWeight: 700 }}
            >
              Étape 7 : Réceptionner les travaux (CEEP + CEEE)
            </Button>
          )}

          {at.exportPdfAutorise ? (
            <Button
              variant="contained"
              color="error"
              startIcon={pdfLoading ? <CircularProgress size={18} color="inherit" /> : <PictureAsPdfIcon />}
              onClick={handleExportPdf}
              disabled={pdfLoading}
              sx={{ fontWeight: 700 }}
            >
              Télécharger le PDF Officiel
            </Button>
          ) : (
            <Tooltip title={at.exportPdfMotifsRefus && at.exportPdfMotifsRefus.length > 0 ? at.exportPdfMotifsRefus.join(' | ') : "Validation HM/HC et conformité permis requises"}>
              <span>
                <Button
                  variant="outlined"
                  color="error"
                  disabled
                  startIcon={<PictureAsPdfIcon />}
                  sx={{ fontWeight: 700 }}
                >
                  Télécharger le PDF (Incomplet)
                </Button>
              </span>
            </Tooltip>
          )}

          {(at.statut === 'CLOTUREE' || at.statut === 'TRAVAUX_RECEPTIONES') && hasValidationRights && (
            <Button
              variant="outlined"
              color="secondary"
              startIcon={<ArchiveIcon />}
              onClick={handleArchiver}
              sx={{ fontWeight: 700 }}
            >
              Étape 8 : Archiver officiellement (HCEP / HCEE)
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
              <Box key={v.id} sx={{ p: 2, mb: 2, border: '1px solid #e2e8f0', borderRadius: 2, bgcolor: '#f8fafc' }}>
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
              <Box key={h.id} sx={{ p: 2, mb: 1.5, borderLeft: '4px solid #00875A', bgcolor: '#f8fafc', borderRadius: 1 }}>
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
