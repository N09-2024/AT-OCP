import React, { useEffect, useState } from 'react';
import {
  Box, Typography, Button, CircularProgress, Stack, Paper,
  Alert, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Divider,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import AutorenewIcon from '@mui/icons-material/Autorenew';
import { reconductionApi, type ReconductionResponse } from '../../services/reconductionApi';
import { useNavigate } from 'react-router-dom';
import { usePopin } from '../../contexts/PopinContext';

const statutColor: Record<string, 'warning' | 'success' | 'error' | 'default'> = {
  REQUESTED: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  CANCELLED: 'default',
};

const statutLabel: Record<string, string> = {
  REQUESTED: 'En attente de décision',
  APPROVED: 'Approuvée',
  REJECTED: 'Refusée',
  CANCELLED: 'Annulée',
};

export default function ReconductionDecisionPage() {
  const navigate = useNavigate();
  const popin = usePopin();
  const [loading, setLoading] = useState(true);
  const [reconductions, setReconductions] = useState<ReconductionResponse[]>([]);
  const [selected, setSelected] = useState<ReconductionResponse | null>(null);
  const [dialogMode, setDialogMode] = useState<'approve' | 'reject' | null>(null);
  const [commentaire, setCommentaire] = useState('');
  const [motifRefus, setMotifRefus] = useState('');
  const [submitLoading, setSubmitLoading] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await reconductionApi.getPending();
      setReconductions(data);
    } catch {
      setReconductions([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleOpenApprove = (r: ReconductionResponse) => {
    setSelected(r);
    setCommentaire('');
    setMotifRefus('');
    setDialogMode('approve');
  };

  const handleOpenReject = (r: ReconductionResponse) => {
    setSelected(r);
    setMotifRefus('');
    setCommentaire('');
    setDialogMode('reject');
  };

  const handleSubmit = async () => {
    if (!selected) return;
    if (dialogMode === 'reject' && !motifRefus.trim()) {
      popin.alert({
        title: 'Motif obligatoire',
        message: 'Le motif de refus est obligatoire.',
        severity: 'warning',
      });
      return;
    }
    setSubmitLoading(true);
    try {
      await reconductionApi.decider(selected.id, {
        approuve: dialogMode === 'approve',
        commentaire,
        motifRefus: dialogMode === 'reject' ? motifRefus : undefined,
      });
      setDialogMode(null);
      setSelected(null);
      popin.toast({
        message:
          dialogMode === 'approve'
            ? 'Reconduction approuvée. L\'AT a été prolongée et le CEEE notifié.'
            : 'Reconduction refusée. Le CEEE a été notifié avec le motif de refus.',
        severity: dialogMode === 'approve' ? 'success' : 'warning',
      });
      load();
    } catch (err: any) {
      popin.alert({
        title: 'Erreur',
        message: err.response?.data?.message || 'Erreur lors de la décision.',
        severity: 'error',
      });
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <Box sx={{ p: 3, maxWidth: 1000, mx: 'auto' }}>
      <Typography variant="h5" sx={{ fontWeight: 800, color: '#1F4D3E', mb: 1 }}>
        🔄 Décisions de Reconduction
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Responsable OCP (HMEP) — Approuvez ou refusez les demandes de prolongation soumises par les CEEE.
      </Typography>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress color="success" />
        </Box>
      ) : reconductions.length === 0 ? (
        <Alert severity="success" icon={<CheckCircleIcon />}>
          Aucune demande de reconduction en attente. Toutes les demandes ont été traitées.
        </Alert>
      ) : (
        <Stack spacing={2}>
          {reconductions.map((r) => (
            <Paper key={r.id} sx={{ p: 3, borderRadius: 2, border: '1px solid #E8F5E9' }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>
                    AT {r.atNumero}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Demandeur : <strong>{r.demandeurPrenom} {r.demandeurNom}</strong>
                    {' · '}
                    {new Date(r.dateDemande).toLocaleString('fr-FR')}
                  </Typography>
                </Box>
                <Chip
                  label={statutLabel[r.statut] || r.statut}
                  color={statutColor[r.statut] || 'default'}
                  size="small"
                />
              </Box>

              <Divider sx={{ my: 1.5 }} />

              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1, mb: 1.5 }}>
                <Box>
                  <Typography variant="caption" color="text.secondary">Nouvelle date de fin demandée</Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {new Date(r.nouvelleDateFin).toLocaleString('fr-FR')}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Motif de la reconduction</Typography>
                  <Typography variant="body2">{r.motif}</Typography>
                </Box>
              </Box>

              {r.statut === 'REQUESTED' && (
                <Stack direction="row" spacing={1.5} sx={{ mt: 2 }}>
                  <Button
                    variant="contained"
                    color="success"
                    startIcon={<CheckCircleIcon />}
                    onClick={() => handleOpenApprove(r)}
                    sx={{ fontWeight: 700 }}
                  >
                    Approuver
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<CancelIcon />}
                    onClick={() => handleOpenReject(r)}
                    sx={{ fontWeight: 700 }}
                  >
                    Refuser (motif obligatoire)
                  </Button>
                  <Button
                    variant="text"
                    color="inherit"
                    onClick={() => navigate(`/autorisations/${r.atId}`)}
                  >
                    Voir l'AT
                  </Button>
                </Stack>
              )}

              {r.statut !== 'REQUESTED' && (
                <Alert
                  severity={r.statut === 'APPROVED' ? 'success' : 'error'}
                  sx={{ mt: 1 }}
                  icon={r.statut === 'APPROVED' ? <CheckCircleIcon /> : <CancelIcon />}
                >
                  {r.statut === 'APPROVED'
                    ? `Approuvée le ${r.dateDecision ? new Date(r.dateDecision).toLocaleString('fr-FR') : '—'} par ${r.decisionParPrenom} ${r.decisionParNom}`
                    : `Refusée — Motif : ${r.motifRefus}`}
                </Alert>
              )}
            </Paper>
          ))}
        </Stack>
      )}

      {/* Dialogue de décision */}
      <Dialog open={dialogMode !== null} onClose={() => setDialogMode(null)} maxWidth="sm" fullWidth>
        <DialogTitle
          sx={{
            fontWeight: 800,
            bgcolor: dialogMode === 'approve' ? '#1F4D3E' : '#C62828',
            color: 'white',
          }}
        >
          {dialogMode === 'approve' ? '✅ Approuver la reconduction' : '❌ Refuser la reconduction'}
        </DialogTitle>
        <DialogContent sx={{ p: 3 }}>
          {selected && (
            <>
              <Alert severity={dialogMode === 'approve' ? 'success' : 'warning'} sx={{ mb: 2 }}>
                <Typography variant="body2">
                  AT <strong>{selected.atNumero}</strong> — Nouvelle échéance demandée :{' '}
                  <strong>{new Date(selected.nouvelleDateFin).toLocaleString('fr-FR')}</strong>
                </Typography>
                <Typography variant="body2" sx={{ mt: 0.5 }}>
                  Motif CEEE : {selected.motif}
                </Typography>
              </Alert>

              {dialogMode === 'reject' && (
                <TextField
                  label="Motif de refus *"
                  multiline
                  rows={3}
                  fullWidth
                  required
                  value={motifRefus}
                  onChange={(e) => setMotifRefus(e.target.value)}
                  placeholder="Expliquer le motif du refus (conditions de sécurité non réunies, durée excessive, etc.)"
                  sx={{ mb: 2 }}
                />
              )}

              <TextField
                label="Commentaire additionnel (optionnel)"
                multiline
                rows={2}
                fullWidth
                value={commentaire}
                onChange={(e) => setCommentaire(e.target.value)}
                placeholder="Observations, recommandations..."
              />
            </>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2, gap: 1 }}>
          <Button onClick={() => setDialogMode(null)} color="inherit">Annuler</Button>
          <Button
            variant="contained"
            color={dialogMode === 'approve' ? 'success' : 'error'}
            startIcon={dialogMode === 'approve' ? <CheckCircleIcon /> : <CancelIcon />}
            disabled={submitLoading || (dialogMode === 'reject' && !motifRefus.trim())}
            onClick={handleSubmit}
            sx={{ fontWeight: 700 }}
          >
            {submitLoading ? <CircularProgress size={20} color="inherit" /> : (
              dialogMode === 'approve' ? 'Confirmer l\'approbation' : 'Confirmer le refus'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
