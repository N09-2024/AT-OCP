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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Checkbox,
  FormControlLabel,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Chip,
} from '@mui/material';

import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import VerifiedIcon from '@mui/icons-material/Verified';
import ArchiveIcon from '@mui/icons-material/Archive';
import EditIcon from '@mui/icons-material/Edit';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import TaskAltIcon from '@mui/icons-material/TaskAlt';
import ErrorIcon from '@mui/icons-material/Error';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';

import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import { visaApi } from '../../../services/visaApi';
import { archiveApi } from '../../../services/archiveApi';
import { interventionApi, type ReadinessCheckItem } from '../../../services/interventionApi';
import { reconductionApi } from '../../../services/reconductionApi';

import type {
  AutorisationTravail,
  Visa,
  HistoriqueAT,
} from '../../../types';

import FormulaireOCPViewer from '../../../components/common/FormulaireOCPViewer';
import { useAuthStore } from '../../../store/authStore';
import { usePopin } from '../../../contexts/PopinContext';

export default function AutorisationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const popin = usePopin();

  const user = useAuthStore((s) => s.user);

  // ============================================================
  // ÉTAT PRINCIPAL
  // ============================================================

  const [loading, setLoading] = useState(true);

  const [at, setAt] = useState<AutorisationTravail | null>(null);

  const [visas, setVisas] = useState<Visa[]>([]);

  const [historiques, setHistoriques] = useState<HistoriqueAT[]>([]);

  const [tabIndex, setTabIndex] = useState(0);

  const [pdfLoading, setPdfLoading] = useState(false);

  const [actionLoading, setActionLoading] = useState(false);

  // ============================================================
  // DIALOGUE READINESS CHECK
  // ============================================================

  const [readinessOpen, setReadinessOpen] = useState(false);

  const [readinessChecks, setReadinessChecks] = useState<
    ReadinessCheckItem[]
  >([]);

  const [readinessLoading, setReadinessLoading] = useState(false);

  // ============================================================
  // DIALOGUE RECONDUCTION
  // CEEE -> HMEP
  // ============================================================

  const [reconductionOpen, setReconductionOpen] = useState(false);

  const [reconductionDate, setReconductionDate] = useState('');

  const [reconductionMotif, setReconductionMotif] = useState('');

  const [reconductionLoading, setReconductionLoading] = useState(false);

  // ============================================================
  // DIALOGUE FIN DES TRAVAUX
  // ============================================================

  const [finOpen, setFinOpen] = useState(false);

  const [rapportFin, setRapportFin] = useState('');

  const [materielEvacue, setMaterielEvacue] = useState(false);

  const [zoneNettoyee, setZoneNettoyee] = useState(false);

  const [consignationRetiree, setConsignationRetiree] = useState(false);

  const [finLoading, setFinLoading] = useState(false);

  // ============================================================
  // CHARGEMENT DES DONNÉES
  // ============================================================

  const loadDetails = async () => {
    if (!id) {
      return;
    }

    setLoading(true);

    try {
      const data = await autorisationTravailApi.findById(id);

      setAt(data);

      const visaList = await visaApi.getVisasByAtId(id);

      setVisas(visaList || []);

      const histoList = await autorisationTravailApi.getHistorique(id);

      setHistoriques(histoList || []);
    } catch (err) {
      console.error('Erreur lors du chargement de l’AT :', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetails();
  }, [id]);

  // ============================================================
  // EXPORT PDF
  // ============================================================

  const handleExportPdf = async () => {
    if (!id || !at) {
      return;
    }

    setPdfLoading(true);

    try {
      const blob = await autorisationTravailApi.exportPdf(id);

      const url = window.URL.createObjectURL(blob);

      const a = document.createElement('a');

      a.href = url;

      a.download = `${at.numero || 'AT'}.pdf`;

      document.body.appendChild(a);

      a.click();

      document.body.removeChild(a);

      window.URL.revokeObjectURL(url);
    } catch (err: any) {
      popin.alert({
        title: 'Export PDF',
        message: err?.response?.data?.message || 'Erreur lors de la génération du PDF officiel.',
        severity: 'warning',
      });
    } finally {
      setPdfLoading(false);
    }
  };

  // ============================================================
  // ARCHIVAGE
  // HCEP / HCEE
  // ============================================================

  const handleArchiver = async () => {
    if (!id || !at) {
      return;
    }

    try {
      await archiveApi.archiverAT(id);
      popin.toast({ message: 'AT archivée officiellement avec succès (Étape 8).', severity: 'success' });
      await loadDetails();
    } catch (err: any) {
      popin.alert({
        title: 'Archivage',
        message: err?.response?.data?.message || "Erreur lors de l'archivage.",
        severity: 'error',
      });
    }
  };

  // ============================================================
  // READINESS CHECK
  // ============================================================

  const handleOpenReadiness = async () => {
    if (!id) {
      return;
    }

    setReadinessLoading(true);

    setReadinessOpen(true);

    try {
      const result = await interventionApi.getReadiness(id);

      setReadinessChecks(result.checks || []);
    } catch (err) {
      console.error(
        'Erreur lors du readiness check :',
        err
      );

      setReadinessChecks([]);
    } finally {
      setReadinessLoading(false);
    }
  };

  // ============================================================
  // DÉMARRER INTERVENTION
  // CEEE
  // ============================================================

  const handleConfirmerDemarrage = async () => {
    if (!id) {
      return;
    }

    setActionLoading(true);

    try {
      await interventionApi.start(id, {
        confirmationCeee: true,
      });

      setReadinessOpen(false);
      popin.toast({
        message: 'Démarrage des travaux enregistré avec succès. Statut : INTERVENTION EN COURS.',
        severity: 'success',
      });
      await loadDetails();
    } catch (err: any) {
      popin.alert({
        title: 'Démarrage des travaux',
        message: err?.response?.data?.message || 'Erreur lors du démarrage des travaux.',
        severity: 'error',
      });
    } finally {
      setActionLoading(false);
    }
  };

  // ============================================================
  // RECONDUCTION
  // CEEE -> HMEP
  // ============================================================

  const handleOpenReconduction = () => {
    setReconductionDate('');

    setReconductionMotif('');

    setReconductionOpen(true);
  };

  const handleSoumettreReconduction = async () => {
    if (
      !id ||
      !at ||
      !reconductionDate ||
      !reconductionMotif.trim()
    ) {
      popin.alert({
        title: 'Champs requis',
        message: 'Veuillez renseigner la nouvelle date de fin et le motif de la reconduction.',
        severity: 'warning',
      });
      return;
    }

    setReconductionLoading(true);

    try {
      await reconductionApi.demander({
        atId: id,
        nouvelleDateFin: reconductionDate,
        motif: reconductionMotif.trim(),
      });

      setReconductionOpen(false);
      popin.toast({
        message: 'Demande de reconduction soumise au HMEP. Vous serez notifié de la décision.',
        severity: 'success',
      });
      await loadDetails();
    } catch (err: any) {
      popin.alert({
        title: 'Reconduction',
        message: err?.response?.data?.message || 'Erreur lors de la demande de reconduction.',
        severity: 'error',
      });
    } finally {
      setReconductionLoading(false);
    }
  };

  // ============================================================
  // FIN DES TRAVAUX
  // CEEE
  // ============================================================

  const handleOpenFin = () => {
    setRapportFin('');

    setMaterielEvacue(false);

    setZoneNettoyee(false);

    setConsignationRetiree(false);

    setFinOpen(true);
  };

  const handleConfirmerFin = async () => {
    if (!id) {
      return;
    }

    if (
      !materielEvacue ||
      !zoneNettoyee ||
      !consignationRetiree
    ) {
      popin.alert({
        title: 'Vérifications obligatoires',
        message: 'Veuillez confirmer toutes les vérifications de fin de chantier avant de déclarer la fin des travaux.',
        severity: 'warning',
      });
      return;
    }

    setFinLoading(true);

    try {
      await interventionApi.end(id, {
        travauxRealises: rapportFin.trim(),
        materielRetire: materielEvacue,
        zoneNettoyee,
        protectionsRetablies: consignationRetiree,
      });

      setFinOpen(false);
      popin.toast({
        message: 'Fin des travaux déclarée par le CEEE avec succès. Prêt pour réception conjointe.',
        severity: 'success',
      });
      await loadDetails();
    } catch (err: any) {
      popin.alert({
        title: 'Déclaration de fin',
        message: err?.response?.data?.message || 'Erreur lors de la déclaration de fin.',
        severity: 'error',
      });
    } finally {
      setFinLoading(false);
    }
  };

  // ============================================================
  // LOADING
  // ============================================================

  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '60vh',
        }}
      >
        <CircularProgress color="success" />
      </Box>
    );
  }

  // ============================================================
  // AT INTROUVABLE
  // ============================================================

  if (!at) {
    return (
      <Box
        sx={{
          p: 4,
          textAlign: 'center',
        }}
      >
        <Alert severity="error">
          Autorisation de travail introuvable.
        </Alert>

        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/autorisations')}
          sx={{ mt: 2 }}
        >
          Retour à la liste
        </Button>
      </Box>
    );
  }

  // ============================================================
  // RÔLES
  // ============================================================

  const roles: string[] = (user?.roles || []).map(
    (r: any) =>
      typeof r === 'string'
        ? r.toUpperCase()
        : (r?.nom || r?.name || '').toUpperCase()
  );

  const isAdmin = roles.includes('ADMIN');

  // Rôles stricts

  const isCeepStrict = roles.includes('CEEP');

  const isCeeeStrict = roles.includes('CEEE');

  const isHcepStrict = roles.includes('HCEP');

  const isHceeStrict = roles.includes('HCEE');

  const isHmepStrict = roles.includes('HMEP');

  const isHmeeStrict = roles.includes('HMEE');

  // Rôles synthétiques

  const isCeSynth = roles.includes('CE');

  const isHcSynth = roles.includes('HC');

  const isHmSynth = roles.includes('HM');

  // Droits effectifs

  const isCeep =
    isCeepStrict ||
    isCeSynth ||
    isAdmin;

  const isCeee =
    isCeeeStrict ||
    isCeSynth ||
    isAdmin;

  const isHcep =
    isHcepStrict ||
    isHcSynth ||
    isAdmin;

  const isHcee =
    isHceeStrict ||
    isHcSynth ||
    isAdmin;

  const isHmep =
    isHmepStrict ||
    isHmSynth ||
    isAdmin;

  const isHmee =
    isHmeeStrict ||
    isHmSynth ||
    isAdmin;

  const isCe = isCeep || isCeee;

  const isHc = isHcep || isHcee;

  // ============================================================
  // STATUT
  // ============================================================

  const statut = at.statut as string;

  // ============================================================
  // DÉTECTION DES VISAS
  // ============================================================

  const isPositiveVisa = (v: Visa) =>
    v.statut === 'VALIDE' ||
    v.statut === 'VALIDATION' ||
    v.statut === 'SIGNATURE';

  const detectVisa = (roleKeyword: string) =>
    visas.some((v) => {
      if (!isPositiveVisa(v)) {
        return false;
      }

      const comment =
        (v.commentaire || '').toUpperCase();

      const keyword =
        roleKeyword.toUpperCase();

      if (comment.includes(keyword)) {
        return true;
      }

      const userRoles: string[] =
        ((v as any).utilisateur?.roles || []).map(
          (r: any) =>
            (r?.nom || r?.name || '').toUpperCase()
        );

      if (
        userRoles.some(
          (r) =>
            r === keyword ||
            r.includes(keyword)
        )
      ) {
        return true;
      }

      const directRole = (
        (v as any).role ||
        (v as any).utilisateurRole ||
        (v as any).roleSignataire ||
        ''
      ).toUpperCase();

      if (
        directRole === keyword ||
        directRole.includes(keyword)
      ) {
        return true;
      }

      return false;
    });

  const hasCeeeVisaExact =
    detectVisa('CEEE');

  const hasCeeeVisa =
    hasCeeeVisaExact ||
    statut === 'EN_ATTENTE_VALIDATION' ||
    statut === 'VALIDEE' ||
    statut === 'AT_VALIDEE' ||
    statut === 'AT_REDIGEE' ||
    Boolean((at as any).dateReceptionCeee);

  const hasHcepVisa =
    detectVisa('HCEP');

  const hasHceeVisa =
    detectVisa('HCEE');

  const hasHmepVisa =
    detectVisa('HMEP');

  const hasHmeeVisa =
    detectVisa('HMEE');

  const allSignaturesComplete =
    hasCeeeVisa &&
    hasHcepVisa &&
    hasHceeVisa &&
    hasHmepVisa &&
    hasHmeeVisa;

  // ============================================================
  // RÈGLES DE WORKFLOW
  // ============================================================

  const canSignVisaStatus =
    statut === 'SOUMISE' ||
    statut === 'AT_REDIGEE' ||
    statut === 'EN_ATTENTE_VALIDATION' ||
    statut === 'VALIDEE' ||
    statut === 'AT_VALIDEE';

  // Étape 0
  const showEditDraft =
    statut === 'BROUILLON' &&
    isCeep;

  // Visa CEEE
  const showSignCeee =
    canSignVisaStatus &&
    isCeee &&
    !hasCeeeVisaExact;

  // Visa HCEP
  const showSignHcep =
    canSignVisaStatus &&
    isHcep &&
    hasCeeeVisa &&
    !hasHcepVisa;

  // Visa HCEE
  const showSignHcee =
    canSignVisaStatus &&
    isHcee &&
    hasHcepVisa &&
    !hasHceeVisa;

  // Visa HMEP
  const showSignHmep =
    canSignVisaStatus &&
    isHmep &&
    hasHceeVisa &&
    !hasHmepVisa;

  // Visa HMEE
  const showSignHmee =
    canSignVisaStatus &&
    isHmee &&
    hasHmepVisa &&
    !hasHmeeVisa;

  // Étape 4
  const showDemarrer =
    (statut === 'VALIDEE' ||
      statut === 'AT_VALIDEE') &&
    isCeee;

  // Étape 5
  const showReconduire =
    (
      statut === 'INTERVENTION_EN_COURS' ||
      statut === 'AT_RECONDUITE'
    ) &&
    isCeee;

  // Étape 6
  const showDeclarerFin =
    (
      statut === 'INTERVENTION_EN_COURS' ||
      statut === 'AT_RECONDUITE'
    ) &&
    isCeee;

  // Étape 7
  const showReceptionner =
    statut === 'FIN_TRAVAUX_DECLAREE' &&
    isCe;

  // ============================================================
  // PDF
  // ============================================================

  const pdfUnlocked =
    hasHmeeVisa ||
    allSignaturesComplete ||
    statut === 'CLOTUREE' ||
    statut === 'TRAVAUX_RECEPTIONES' ||
    statut === 'ARCHIVEE';

  const isWorkflowParticipant =
    isCeep ||
    isCeee ||
    isHcep ||
    isHcee ||
    isHmep ||
    isHmee ||
    isAdmin;

  const showPdf =
    pdfUnlocked &&
    isWorkflowParticipant;

  // ============================================================
  // ARCHIVAGE
  // HCEP / HCEE
  // ============================================================

  const showArchiver =
    (
      statut === 'CLOTUREE' ||
      statut === 'TRAVAUX_RECEPTIONES'
    ) &&
    isHc;

  // ============================================================
  // RENDER
  // ============================================================

  return (
    <>
      {/* ========================================================
          CONTENU PRINCIPAL
      ======================================================== */}

      <Box
        sx={{
          p: 3,
          maxWidth: 1100,
          mx: 'auto',
        }}
      >
        {/* HEADER */}

        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 3,
            gap: 2,
          }}
        >
          <Button
            startIcon={<ArrowBackIcon />}
            onClick={() =>
              navigate('/autorisations')
            }
            sx={{
              color: '#5C6E67',
              flexShrink: 0,
            }}
          >
            Retour aux ATs
          </Button>

          <Stack
            direction="row"
            spacing={1.5}
            sx={{
              flexWrap: 'wrap',
              gap: 1,
              justifyContent: 'flex-end',
            }}
          >
            {/* ==================================================
                ÉTAPE 0 - BROUILLON
            ================================================== */}

            {showEditDraft && (
              <Button
                variant="contained"
                startIcon={<EditIcon />}
                onClick={() =>
                  navigate(
                    `/autorisations/${at.id}/editer`
                  )
                }
                sx={{
                  fontWeight: 700,
                  background: '#A87532',
                  '&:hover': {
                    background: '#A87532',
                  },
                }}
              >
                Reprendre le brouillon
              </Button>
            )}

            {/* ==================================================
                VISA CEEE
            ================================================== */}

            {showSignCeee && (
              <Tooltip
                title="Accuser réception et apposer votre visa CEEE"
              >
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={() =>
                    navigate(
                      `/autorisations/${at.id}/signature-ceee`
                    )
                  }
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Signer l'AT (Visa CEEE)
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                VISA HCEP
            ================================================== */}

            {showSignHcep && (
              <Tooltip
                title="Apposer votre visa en tant que Hors Cadre Propriétaire (HCEP)"
              >
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={() =>
                    navigate(
                      `/visas/validation/${at.id}?role=HCEP`
                    )
                  }
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Signer l'AT (Visa HCEP)
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                VISA HCEE
            ================================================== */}

            {showSignHcee && (
              <Tooltip
                title="Apposer votre visa en tant que Hors Cadre Exécutant (HCEE)"
              >
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={() =>
                    navigate(
                      `/visas/validation/${at.id}?role=HCEE`
                    )
                  }
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Signer l'AT (Visa HCEE)
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                VISA HMEP
            ================================================== */}

            {showSignHmep && (
              <Tooltip
                title="Apposer votre visa en tant que Haute Maîtrise Propriétaire (HMEP)"
              >
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={() =>
                    navigate(
                      `/visas/validation/${at.id}?role=HMEP`
                    )
                  }
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Signer l'AT (Visa HMEP)
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                VISA HMEE
            ================================================== */}

            {showSignHmee && (
              <Tooltip
                title="Apposer votre visa en tant que Haute Maîtrise Exécutante (HMEE)"
              >
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={() =>
                    navigate(
                      `/visas/validation/${at.id}?role=HMEE`
                    )
                  }
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Signer l'AT (Visa HMEE)
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                ÉTAPE 4 - DÉMARRER
            ================================================== */}

            {showDemarrer && (
              <Tooltip
                title="Vérifier les conditions pré-démarrage"
              >
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<PlayArrowIcon />}
                  onClick={handleOpenReadiness}
                  disabled={actionLoading}
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Démarrer l'intervention
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                ÉTAPE 5 - RECONDUCTION
                CEEE -> HMEP
            ================================================== */}

            {showReconduire && (
              <Tooltip
                title="Demander une reconduction au Responsable OCP (HMEP)"
              >
                <Button
                  variant="outlined"
                  color="warning"
                  startIcon={<AutorenewIcon />}
                  onClick={handleOpenReconduction}
                  disabled={actionLoading}
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Demander une reconduction
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                ÉTAPE 6 - FIN TRAVAUX
            ================================================== */}

            {showDeclarerFin && (
              <Tooltip
                title="Déclarer la fin des travaux"
              >
                <Button
                  variant="contained"
                  color="info"
                  startIcon={<TaskAltIcon />}
                  onClick={handleOpenFin}
                  disabled={actionLoading}
                  sx={{
                    fontWeight: 700,
                  }}
                >
                  Déclarer la fin des travaux
                </Button>
              </Tooltip>
            )}

            {/* ==================================================
                ÉTAPE 7 - RÉCEPTION
            ================================================== */}

            {showReceptionner && (
              <Button
                variant="contained"
                color="primary"
                startIcon={<VerifiedIcon />}
                onClick={() =>
                  navigate(
                    `/receptions?atId=${at.id}`
                  )
                }
                sx={{
                  fontWeight: 700,
                }}
              >
                Réception conjointe & Clôture
              </Button>
            )}

            {/* ==================================================
                PDF OFFICIEL
            ================================================== */}

            {showPdf && (
              <Button
                variant="outlined"
                color="success"
                startIcon={
                  pdfLoading ? (
                    <CircularProgress
                      size={18}
                      color="inherit"
                    />
                  ) : (
                    <PictureAsPdfIcon />
                  )
                }
                onClick={handleExportPdf}
                disabled={pdfLoading}
                sx={{
                  fontWeight: 700,
                }}
              >
                Télécharger le PDF Officiel
              </Button>
            )}

            {/* ==================================================
                ÉTAPE 8 - ARCHIVAGE
            ================================================== */}

            {showArchiver && (
              <Button
                variant="outlined"
                color="secondary"
                startIcon={<ArchiveIcon />}
                onClick={handleArchiver}
                sx={{
                  fontWeight: 700,
                }}
              >
                Archiver officiellement
              </Button>
            )}
          </Stack>
        </Box>

        {/* ======================================================
            MESSAGE PDF
        ====================================================== */}

        {at.exportPdfAutorise === false &&
          (at.exportPdfMotifsRefus || []).length > 0 && (
            <Alert
              severity="warning"
              sx={{ mb: 3 }}
            >
              <Typography
                variant="subtitle2"
                sx={{ fontWeight: 800 }}
              >
                Conditions requises pour télécharger
                le document PDF officiel :
              </Typography>

              <ul
                style={{
                  margin: '4px 0 0 16px',
                  padding: 0,
                }}
              >
                {at.exportPdfMotifsRefus?.map(
                  (motif, index) => (
                    <li key={index}>
                      {motif}
                    </li>
                  )
                )}
              </ul>
            </Alert>
          )}

        {/* ======================================================
            TABS
        ====================================================== */}

        <Box
          sx={{
            borderBottom: 1,
            borderColor: 'divider',
            mb: 3,
          }}
        >
          <Tabs
            value={tabIndex}
            onChange={(_, value) =>
              setTabIndex(value)
            }
          >
            <Tab label="Formulaire Officiel F-HSE-SEC-31-04" />

            <Tab
              label={`Visas (${visas.length})`}
            />

            <Tab
              label={`Historique d'Audit (${historiques.length})`}
            />
          </Tabs>
        </Box>

        {/* ======================================================
            TAB 0 - FORMULAIRE
        ====================================================== */}

        {tabIndex === 0 && (
          <FormulaireOCPViewer
            at={at}
            visas={visas}
          />
        )}

        {/* ======================================================
            TAB 1 - VISAS
        ====================================================== */}

        {tabIndex === 1 && (
          <Paper
            sx={{
              p: 3,
              borderRadius: 2,
            }}
          >
            <Typography
              variant="h6"
              sx={{
                fontWeight: 700,
                mb: 2,
              }}
            >
              Liste des Visas & Signatures
            </Typography>

            {visas.length === 0 ? (
              <Alert severity="info">
                Aucun visa enregistré pour le moment.
              </Alert>
            ) : (
              visas.map((v) => (
                <Box
                  key={v.id}
                  sx={{
                    p: 2,
                    mb: 2,
                    border:
                      '1px solid #D6E3DC',
                    borderRadius: 2,
                    bgcolor: '#F7FAF8',
                  }}
                >
                  <Box
                    sx={{
                      display: 'flex',
                      justifyContent:
                        'space-between',
                      alignItems: 'center',
                    }}
                  >
                    <Typography
                      variant="subtitle2"
                      sx={{ fontWeight: 700 }}
                    >
                      Signataire :{' '}
                      {v.utilisateurNomComplet ||
                        'Utilisateur'}
                    </Typography>

                    <Typography
                      variant="caption"
                      color="text.secondary"
                    >
                      {v.dateSignature
                        ? new Date(
                            v.dateSignature
                          ).toLocaleString(
                            'fr-FR'
                          )
                        : 'En attente'}
                    </Typography>
                  </Box>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mt: 0.5 }}
                  >
                    Statut :{' '}
                    <strong>
                      {v.statut}
                    </strong>{' '}
                    | IP :{' '}
                    {v.adresseIP ||
                      'Non capturée'}
                  </Typography>

                  {v.commentaire && (
                    <Typography
                      variant="body2"
                      sx={{
                        fontStyle: 'italic',
                        mt: 1,
                        p: 1,
                        bgcolor: 'white',
                        borderRadius: 1,
                      }}
                    >
                      « {v.commentaire} »
                    </Typography>
                  )}
                </Box>
              ))
            )}
          </Paper>
        )}

        {/* ======================================================
            TAB 2 - HISTORIQUE
        ====================================================== */}

        {tabIndex === 2 && (
          <Paper
            sx={{
              p: 3,
              borderRadius: 2,
            }}
          >
            <Typography
              variant="h6"
              sx={{
                fontWeight: 700,
                mb: 2,
              }}
            >
              Journal des Événements & Audit
            </Typography>

            {historiques.length === 0 ? (
              <Alert severity="info">
                Aucun événement enregistré.
              </Alert>
            ) : (
              historiques.map((h) => (
                <Box
                  key={h.id}
                  sx={{
                    p: 2,
                    mb: 1.5,
                    borderLeft:
                      '4px solid #1F4D3E',
                    bgcolor: '#F7FAF8',
                    borderRadius: 1,
                  }}
                >
                  <Box
                    sx={{
                      display: 'flex',
                      justifyContent:
                        'space-between',
                    }}
                  >
                    <Typography
                      variant="subtitle2"
                      sx={{ fontWeight: 700 }}
                    >
                      {h.action} par{' '}
                      {h.utilisateurNomComplet ||
                        'Système'}
                    </Typography>

                    <Typography
                      variant="caption"
                      color="text.secondary"
                    >
                      {new Date(
                        h.dateAction
                      ).toLocaleString(
                        'fr-FR'
                      )}
                    </Typography>
                  </Box>

                  {h.commentaire && (
                    <Typography
                      variant="body2"
                      color="text.secondary"
                      sx={{ mt: 0.5 }}
                    >
                      {h.commentaire}
                    </Typography>
                  )}
                </Box>
              ))
            )}
          </Paper>
        )}
      </Box>

      {/* ========================================================
          DIALOGUE READINESS CHECK
      ======================================================== */}

      <Dialog
        open={readinessOpen}
        onClose={() =>
          setReadinessOpen(false)
        }
        maxWidth="md"
        fullWidth
      >
        <DialogTitle
          sx={{
            fontWeight: 800,
            bgcolor: '#1F4D3E',
            color: 'white',
          }}
        >
          Contrôle Pré-Démarrage
        </DialogTitle>

        <DialogContent sx={{ p: 3 }}>
          {readinessLoading ? (
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'center',
                py: 4,
              }}
            >
              <CircularProgress color="success" />
            </Box>
          ) : (
            <>
              <Alert
                severity="info"
                sx={{ mb: 2 }}
              >
                Les conditions suivantes doivent
                être satisfaites avant de démarrer
                l'intervention.
              </Alert>

              <List dense>
                {readinessChecks.map(
                  (check) => (
                    <ListItem
                      key={check.code}
                      sx={{
                        mb: 0.5,
                        borderRadius: 1,
                        bgcolor: check.passed
                          ? '#F0FAF5'
                          : check.blocking
                            ? '#FFF3F3'
                            : '#FFF8E1',
                        border: `1px solid ${
                          check.passed
                            ? '#C8E6C9'
                            : check.blocking
                              ? '#FFCDD2'
                              : '#FFE082'
                        }`,
                      }}
                    >
                      <ListItemIcon
                        sx={{
                          minWidth: 36,
                        }}
                      >
                        {check.passed ? (
                          <CheckCircleIcon color="success" />
                        ) : check.blocking ? (
                          <ErrorIcon color="error" />
                        ) : (
                          <WarningAmberIcon color="warning" />
                        )}
                      </ListItemIcon>

                      <ListItemText
                        primary={
                          <Box
                            sx={{
                              display: 'flex',
                              alignItems:
                                'center',
                              gap: 1,
                            }}
                          >
                            <Typography
                              variant="body2"
                              sx={{
                                fontWeight: 600,
                              }}
                            >
                              {check.label}
                            </Typography>

                            {check.blocking &&
                              !check.passed && (
                                <Chip
                                  label="BLOQUANT"
                                  size="small"
                                  color="error"
                                />
                              )}
                          </Box>
                        }
                        secondary={
                          check.message
                        }
                      />
                    </ListItem>
                  )
                )}
              </List>

              {readinessChecks.filter(
                (c) =>
                  !c.passed &&
                  c.blocking
              ).length > 0 && (
                <Alert
                  severity="error"
                  sx={{ mt: 2 }}
                >
                  {
                    readinessChecks.filter(
                      (c) =>
                        !c.passed &&
                        c.blocking
                    ).length
                  }{' '}
                  condition(s) bloquante(s)
                  détectée(s). Résolvez-les
                  avant de démarrer.
                </Alert>
              )}
            </>
          )}
        </DialogContent>

        <DialogActions
          sx={{
            p: 2,
            gap: 1,
          }}
        >
          <Button
            onClick={() =>
              setReadinessOpen(false)
            }
            color="inherit"
          >
            Annuler
          </Button>

          <Button
            variant="contained"
            color="success"
            startIcon={<PlayArrowIcon />}
            disabled={
              actionLoading ||
              readinessLoading ||
              readinessChecks.some(
                (c) =>
                  !c.passed &&
                  c.blocking
              )
            }
            onClick={
              handleConfirmerDemarrage
            }
            sx={{
              fontWeight: 700,
            }}
          >
            {actionLoading ? (
              <CircularProgress
                size={20}
                color="inherit"
              />
            ) : (
              'Confirmer le démarrage'
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* ========================================================
          DIALOGUE RECONDUCTION
          CEEE -> HMEP
      ======================================================== */}

      <Dialog
        open={reconductionOpen}
        onClose={() =>
          setReconductionOpen(false)
        }
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle
          sx={{
            fontWeight: 800,
            bgcolor: '#E65100',
            color: 'white',
          }}
        >
          Demande de Reconduction
        </DialogTitle>

        <DialogContent sx={{ p: 3 }}>
          <Alert
            severity="warning"
            sx={{ mb: 3 }}
          >
            <Typography variant="body2">
              La demande sera transmise au{' '}
              <strong>
                Responsable OCP (HMEP)
              </strong>{' '}
              pour approbation ou refus motivé.
            </Typography>
          </Alert>

          <TextField
            label="Nouvelle date/heure de fin"
            type="datetime-local"
            fullWidth
            value={reconductionDate}
            onChange={(e) =>
              setReconductionDate(
                e.target.value
              )
            }
            slotProps={{
              inputLabel: {
                shrink: true,
              },
            }}
            sx={{ mb: 2 }}
          />

          <TextField
            label="Motif de la reconduction *"
            multiline
            rows={4}
            fullWidth
            value={reconductionMotif}
            onChange={(e) =>
              setReconductionMotif(
                e.target.value
              )
            }
            placeholder="Expliquer pourquoi la reconduction est nécessaire."
          />
        </DialogContent>

        <DialogActions
          sx={{
            p: 2,
            gap: 1,
          }}
        >
          <Button
            onClick={() =>
              setReconductionOpen(false)
            }
            color="inherit"
          >
            Annuler
          </Button>

          <Button
            variant="contained"
            color="warning"
            startIcon={<AutorenewIcon />}
            disabled={
              reconductionLoading ||
              !reconductionDate ||
              !reconductionMotif.trim()
            }
            onClick={
              handleSoumettreReconduction
            }
            sx={{
              fontWeight: 700,
            }}
          >
            {reconductionLoading ? (
              <CircularProgress
                size={20}
                color="inherit"
              />
            ) : (
              'Soumettre au HMEP'
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* ========================================================
          DIALOGUE FIN DES TRAVAUX
          CEEE
      ======================================================== */}

      <Dialog
        open={finOpen}
        onClose={() => setFinOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle
          sx={{
            fontWeight: 800,
            bgcolor: '#0288D1',
            color: 'white',
          }}
        >
          Déclaration de Fin des Travaux
        </DialogTitle>

        <DialogContent sx={{ p: 3 }}>
          <Alert
            severity="info"
            sx={{ mb: 2 }}
          >
            Confirmez les vérifications
            obligatoires de fin de chantier avant
            de déclarer la fin.
          </Alert>

          <FormControlLabel
            control={
              <Checkbox
                checked={materielEvacue}
                onChange={(e) =>
                  setMaterielEvacue(
                    e.target.checked
                  )
                }
                color="success"
              />
            }
            label="Tout le matériel et les outils ont été évacués de la zone"
          />

          <FormControlLabel
            control={
              <Checkbox
                checked={zoneNettoyee}
                onChange={(e) =>
                  setZoneNettoyee(
                    e.target.checked
                  )
                }
                color="success"
              />
            }
            label="La zone de travail est nettoyée et remise en état"
          />

          <FormControlLabel
            control={
              <Checkbox
                checked={consignationRetiree}
                onChange={(e) =>
                  setConsignationRetiree(
                    e.target.checked
                  )
                }
                color="success"
              />
            }
            label="La consignation / déconsignation a été réalisée conformément"
          />

          <Divider sx={{ my: 2 }} />

          <TextField
            label="Rapport de fin de chantier (observations)"
            multiline
            rows={3}
            fullWidth
            value={rapportFin}
            onChange={(e) =>
              setRapportFin(
                e.target.value
              )
            }
            placeholder="Résumé des travaux réalisés, anomalies constatées, réserves éventuelles..."
          />
        </DialogContent>

        <DialogActions
          sx={{
            p: 2,
            gap: 1,
          }}
        >
          <Button
            onClick={() =>
              setFinOpen(false)
            }
            color="inherit"
          >
            Annuler
          </Button>

          <Button
            variant="contained"
            color="info"
            startIcon={<TaskAltIcon />}
            disabled={
              finLoading ||
              !materielEvacue ||
              !zoneNettoyee ||
              !consignationRetiree
            }
            onClick={handleConfirmerFin}
            sx={{
              fontWeight: 700,
            }}
          >
            {finLoading ? (
              <CircularProgress
                size={20}
                color="inherit"
              />
            ) : (
              'Déclarer la fin'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}