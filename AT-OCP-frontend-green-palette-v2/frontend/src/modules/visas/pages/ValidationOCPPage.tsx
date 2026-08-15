import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Button,
  CircularProgress,
  Stack,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Grid,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import SignaturePad from '../../../components/common/SignaturePad';
import FormulaireOCPViewer from '../../../components/common/FormulaireOCPViewer';
import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import { visaApi } from '../../../services/visaApi';
import type { AutorisationTravail, Visa } from '../../../types';
import { useAuthStore } from '../../../store/authStore'; // user kept for future role display

export default function ValidationOCPPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const _user = useAuthStore((s) => s.user); // reserved for future display

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [at, setAt] = useState<AutorisationTravail | null>(null);
  const [visas, setVisas] = useState<Visa[]>([]);

  // Visa Signature state
  const [signatureBlob, setSignatureBlob] = useState<Blob | null>(null);
  const [signatureDataUrl, setSignatureDataUrl] = useState<string | null>(null);
  const [visaRecorded, setVisaRecorded] = useState(false);
  const [commentaire, setCommentaire] = useState('');

  // Rejection dialog state
  const [rejectOpen, setRejectOpen] = useState(false);
  const [motifRejet, setMotifRejet] = useState('');

  const loadData = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const atData = await autorisationTravailApi.findById(id);
      setAt(atData);
      const visaList = await visaApi.getVisasByAtId(id);
      setVisas(visaList || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [id]);

  const handleValider = async () => {
    if (!id || !at) return;
    if (!signatureBlob) {
      alert('Votre signature manuscrite est obligatoire pour valider l\'AT.');
      return;
    }

    setSubmitting(true);
    try {
      // Déterminer le rôle contextuel (HCEP, HCEE, HMEP, HMEE)
      const userRoles = _user?.roles?.map((r: any) => r.nom) || [];
      const searchParams = new URLSearchParams(window.location.search);
      const urlRole = searchParams.get('role');

      let targetRoleTag = urlRole || 'HCEE';
      if (!urlRole) {
        if (userRoles.includes('HCEP')) targetRoleTag = 'HCEP';
        else if (userRoles.includes('HCEE')) targetRoleTag = 'HCEE';
        else if (userRoles.includes('HMEP')) targetRoleTag = 'HMEP';
        else if (userRoles.includes('HMEE')) targetRoleTag = 'HMEE';
        else if (userRoles.includes('HC')) targetRoleTag = 'HCEE';
        else if (userRoles.includes('HM')) targetRoleTag = 'HMEP';
      }

      const finalCommentaire = commentaire
        ? `Visa ${targetRoleTag} — ${commentaire}`
        : `Visa ${targetRoleTag} — Signature officielle`;

      // 1. Signer le visa avec l'étiquette de rôle
      await visaApi.createAndSignVisa(id, signatureBlob, finalCommentaire, 2);

      // 2. Valider l'AT
      await autorisationTravailApi.valider(id);

      alert(`Visa ${targetRoleTag} apposé et Autorisation de Travail mise à jour avec succès !`);
      navigate('/autorisations');
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Erreur lors de la validation.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRefuser = async () => {
    if (!id || !at) return;
    if (!motifRejet.trim()) {
      alert('Le motif du refus est obligatoire.');
      return;
    }

    setSubmitting(true);
    try {
      // Si une signature a été apposée, on peut aussi l'enregistrer
      if (signatureBlob) {
        await visaApi.createAndSignVisa(id, signatureBlob, `Refus: ${motifRejet}`, 2);
      }

      await autorisationTravailApi.refuser(id, motifRejet);
      alert('Autorisation de Travail rejetée.');
      setRejectOpen(false);
      navigate('/autorisations');
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Erreur lors du refus.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  if (!at) {
    return (
      <Box sx={{ p: 4, textAlign: 'center' }}>
        <Alert severity="error">AT non trouvée.</Alert>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/autorisations')} sx={{ mt: 2 }}>
          Retour
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, maxWidth: 1100, mx: 'auto' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/autorisations')} sx={{ color: '#5C6E67' }}>
          Retour
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 800, color: '#0E2A21' }}>
          Évaluation & Validation CEEE / HCEE
        </Typography>
      </Box>

      <Alert severity="info" sx={{ mb: 3, fontWeight: 600 }}>
        Examinez l'intégralité du formulaire F-HSE-SEC-31-04 ci-dessous, puis apposez votre visa manuscrit avant de valider ou refuser.
      </Alert>

      {/* Complete Official Form Preview */}
      <Box sx={{ mb: 4 }}>
        <FormulaireOCPViewer at={at} visas={visas} />
      </Box>

      {/* Signature Section for CEEE / HCEE */}
      <Paper sx={{ p: 4, mb: 4, borderRadius: 3, border: '2px solid #1F4D3E' }}>
        <Typography variant="h6" sx={{ fontWeight: 800, color: '#1F4D3E', mb: 2 }}>
          Visa du CEEE / HCEE (CEEP / CEEE)
        </Typography>

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 7 }}>
            <SignaturePad
              title="Votre Visa et Signature Manuscrite"
              onSave={(blob, dataUrl) => {
                setSignatureBlob(blob);
                setSignatureDataUrl(dataUrl);
                setVisaRecorded(true);
              }}
              onClear={() => {
                setSignatureBlob(null);
                setSignatureDataUrl(null);
                setVisaRecorded(false);
              }}
              savedDataUrl={signatureDataUrl}
            />
          </Grid>

          <Grid size={{ xs: 12, md: 5 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, height: '100%', justifyContent: 'space-between' }}>
              <TextField
                fullWidth
                multiline
                rows={4}
                label="Commentaires / Instructions particulières"
                value={commentaire}
                onChange={(e) => setCommentaire(e.target.value)}
                placeholder="Optionnel : saisissez vos remarques ou consignes..."
              />

              <Stack direction="column" spacing={1.5}>
                <Button
                  variant="contained"
                  color="success"
                  size="large"
                  startIcon={submitting ? <CircularProgress size={20} color="inherit" /> : <CheckCircleIcon />}
                  onClick={handleValider}
                  disabled={!visaRecorded || submitting}
                  sx={{ py: 1.5, fontWeight: 800, fontSize: 15 }}
                >
                  Valider et Signer l'AT
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  size="large"
                  startIcon={<CancelIcon />}
                  onClick={() => setRejectOpen(true)}
                  disabled={submitting}
                  sx={{ fontWeight: 700 }}
                >
                  Refuser l'AT
                </Button>
              </Stack>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Reject Modal */}
      <Dialog open={rejectOpen} onClose={() => setRejectOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 800, color: '#9A3D2F' }}>
          Motif du Refus de l'AT {at.numero}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Veuillez justifier la raison du rejet. Le demandeur recevra une notification et pourra corriger le dossier.
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={4}
            required
            label="Motif du refus"
            value={motifRejet}
            onChange={(e) => setMotifRejet(e.target.value)}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setRejectOpen(false)}>Annuler</Button>
          <Button
            variant="contained"
            color="error"
            onClick={handleRefuser}
            disabled={!motifRejet.trim() || submitting}
          >
            Confirmer le Refus
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
